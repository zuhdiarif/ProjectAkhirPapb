package com.zuhdiarifazizi.projectakhirpapb.ui.manageclothes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zuhdiarifazizi.projectakhirpapb.data.repository.DemoClothingCatalog
import com.zuhdiarifazizi.projectakhirpapb.data.storage.ClothingPhotoStorage
import com.zuhdiarifazizi.projectakhirpapb.domain.feedback.HapticFeedbackManager
import com.zuhdiarifazizi.projectakhirpapb.domain.feedback.TextToSpeechManager
import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingItem
import com.zuhdiarifazizi.projectakhirpapb.domain.model.HsvColor
import com.zuhdiarifazizi.projectakhirpapb.domain.repository.ClothesRepository
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ManageClothesViewModel(
    private val clothesRepository: ClothesRepository,
    private val photoStorage: ClothingPhotoStorage,
    private val ttsManager: TextToSpeechManager,
    private val hapticManager: HapticFeedbackManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManageClothesUiState())
    val uiState: StateFlow<ManageClothesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            clothesRepository.observeClothes(DemoClothingCatalog.USER_ID)
                .catch {
                    _uiState.update { it.copy(isLoading = false) }
                    announceError("Koleksi pakaian belum dapat dimuat.")
                }
                .collect { clothes -> _uiState.update { it.copy(items = clothes, isLoading = false) } }
        }
    }

    fun openAddForm() {
        val previousStage = _uiState.value.stagedPhotoUri
        if (previousStage != null && previousStage != _uiState.value.originalPhotoUri) {
            viewModelScope.launch { photoStorage.deleteStoredPhoto(previousStage) }
        }
        _uiState.update {
            it.copy(
                page = ManageClothesPage.FORM,
                selectedItem = null,
                editingItemId = null,
                form = ClothingFormState(),
                originalPhotoUri = null,
                stagedPhotoUri = null,
                errorMessage = null,
                statusMessage = "Form tambah pakaian dibuka."
            )
        }
        announce("Form tambah pakaian dibuka. Isi nama, kategori, warna, motif, dan aktivitas. Foto dapat diambil atau dipilih dari galeri.")
    }

    fun openDetails(item: ClothingItem) {
        _uiState.update {
            it.copy(
                page = ManageClothesPage.DETAIL,
                selectedItem = item,
                showDeleteConfirmation = false,
                errorMessage = null,
                statusMessage = "Detail ${item.name}."
            )
        }
        speakItem(item)
    }

    fun openEditForm(item: ClothingItem) {
        val colorName = ClothingColorOptions.firstOrNull { it.name.equals(item.color.colorName, ignoreCase = true) }?.name
            ?: ClothingColorOptions.minByOrNull {
                kotlin.math.abs(it.hue - item.color.hue) + kotlin.math.abs(it.saturation - item.color.saturation) * 100
            }?.name
        _uiState.update {
            it.copy(
                page = ManageClothesPage.FORM,
                editingItemId = item.id,
                selectedItem = item,
                form = ClothingFormState(
                    name = item.name,
                    category = item.category,
                    pattern = item.pattern,
                    occasion = item.occasion,
                    colorName = colorName,
                    photoUri = item.imageUri
                ),
                originalPhotoUri = item.imageUri,
                stagedPhotoUri = null,
                errorMessage = null,
                statusMessage = "Mengedit ${item.name}."
            )
        }
        announce("Form edit pakaian ${item.name} dibuka.")
    }

    fun onNameChange(name: String) = _uiState.update {
        it.copy(form = it.form.copy(name = name, nameError = null), errorMessage = null)
    }

    fun onCategoryChange(category: com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingCategory) =
        _uiState.update { it.copy(form = it.form.copy(category = category)) }

    fun onPatternChange(pattern: com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingPattern) =
        _uiState.update { it.copy(form = it.form.copy(pattern = pattern)) }

    fun onOccasionChange(occasion: com.zuhdiarifazizi.projectakhirpapb.domain.model.Occasion) =
        _uiState.update { it.copy(form = it.form.copy(occasion = occasion)) }

    fun onColorChange(colorName: String) = _uiState.update {
        it.copy(form = it.form.copy(colorName = colorName, colorError = null), errorMessage = null)
    }

    fun onSearchChange(query: String) = _uiState.update { it.copy(searchQuery = query) }

    fun setCategoryFilter(category: com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingCategory?) =
        _uiState.update { it.copy(categoryFilter = category) }

    fun requestDeleteConfirmation() = _uiState.update { it.copy(showDeleteConfirmation = true) }

    fun cancelDelete() = _uiState.update { it.copy(showDeleteConfirmation = false) }

    fun confirmDelete() {
        val item = _uiState.value.selectedItem ?: return
        viewModelScope.launch {
            runCatching { clothesRepository.deleteClothing(item.id) }
                .onSuccess {
                    photoStorage.deleteStoredPhoto(item.imageUri)
                    hapticManager.performSuccess()
                    _uiState.update {
                        it.copy(page = ManageClothesPage.LIST, selectedItem = null, showDeleteConfirmation = false,
                            statusMessage = "${item.name} dihapus dari koleksi.", errorMessage = null)
                    }
                    ttsManager.speak("${item.name} berhasil dihapus. Rekomendasi akan menggunakan koleksi terbaru.")
                }
                .onFailure { announceError("Pakaian belum dapat dihapus. Silakan coba lagi.") }
        }
    }

    fun selectGalleryPhoto(uri: String?) {
        if (uri.isNullOrBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null, statusMessage = "Memuat foto dari galeri.") }
            runCatching { photoStorage.importPhoto(uri) }
                .onSuccess { savedUri ->
                    deleteStagedPhotoIfReplaced(savedUri)
                    _uiState.update {
                        it.copy(
                            page = ManageClothesPage.FORM,
                            isSaving = false,
                            stagedPhotoUri = savedUri,
                            form = it.form.copy(photoUri = savedUri),
                            statusMessage = "Foto dari galeri siap digunakan."
                        )
                    }
                    hapticManager.performSuccess()
                    ttsManager.speak("Foto pakaian dari galeri berhasil dimuat. Foto siap digunakan pada formulir.")
                }
                .onFailure {
                    _uiState.update { it.copy(isSaving = false) }
                    announceError("Foto galeri tidak dapat dibuka. Silakan pilih foto lain.")
                }
        }
    }

    fun openCamera() {
        _uiState.update { it.copy(page = ManageClothesPage.CAMERA, capturedCameraPhotoUri = null, errorMessage = null) }
    }

    fun onCameraReady() {
        hapticManager.performSuccess()
        announce("Kamera berhasil dibuka. Silakan arahkan kamera ke pakaian. Tekan Ambil Foto untuk mengambil gambar.")
    }

    fun onCameraUnavailable() {
        _uiState.update {
            it.copy(page = ManageClothesPage.FORM, statusMessage = "Kamera tidak tersedia. Anda dapat memilih foto dari galeri.")
        }
        announceError("Kamera tidak tersedia. Anda tetap dapat memilih foto dari galeri.")
    }

    fun onCameraPermissionDenied() {
        _uiState.update {
            it.copy(page = ManageClothesPage.FORM, statusMessage = "Izin kamera ditolak. Galeri tetap dapat digunakan.")
        }
        hapticManager.performError()
        ttsManager.speak("Izin kamera ditolak. Anda tetap dapat memilih foto pakaian dari galeri.")
    }

    fun onCameraCaptureFailed() = announceError("Foto gagal diambil. Periksa kamera atau coba lagi.")

    fun onCameraPhotoCaptured(uri: String) {
        _uiState.update {
            it.copy(
                capturedCameraPhotoUri = uri,
                statusMessage = "Foto berhasil diambil. Periksa foto, lalu pilih Gunakan Foto atau Ambil Ulang."
            )
        }
        hapticManager.performSuccess()
        ttsManager.speak("Foto berhasil diambil. Periksa foto. Pilih Gunakan Foto atau Ambil Ulang.")
    }

    fun useCapturedPhoto() {
        val uri = _uiState.value.capturedCameraPhotoUri ?: return
        deleteStagedPhotoIfReplaced(uri)
        _uiState.update {
            it.copy(
                page = ManageClothesPage.FORM,
                capturedCameraPhotoUri = null,
                stagedPhotoUri = uri,
                form = it.form.copy(photoUri = uri),
                statusMessage = "Foto berhasil disimpan untuk formulir."
            )
        }
        hapticManager.performSuccess()
        ttsManager.speak("Foto berhasil disimpan ke formulir pakaian.")
    }

    fun retakePhoto() {
        val uri = _uiState.value.capturedCameraPhotoUri
        viewModelScope.launch { photoStorage.deleteStoredPhoto(uri) }
        _uiState.update { it.copy(capturedCameraPhotoUri = null, statusMessage = "Silakan arahkan kamera dan ambil ulang foto.") }
        announce("Foto sebelumnya dihapus. Silakan arahkan kamera ke pakaian dan ambil ulang.")
    }

    fun requestVoiceCapture() {
        if (_uiState.value.page != ManageClothesPage.CAMERA) {
            announceError("Buka kamera terlebih dahulu sebelum memberi perintah ambil foto.")
            return
        }
        _uiState.update { it.copy(captureRequestId = it.captureRequestId + 1) }
        ttsManager.speak("Mengambil foto pakaian.")
    }

    fun cancelCamera() {
        val capture = _uiState.value.capturedCameraPhotoUri
        viewModelScope.launch { photoStorage.deleteStoredPhoto(capture) }
        _uiState.update { it.copy(page = ManageClothesPage.FORM, capturedCameraPhotoUri = null) }
        announce("Kembali ke formulir pakaian.")
    }

    fun removeSelectedPhoto() {
        val state = _uiState.value
        val photo = state.form.photoUri
        if (photo != state.originalPhotoUri) viewModelScope.launch { photoStorage.deleteStoredPhoto(photo) }
        _uiState.update { it.copy(form = it.form.copy(photoUri = null), stagedPhotoUri = null) }
        announce("Foto dihapus dari formulir. Anda dapat menambahkan foto lain atau melanjutkan tanpa foto.")
    }

    fun saveForm() {
        val current = _uiState.value
        val name = current.form.name.trim()
        val color = ClothingColorOptions.firstOrNull { it.name == current.form.colorName }
        val nameError = if (name.isBlank()) "Nama pakaian wajib diisi." else null
        val colorError = if (color == null) "Pilih warna pakaian." else null
        if (nameError != null || colorError != null) {
            _uiState.update {
                it.copy(form = it.form.copy(nameError = nameError, colorError = colorError), errorMessage = nameError ?: colorError)
            }
            hapticManager.performError()
            ttsManager.speak(listOfNotNull(nameError, colorError).joinToString(" "))
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            val existing = try {
                current.editingItemId?.let { id -> clothesRepository.getClothingById(id) }
            } catch (_: Exception) {
                _uiState.update { it.copy(isSaving = false) }
                announceError("Data pakaian lama belum dapat dimuat untuk diedit.")
                return@launch
            }
            val item = ClothingItem(
                id = existing?.id ?: UUID.randomUUID().toString(),
                userId = DemoClothingCatalog.USER_ID,
                name = name,
                category = current.form.category,
                pattern = current.form.pattern,
                occasion = current.form.occasion,
                color = HsvColor(color!!.hue, color.saturation, color.value, color.name),
                imageUri = current.form.photoUri,
                createdAtEpochMillis = existing?.createdAtEpochMillis ?: System.currentTimeMillis()
            )
            runCatching { clothesRepository.upsertClothing(item) }
                .onSuccess {
                    if (existing?.imageUri != null && existing.imageUri != item.imageUri) {
                        photoStorage.deleteStoredPhoto(existing.imageUri)
                    }
                    hapticManager.performSuccess()
                    _uiState.update {
                        it.copy(page = ManageClothesPage.LIST, selectedItem = null, editingItemId = null,
                            form = ClothingFormState(), originalPhotoUri = null, stagedPhotoUri = null,
                            isSaving = false, errorMessage = null,
                            statusMessage = "$name berhasil disimpan ke koleksi.")
                    }
                    ttsManager.speak("$name berhasil disimpan. Pakaian ini tersedia untuk rekomendasi outfit.")
                }
                .onFailure {
                    _uiState.update { it.copy(isSaving = false) }
                    announceError("Pakaian belum dapat disimpan. Silakan coba lagi.")
                }
        }
    }

    fun cancelForm() {
        val state = _uiState.value
        val staged = state.stagedPhotoUri
        if (staged != null && staged != state.originalPhotoUri) viewModelScope.launch { photoStorage.deleteStoredPhoto(staged) }
        _uiState.update {
            it.copy(page = if (it.editingItemId == null) ManageClothesPage.LIST else ManageClothesPage.DETAIL,
                form = ClothingFormState(), stagedPhotoUri = null, errorMessage = null, isSaving = false)
        }
        announce("Perubahan formulir dibatalkan.")
    }

    fun returnToList() = _uiState.update {
        it.copy(page = ManageClothesPage.LIST, selectedItem = null, showDeleteConfirmation = false)
    }

    fun navigateBack() {
        when (_uiState.value.page) {
            ManageClothesPage.LIST -> Unit
            ManageClothesPage.FORM -> cancelForm()
            ManageClothesPage.DETAIL -> returnToList()
            ManageClothesPage.CAMERA -> cancelCamera()
        }
    }

    private fun deleteStagedPhotoIfReplaced(newUri: String) {
        val oldStaged = _uiState.value.stagedPhotoUri
        if (oldStaged != null && oldStaged != newUri && oldStaged != _uiState.value.originalPhotoUri) {
            viewModelScope.launch { photoStorage.deleteStoredPhoto(oldStaged) }
        }
    }

    private fun speakItem(item: ClothingItem) {
        ttsManager.speak(
            "${item.name}. Kategori ${item.category.displayName}. Warna ${item.color.colorName}. " +
                "Motif ${item.pattern.displayName}. Aktivitas ${item.occasion.displayName}. " +
                if (item.imageUri.isNullOrBlank()) "Foto belum tersedia." else "Foto tersedia."
        )
        hapticManager.performButtonClick()
    }

    private fun announce(message: String) {
        _uiState.update { it.copy(statusMessage = message, errorMessage = null) }
        ttsManager.speak(message)
    }

    private fun announceError(message: String) {
        _uiState.update { it.copy(statusMessage = message, errorMessage = message) }
        hapticManager.performError()
        ttsManager.speak(message)
    }
}
