package com.zuhdiarifazizi.projectakhirpapb.ui.manageclothes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zuhdiarifazizi.projectakhirpapb.data.classifier.DefaultClothingClassifier
import com.zuhdiarifazizi.projectakhirpapb.data.repository.DemoClothingCatalog
import com.zuhdiarifazizi.projectakhirpapb.data.storage.ClothingPhotoStorage
import com.zuhdiarifazizi.projectakhirpapb.domain.classifier.ClothingClassifier
import com.zuhdiarifazizi.projectakhirpapb.domain.feedback.HapticFeedbackManager
import com.zuhdiarifazizi.projectakhirpapb.domain.feedback.TextToSpeechManager
import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingCategory
import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingItem
import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingPattern
import com.zuhdiarifazizi.projectakhirpapb.domain.model.HsvColor
import com.zuhdiarifazizi.projectakhirpapb.domain.model.Occasion
import com.zuhdiarifazizi.projectakhirpapb.domain.nfc.NfcTagReader
import com.zuhdiarifazizi.projectakhirpapb.domain.repository.ClothesRepository
import com.zuhdiarifazizi.projectakhirpapb.domain.scanner.ScannerFrameGuidance
import com.zuhdiarifazizi.projectakhirpapb.domain.scanner.ScannerHapticCue
import com.zuhdiarifazizi.projectakhirpapb.domain.security.SecurityInputValidator
import com.zuhdiarifazizi.projectakhirpapb.domain.usecase.MatchClothingFromWardrobeUseCase
import java.util.UUID
import kotlin.math.abs
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ManageClothesViewModel(
    private val clothesRepository: ClothesRepository,
    private val photoStorage: ClothingPhotoStorage,
    private val ttsManager: TextToSpeechManager,
    private val hapticManager: HapticFeedbackManager,
    private val classifier: ClothingClassifier = DefaultClothingClassifier(),
    private val nfcTagReader: NfcTagReader? = null,
    private val matchClothingUseCase: MatchClothingFromWardrobeUseCase = MatchClothingFromWardrobeUseCase(),
    private val photoByteReader: suspend (String) -> ByteArray = { ByteArray(0) },
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ManageClothesUiState(
            isNfcSupported = nfcTagReader?.isSupported == true,
            isNfcEnabled = nfcTagReader?.isEnabled?.value == true
        )
    )
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

        nfcTagReader?.let { reader ->
            viewModelScope.launch {
                reader.isEnabled.collect { enabled ->
                    _uiState.update { it.copy(isNfcSupported = reader.isSupported, isNfcEnabled = enabled) }
                }
            }
            viewModelScope.launch {
                reader.tagScanned.collect { uid -> handleScannedRfidUid(uid) }
            }
        }
    }

    fun openAddGuide() {
        clearUncommittedStage()
        _uiState.update {
            it.copy(
                page = ManageClothesPage.ADD_GUIDE,
                selectedItem = null,
                editingItemId = null,
                form = ClothingFormState(),
                originalPhotoUri = null,
                stagedPhotoUri = null,
                isBindingRfidInForm = false,
                errorMessage = null,
                statusMessage = "Panduan 4 langkah tambah baju baru dibuka."
            )
        }
        announce(
            "Tambah baju baru. Letakkan pakaian mendatar, ratakan lipatan, pastikan terlihat penuh di layar, " +
                "dan pegang ponsel sejajar. Tekan Ambil Foto untuk memulai pemindai."
        )
    }

    fun openAddForm() {
        clearUncommittedStage()
        _uiState.update {
            it.copy(
                page = ManageClothesPage.FORM,
                selectedItem = null,
                editingItemId = null,
                form = ClothingFormState(),
                originalPhotoUri = null,
                stagedPhotoUri = null,
                isBindingRfidInForm = false,
                errorMessage = null,
                statusMessage = "Form tambah pakaian dibuka."
            )
        }
        announce("Form tambah pakaian dibuka. Isi nama, kategori, warna, motif, dan aktivitas. Foto dapat diambil atau dipilih dari galeri.")
    }

    fun openScanFind() {
        nfcTagReader?.refreshAvailability()
        _uiState.update {
            it.copy(
                page = ManageClothesPage.SCAN_FIND,
                scannedRfidUid = null,
                matchedScanItem = null,
                matchedVisualScore = null,
                scanStatusMessage = "Tempelkan kartu atau stiker RFID 13,56 MHz ke bagian belakang ponsel, atau gunakan Kamera AI.",
                errorMessage = null
            )
        }
        announce("Mode kenali pakaian aktif. Tempelkan tag RFID ke belakang ponsel atau pilih Kenali lewat Kamera AI.")
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
            ?: nearestColorOptionName(item.color)
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
                    extractedHsv = item.color,
                    material = item.material.ifBlank { "Katun Halus" },
                    rfidTagUid = item.rfidTagUid,
                    photoUri = item.imageUri
                ),
                originalPhotoUri = item.imageUri,
                stagedPhotoUri = null,
                isBindingRfidInForm = false,
                errorMessage = null,
                statusMessage = "Mengedit ${item.name}."
            )
        }
        announce("Form edit pakaian ${item.name} dibuka.")
    }

    fun onNameChange(name: String) = _uiState.update {
        it.copy(form = it.form.copy(name = name, nameError = null), errorMessage = null)
    }

    fun onCategoryChange(category: ClothingCategory) =
        _uiState.update { it.copy(form = it.form.copy(category = category)) }

    fun onPatternChange(pattern: ClothingPattern) =
        _uiState.update { it.copy(form = it.form.copy(pattern = pattern)) }

    fun onOccasionChange(occasion: Occasion) =
        _uiState.update { it.copy(form = it.form.copy(occasion = occasion)) }

    fun onColorChange(colorName: String) {
        val preset = ClothingColorOptions.firstOrNull { it.name == colorName }
        _uiState.update {
            it.copy(
                form = it.form.copy(
                    colorName = colorName,
                    extractedHsv = preset?.let { opt -> HsvColor(opt.hue, opt.saturation, opt.value, opt.name) },
                    colorError = null
                ),
                errorMessage = null
            )
        }
    }

    fun onMaterialChange(material: String) = _uiState.update {
        it.copy(form = it.form.copy(material = material))
    }

    fun startRfidBindingInForm() {
        nfcTagReader?.refreshAvailability()
        _uiState.update {
            it.copy(
                isBindingRfidInForm = true,
                statusMessage = "Tempelkan tag RFID / kartu NFC ke bagian belakang ponsel sekarang."
            )
        }
        announce("Menunggu tag RFID. Tempelkan stiker atau kartu NFC ke bagian belakang ponsel.")
    }

    fun cancelRfidBindingInForm() {
        _uiState.update { it.copy(isBindingRfidInForm = false) }
    }

    fun clearRfidInForm() {
        _uiState.update {
            it.copy(
                form = it.form.copy(rfidTagUid = null),
                isBindingRfidInForm = false,
                statusMessage = "Tag RFID dilepas dari formulir."
            )
        }
    }

    fun simulateRfidTap(uid: String) {
        if (nfcTagReader != null) {
            nfcTagReader.simulateTagScan(uid)
        } else {
            val normalized = SecurityInputValidator.normalizeNfcUidString(uid) ?: return
            handleScannedRfidUid(normalized)
        }
    }

    fun onSearchChange(query: String) = _uiState.update { it.copy(searchQuery = query) }

    fun setCategoryFilter(category: ClothingCategory?) =
        _uiState.update { it.copy(categoryFilter = category) }

    fun requestDeleteConfirmation(item: ClothingItem? = null) = _uiState.update {
        it.copy(
            selectedItem = item ?: it.selectedItem,
            showDeleteConfirmation = true
        )
    }

    fun cancelDelete() = _uiState.update { it.copy(showDeleteConfirmation = false) }

    fun confirmDelete() {
        val item = _uiState.value.selectedItem ?: return
        viewModelScope.launch {
            runCatching { clothesRepository.deleteClothing(item.id) }
                .onSuccess {
                    photoStorage.deleteStoredPhoto(item.imageUri)
                    hapticManager.performSuccess()
                    _uiState.update {
                        it.copy(
                            page = ManageClothesPage.LIST,
                            selectedItem = null,
                            showDeleteConfirmation = false,
                            statusMessage = "${item.name} dihapus dari koleksi.",
                            errorMessage = null
                        )
                    }
                    ttsManager.speak("${item.name} berhasil dihapus. Rekomendasi akan menggunakan koleksi terbaru.")
                }
                .onFailure { announceError("Pakaian belum dapat dihapus. Silakan coba lagi.") }
        }
    }

    fun selectGalleryPhoto(uri: String?) {
        if (uri.isNullOrBlank()) return
        viewModelScope.launch {
            val isFindMode = _uiState.value.scannerHud.isFindMode || _uiState.value.page == ManageClothesPage.SCAN_FIND
            _uiState.update { it.copy(isSaving = true, errorMessage = null, statusMessage = "Memuat foto dari galeri.") }
            runCatching { photoStorage.importPhoto(uri) }
                .onSuccess { savedUri ->
                    if (isFindMode) {
                        _uiState.update { it.copy(isSaving = false) }
                        identifyWardrobeFromPhoto(savedUri)
                    } else {
                        deleteStagedPhotoIfReplaced(savedUri)
                        _uiState.update {
                            it.copy(
                                page = ManageClothesPage.FORM,
                                isSaving = false,
                                stagedPhotoUri = savedUri,
                                form = it.form.copy(photoUri = savedUri),
                                statusMessage = "Foto dari galeri dimuat. Memulai analisis AI."
                            )
                        }
                        hapticManager.performSuccess()
                        analyzePhotoAndPrefillForm(savedUri)
                    }
                }
                .onFailure {
                    _uiState.update { it.copy(isSaving = false) }
                    announceError("Foto galeri tidak dapat dibuka. Silakan pilih foto lain.")
                }
        }
    }

    fun openCamera(findMode: Boolean = false) {
        _uiState.update {
            it.copy(
                page = ManageClothesPage.CAMERA,
                capturedCameraPhotoUri = null,
                scannerHud = ScannerHudState(
                    isFindMode = findMode,
                    isVoiceGuidanceOn = it.scannerHud.isVoiceGuidanceOn
                ),
                errorMessage = null
            )
        }
    }

    fun onCameraReady() {
        hapticManager.performSuccess()
        val msg = if (_uiState.value.scannerHud.isFindMode) {
            "Pemindai kamera siap. Arahkan ke pakaian yang ingin dikenali."
        } else {
            "Pemindai AI aktif. Arahkan kamera sejajar di atas pakaian."
        }
        announce(msg)
    }

    fun onScannerFrameEvaluated(guidance: ScannerFrameGuidance) {
        val current = _uiState.value
        if (current.page != ManageClothesPage.CAMERA || current.capturedCameraPhotoUri != null) return

        _uiState.update {
            it.copy(
                scannerHud = it.scannerHud.copy(
                    lightPercent = guidance.lightPercent,
                    stepLabel = guidance.stepLabel,
                    detectedTitle = guidance.detectedTitle,
                    guidanceText = guidance.guidanceText,
                    readiness = guidance.readiness,
                    isTorchOn = it.scannerHud.isTorchOn || guidance.needsTorch,
                    isLockedForAutoCapture = guidance.isReadyForAutoCapture
                )
            )
        }

        when (guidance.hapticCue) {
            ScannerHapticCue.TICK -> hapticManager.performCategorySelected()
            ScannerHapticCue.WARNING -> hapticManager.performButtonClick()
            ScannerHapticCue.LOCKED_CAPTURE -> hapticManager.performSuccess()
            null -> Unit
        }

        if (current.scannerHud.isVoiceGuidanceOn && guidance.spokenCue != null) {
            ttsManager.speak(guidance.spokenCue)
        }

        if (guidance.shouldTriggerAutoCapture) {
            _uiState.update { it.copy(captureRequestId = it.captureRequestId + 1) }
        }
    }

    fun toggleScannerTorch() {
        val next = !_uiState.value.scannerHud.isTorchOn
        _uiState.update { it.copy(scannerHud = it.scannerHud.copy(isTorchOn = next)) }
        hapticManager.performButtonClick()
        ttsManager.speak(if (next) "Lampu senter dinyalakan." else "Lampu senter dimatikan.")
    }

    fun toggleScannerCameraFacing() {
        val next = !_uiState.value.scannerHud.useFrontCamera
        _uiState.update { it.copy(scannerHud = it.scannerHud.copy(useFrontCamera = next)) }
        hapticManager.performButtonClick()
        ttsManager.speak(if (next) "Beralih ke kamera depan." else "Beralih ke kamera belakang.")
    }

    fun toggleScannerVoiceGuidance() {
        val next = !_uiState.value.scannerHud.isVoiceGuidanceOn
        _uiState.update { it.copy(scannerHud = it.scannerHud.copy(isVoiceGuidanceOn = next)) }
        hapticManager.performButtonClick()
        ttsManager.speak(if (next) "Panduan suara otomatis aktif." else "Panduan suara otomatis dimatikan.")
    }

    fun speakCurrentScannerInstruction() {
        val hud = _uiState.value.scannerHud
        hapticManager.performButtonClick()
        ttsManager.speak("${hud.stepLabel}. ${hud.detectedTitle}. ${hud.guidanceText}")
    }

    fun onCameraUnavailable() {
        val fallbackPage = if (_uiState.value.scannerHud.isFindMode) ManageClothesPage.SCAN_FIND else ManageClothesPage.FORM
        _uiState.update {
            it.copy(
                page = fallbackPage,
                statusMessage = "Kamera tidak tersedia. Anda dapat memilih foto dari galeri."
            )
        }
        announceError("Kamera tidak tersedia. Anda tetap dapat memilih foto dari galeri.")
    }

    fun onCameraPermissionDenied() {
        val fallbackPage = if (_uiState.value.scannerHud.isFindMode) ManageClothesPage.SCAN_FIND else ManageClothesPage.FORM
        _uiState.update {
            it.copy(
                page = fallbackPage,
                statusMessage = "Izin kamera ditolak. Galeri tetap dapat digunakan."
            )
        }
        hapticManager.performError()
        ttsManager.speak("Izin kamera ditolak. Anda tetap dapat memilih foto pakaian dari galeri.")
    }

    fun onCameraCaptureFailed() = announceError("Foto gagal diambil. Periksa kamera atau coba lagi.")

    fun onCameraPhotoCaptured(uri: String) {
        if (_uiState.value.scannerHud.isFindMode) {
            identifyWardrobeFromPhoto(uri)
            return
        }
        _uiState.update {
            it.copy(
                capturedCameraPhotoUri = uri,
                statusMessage = "Foto berhasil diambil. Periksa foto, lalu pilih Gunakan Foto atau Ambil Ulang."
            )
        }
        hapticManager.performSuccess()
        ttsManager.speak("Foto berhasil diambil. Pilih Gunakan Foto untuk dianalisis AI, atau Ambil Ulang.")
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
                statusMessage = "Foto disimpan. AI sedang menganalisis pakaian…"
            )
        }
        hapticManager.performSuccess()
        analyzePhotoAndPrefillForm(uri)
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
        val wasFindMode = _uiState.value.scannerHud.isFindMode
        viewModelScope.launch { photoStorage.deleteStoredPhoto(capture) }
        _uiState.update {
            it.copy(
                page = if (wasFindMode) ManageClothesPage.SCAN_FIND else ManageClothesPage.ADD_GUIDE,
                capturedCameraPhotoUri = null
            )
        }
        announce("Pemindai kamera ditutup.")
    }

    fun removeSelectedPhoto() {
        val state = _uiState.value
        val photo = state.form.photoUri
        if (photo != state.originalPhotoUri) viewModelScope.launch { photoStorage.deleteStoredPhoto(photo) }
        _uiState.update { it.copy(form = it.form.copy(photoUri = null), stagedPhotoUri = null) }
        announce("Foto dihapus dari formulir. Anda dapat menambahkan foto lain atau melanjutkan tanpa foto.")
    }

    fun speakWardrobeOverview() {
        val items = _uiState.value.items
        hapticManager.performButtonClick()
        if (items.isEmpty()) {
            ttsManager.speak("Lemari pakaian Anda masih kosong. Tekan Tambah baju baru untuk mulai mendigitalkan pakaian.")
            return
        }
        val tops = items.count { it.category == ClothingCategory.TOP_WEAR }
        val bottoms = items.count { it.category == ClothingCategory.BOTTOM_WEAR }
        val outers = items.count { it.category == ClothingCategory.OUTER_WEAR }
        ttsManager.speak(
            "Lemari pakaian Anda berisi ${items.size} pakaian. " +
                "$tops atasan, $bottoms bawahan, dan $outers luaran."
        )
    }

    fun speakItemSummary(item: ClothingItem) {
        speakItem(item)
    }

    fun saveForm() {
        val current = _uiState.value
        val name = SecurityInputValidator.sanitizeText(current.form.name, maxLength = 60)
        val material = SecurityInputValidator.sanitizeText(current.form.material, maxLength = 32)
        val selectedOption = ClothingColorOptions.firstOrNull { it.name == current.form.colorName }
        val extractedHsv = current.form.extractedHsv

        val finalHsv: HsvColor? = when {
            extractedHsv != null && (current.form.colorName == null || current.form.colorName == extractedHsv.colorName) -> extractedHsv
            selectedOption != null -> HsvColor(
                selectedOption.hue,
                selectedOption.saturation,
                selectedOption.value,
                selectedOption.name
            )
            extractedHsv != null -> extractedHsv
            else -> null
        }

        val nameError = if (name.isBlank()) "Nama pakaian wajib diisi." else null
        val colorError = if (finalHsv == null) "Pilih warna pakaian." else null
        if (nameError != null || colorError != null) {
            _uiState.update {
                it.copy(
                    form = it.form.copy(nameError = nameError, colorError = colorError),
                    errorMessage = nameError ?: colorError
                )
            }
            hapticManager.performError()
            ttsManager.speak(listOfNotNull(nameError, colorError).joinToString(" "))
            return
        }

        val normalizedRfid = current.form.rfidTagUid?.let { SecurityInputValidator.normalizeNfcUidString(it) }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }

            if (normalizedRfid != null) {
                val conflicting = clothesRepository.getClothingByRfid(normalizedRfid)
                if (conflicting != null && conflicting.id != current.editingItemId) {
                    _uiState.update { it.copy(isSaving = false) }
                    announceError("Tag RFID sudah terdaftar pada ${conflicting.name}. Gunakan tag lain.")
                    return@launch
                }
            }

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
                color = finalHsv!!,
                imageUri = current.form.photoUri,
                createdAtEpochMillis = existing?.createdAtEpochMillis ?: System.currentTimeMillis(),
                material = material,
                rfidTagUid = normalizedRfid,
                lastWornEpochMillis = existing?.lastWornEpochMillis
            )

            runCatching { clothesRepository.upsertClothing(item) }
                .onSuccess {
                    if (existing?.imageUri != null && existing.imageUri != item.imageUri) {
                        photoStorage.deleteStoredPhoto(existing.imageUri)
                    }
                    hapticManager.performSuccess()
                    _uiState.update {
                        it.copy(
                            page = ManageClothesPage.LIST,
                            selectedItem = null,
                            editingItemId = null,
                            form = ClothingFormState(),
                            originalPhotoUri = null,
                            stagedPhotoUri = null,
                            isBindingRfidInForm = false,
                            isSaving = false,
                            errorMessage = null,
                            statusMessage = "$name berhasil disimpan ke koleksi."
                        )
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
        clearUncommittedStage()
        _uiState.update {
            it.copy(
                page = if (it.editingItemId == null) ManageClothesPage.LIST else ManageClothesPage.DETAIL,
                form = ClothingFormState(),
                stagedPhotoUri = null,
                isBindingRfidInForm = false,
                isAnalyzingPhoto = false,
                errorMessage = null,
                isSaving = false
            )
        }
        announce("Perubahan formulir dibatalkan.")
    }

    fun returnToList() = _uiState.update {
        it.copy(page = ManageClothesPage.LIST, selectedItem = null, showDeleteConfirmation = false)
    }

    fun navigateBack() {
        when (_uiState.value.page) {
            ManageClothesPage.LIST -> Unit
            ManageClothesPage.ADD_GUIDE -> returnToList()
            ManageClothesPage.SCAN_FIND -> returnToList()
            ManageClothesPage.FORM -> cancelForm()
            ManageClothesPage.DETAIL -> returnToList()
            ManageClothesPage.CAMERA -> cancelCamera()
        }
    }

    private fun analyzePhotoAndPrefillForm(photoUri: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAnalyzingPhoto = true, statusMessage = "AI menganalisis warna dan jenis pakaian…") }
            val bytes = withContext(ioDispatcher) { runCatching { photoByteReader(photoUri) }.getOrDefault(ByteArray(0)) }
            if (bytes.isEmpty()) {
                _uiState.update {
                    it.copy(isAnalyzingPhoto = false, statusMessage = "Foto siap. Lengkapi detail pakaian.")
                }
                return@launch
            }
            classifier.classify(bytes).fold(
                onSuccess = { result ->
                    val optionName = nearestColorOptionName(result.dominantColor)
                    _uiState.update { current ->
                        val nextName = current.form.name.ifBlank { result.suggestedName }
                        current.copy(
                            isAnalyzingPhoto = false,
                            form = current.form.copy(
                                name = nextName,
                                category = result.category,
                                pattern = result.pattern,
                                occasion = result.occasion,
                                material = result.material,
                                colorName = optionName,
                                extractedHsv = result.dominantColor,
                                nameError = null,
                                colorError = null
                            ),
                            statusMessage = "Terdeteksi: $nextName (${result.dominantColor.colorName}, ${result.material})."
                        )
                    }
                    hapticManager.performSuccess()
                    ttsManager.speak(
                        "Terdeteksi ${result.suggestedName}, kategori ${result.category.displayName}, " +
                            "warna ${result.dominantColor.colorName}, bahan ${result.material}. " +
                            "Silakan periksa lalu tekan Simpan Pakaian."
                    )
                },
                onFailure = {
                    _uiState.update {
                        it.copy(
                            isAnalyzingPhoto = false,
                            statusMessage = "Foto dimuat. Silakan isi atau pilih detail pakaian secara manual."
                        )
                    }
                }
            )
        }
    }

    private fun identifyWardrobeFromPhoto(photoUri: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    page = ManageClothesPage.SCAN_FIND,
                    capturedCameraPhotoUri = null,
                    isAnalyzingPhoto = true,
                    scanStatusMessage = "Mengenali pakaian dari foto…"
                )
            }
            val bytes = withContext(ioDispatcher) { runCatching { photoByteReader(photoUri) }.getOrDefault(ByteArray(0)) }
            photoStorage.deleteStoredPhoto(photoUri)
            if (bytes.isEmpty()) {
                _uiState.update { it.copy(isAnalyzingPhoto = false, scanStatusMessage = "Foto belum dapat dibaca.") }
                announceError("Foto belum dapat dibaca.")
                return@launch
            }
            classifier.classify(bytes).fold(
                onSuccess = { result ->
                    val match = matchClothingUseCase.matchByVisualSimilarity(
                        clothes = _uiState.value.items,
                        detectedColor = result.dominantColor,
                        detectedCategory = result.category
                    )
                    if (match != null) {
                        _uiState.update {
                            it.copy(
                                isAnalyzingPhoto = false,
                                matchedScanItem = match.item,
                                matchedVisualScore = match.similarityScore,
                                scanStatusMessage = "Paling mirip: ${match.item.name} (${(match.similarityScore * 100).toInt()}%)."
                            )
                        }
                        hapticManager.performSuccess()
                        ttsManager.speak(
                            "Pakaian dikenali sebagai ${match.item.name}, warna ${match.item.color.colorName}, " +
                                "untuk ${match.item.occasion.displayName}."
                        )
                    } else {
                        _uiState.update {
                            it.copy(isAnalyzingPhoto = false, scanStatusMessage = "Koleksi masih kosong.")
                        }
                        announceError("Belum ada pakaian tersimpan untuk dicocokkan.")
                    }
                },
                onFailure = {
                    _uiState.update { it.copy(isAnalyzingPhoto = false) }
                    announceError("Gagal menganalisis foto untuk pengenalan pakaian.")
                }
            )
        }
    }

    private fun handleScannedRfidUid(rawUid: String) {
        val uid = SecurityInputValidator.normalizeNfcUidString(rawUid) ?: return
        val current = _uiState.value
        if (current.page == ManageClothesPage.FORM && current.isBindingRfidInForm) {
            _uiState.update {
                it.copy(
                    isBindingRfidInForm = false,
                    form = it.form.copy(rfidTagUid = uid),
                    statusMessage = "Tag RFID $uid berhasil ditautkan ke formulir."
                )
            }
            hapticManager.performSuccess()
            ttsManager.speak("Tag RFID berhasil ditautkan ke pakaian ini.")
            return
        }

        if (current.page == ManageClothesPage.SCAN_FIND || current.page == ManageClothesPage.LIST) {
            viewModelScope.launch {
                val repoMatch = clothesRepository.getClothingByRfid(uid)
                val match = repoMatch ?: matchClothingUseCase.matchByRfid(current.items, uid)
                if (match != null) {
                    _uiState.update {
                        it.copy(
                            page = ManageClothesPage.SCAN_FIND,
                            scannedRfidUid = uid,
                            matchedScanItem = match,
                            matchedVisualScore = 1.0,
                            scanStatusMessage = "Tag RFID cocok: ${match.name}."
                        )
                    }
                    hapticManager.performSuccess()
                    ttsManager.speak(
                        "Ditemukan ${match.name}, ${match.category.displayName}, " +
                            "warna ${match.color.colorName}, untuk ${match.occasion.displayName}."
                    )
                } else {
                    _uiState.update {
                        it.copy(
                            page = ManageClothesPage.SCAN_FIND,
                            scannedRfidUid = uid,
                            matchedScanItem = null,
                            matchedVisualScore = null,
                            scanStatusMessage = "Tag RFID $uid belum terdaftar pada pakaian mana pun."
                        )
                    }
                    hapticManager.performError()
                    ttsManager.speak("Tag RFID belum terdaftar. Buka Tambah atau Edit pakaian untuk menautkan tag ini.")
                }
            }
        }
    }

    private fun nearestColorOptionName(hsv: HsvColor): String {
        val exact = ClothingColorOptions.firstOrNull { it.name.equals(hsv.colorName, ignoreCase = true) }
        if (exact != null) return exact.name
        return ClothingColorOptions.minByOrNull { option ->
            val rawDiff = abs(option.hue - hsv.hue)
            val hueDiff = minOf(rawDiff, 360f - rawDiff)
            hueDiff + abs(option.saturation - hsv.saturation) * 100f + abs(option.value - hsv.value) * 100f
        }?.name ?: "Abu-abu"
    }

    private fun clearUncommittedStage() {
        val staged = _uiState.value.stagedPhotoUri
        if (staged != null && staged != _uiState.value.originalPhotoUri) {
            viewModelScope.launch { photoStorage.deleteStoredPhoto(staged) }
        }
    }

    private fun deleteStagedPhotoIfReplaced(newUri: String) {
        val oldStaged = _uiState.value.stagedPhotoUri
        if (oldStaged != null && oldStaged != newUri && oldStaged != _uiState.value.originalPhotoUri) {
            viewModelScope.launch { photoStorage.deleteStoredPhoto(oldStaged) }
        }
    }

    private fun speakItem(item: ClothingItem) {
        val materialText = if (item.material.isNotBlank()) "Bahan ${item.material}. " else ""
        val rfidText = if (!item.rfidTagUid.isNullOrBlank()) "Terhubung ke tag RFID. " else ""
        ttsManager.speak(
            "${item.name}. Kategori ${item.category.displayName}. Warna ${item.color.colorName}. " +
                "${materialText}Motif ${item.pattern.displayName}. Aktivitas ${item.occasion.displayName}. $rfidText" +
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
