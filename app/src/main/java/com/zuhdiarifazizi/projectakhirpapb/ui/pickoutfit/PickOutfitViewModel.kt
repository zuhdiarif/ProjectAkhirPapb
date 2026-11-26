package com.zuhdiarifazizi.projectakhirpapb.ui.pickoutfit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zuhdiarifazizi.projectakhirpapb.data.repository.DemoClothingCatalog
import com.zuhdiarifazizi.projectakhirpapb.domain.feedback.HapticFeedbackManager
import com.zuhdiarifazizi.projectakhirpapb.domain.feedback.TextToSpeechManager
import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingCategory
import com.zuhdiarifazizi.projectakhirpapb.domain.model.Occasion
import com.zuhdiarifazizi.projectakhirpapb.domain.model.OutfitCandidate
import com.zuhdiarifazizi.projectakhirpapb.domain.repository.ClothesRepository
import com.zuhdiarifazizi.projectakhirpapb.domain.usecase.CalculateOutfitScoreUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PickOutfitViewModel(
    private val clothesRepository: ClothesRepository,
    private val ttsManager: TextToSpeechManager? = null,
    private val hapticManager: HapticFeedbackManager? = null,
    private val calculateOutfitScoreUseCase: CalculateOutfitScoreUseCase = CalculateOutfitScoreUseCase()
) : ViewModel() {

    private val _uiState = MutableStateFlow(PickOutfitUiState())
    val uiState: StateFlow<PickOutfitUiState> = _uiState.asStateFlow()

    fun beginSelection(occasion: Occasion) {
        _uiState.update {
            it.copy(
                step = PickOutfitStep.CATEGORY_SELECTION,
                selectedOccasion = occasion,
                selectedCategories = REQUIRED_CATEGORIES,
                candidates = emptyList(),
                selectedCandidate = null,
                currentCandidateIndex = 0,
                isLoading = true,
                errorMessage = null,
                statusMessage = null
            )
        }
        speak("Kategori aktivitas ${occasion.displayName}. Pilih atasan dan bawahan. Pakaian luar opsional. Gunakan kotak centang untuk memilih kategori.")
        viewModelScope.launch {
            runCatching { clothesRepository.getClothesByUser(DemoClothingCatalog.USER_ID) }
                .onSuccess { clothes ->
                    _uiState.update { it.copy(availableClothes = clothes, isLoading = false) }
                    if (clothes.isEmpty()) {
                        announceError("Belum ada pakaian yang tersedia di koleksi.")
                    }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = "Koleksi pakaian belum dapat dimuat.")
                    }
                    speak("Koleksi pakaian belum dapat dimuat. Silakan coba lagi.")
                    hapticManager?.performError()
                }
        }
    }

    fun setCategorySelected(category: ClothingCategory, selected: Boolean) {
        _uiState.update { current ->
            val next = current.selectedCategories.toMutableSet().apply {
                if (selected) add(category) else remove(category)
            }
            current.copy(selectedCategories = next, errorMessage = null, statusMessage = null)
        }
        hapticManager?.performCategorySelected()
    }

    fun generateRecommendations(): Boolean {
        val current = _uiState.value
        val missingRequired = REQUIRED_CATEGORIES - current.selectedCategories
        if (missingRequired.isNotEmpty()) {
            val missingNames = missingRequired.joinToString(" dan ") { it.displayName }
            announceError("Pilih $missingNames terlebih dahulu. Pakaian luar bersifat opsional.")
            return false
        }
        val occasion = current.selectedOccasion
        if (occasion == null) {
            announceError("Kategori aktivitas belum dipilih. Kembali dan pilih tujuan terlebih dahulu.")
            return false
        }
        if (current.isLoading) {
            announceError("Koleksi pakaian sedang dimuat. Silakan tunggu sebentar.")
            return false
        }

        val candidates = calculateOutfitScoreUseCase(
            clothes = current.availableClothes,
            targetOccasion = occasion,
            selectedCategories = current.selectedCategories
        ).take(MAX_RECOMMENDATIONS)

        if (candidates.isEmpty()) {
            announceError("Belum ada kombinasi. Pastikan koleksi memiliki atasan dan bawahan.")
            return false
        }

        _uiState.update {
            it.copy(
                step = PickOutfitStep.RECOMMENDATIONS,
                candidates = candidates,
                currentCandidateIndex = 0,
                selectedCandidate = null,
                errorMessage = null,
                statusMessage = "Ada ${candidates.size} rekomendasi berdasarkan pakaian yang tersedia."
            )
        }
        hapticManager?.performSuccess()
        speak("Ada ${candidates.size} rekomendasi untuk ${occasion.displayName}. " + description(candidates.first(), 0, candidates.size))
        return true
    }

    fun nextRecommendation() {
        val current = _uiState.value
        if (current.candidates.isEmpty()) {
            announceError("Belum ada rekomendasi untuk ditampilkan.")
            return
        }
        val nextIndex = (current.currentCandidateIndex + 1) % current.candidates.size
        _uiState.update {
            it.copy(
                currentCandidateIndex = nextIndex,
                statusMessage = "Rekomendasi ${nextIndex + 1} dari ${current.candidates.size}."
            )
        }
        hapticManager?.performButtonClick()
        speak(description(current.candidates[nextIndex], nextIndex, current.candidates.size))
    }

    fun speakRecommendation(index: Int = _uiState.value.currentCandidateIndex) {
        val current = _uiState.value
        val candidate = current.candidates.getOrNull(index)
        if (candidate == null) {
            announceError("Belum ada rekomendasi untuk dibacakan.")
            return
        }
        _uiState.update {
            it.copy(
                statusMessage = "Membacakan rekomendasi ${index + 1} dari ${current.candidates.size}.",
                errorMessage = null
            )
        }
        hapticManager?.performButtonClick()
        speak(description(candidate, index, current.candidates.size))
    }

    fun viewRecommendationDetails(index: Int) {
        showResult(index, selectedByUser = false)
    }

    fun chooseRecommendation(index: Int): Boolean = showResult(index, selectedByUser = true)

    fun selectFirstRecommendationByVoice(): Boolean {
        if (_uiState.value.candidates.isEmpty()) {
            announceError("Belum ada rekomendasi. Pilih kategori pakaian dan minta rekomendasi terlebih dahulu.")
            return false
        }
        return chooseRecommendation(0)
    }

    fun speakCurrentRecommendationByVoice() {
        speakRecommendation()
    }

    fun speakSelectedResult() {
        val current = _uiState.value
        val candidate = current.selectedCandidate
        if (candidate == null) {
            announceError("Belum ada pakaian yang dipilih.")
            return
        }
        hapticManager?.performButtonClick()
        speak(description(candidate, current.currentCandidateIndex, current.candidates.size) + STORAGE_NOTE)
    }

    fun goToPreviousStep(): Boolean {
        val current = _uiState.value
        return when (current.step) {
            PickOutfitStep.CATEGORY_SELECTION -> false
            PickOutfitStep.RECOMMENDATIONS -> {
                _uiState.update { it.copy(step = PickOutfitStep.CATEGORY_SELECTION, errorMessage = null) }
                speak("Kembali ke pemilihan kategori pakaian.")
                true
            }
            PickOutfitStep.RESULT -> {
                _uiState.update { it.copy(step = PickOutfitStep.RECOMMENDATIONS, errorMessage = null) }
                speak("Kembali ke daftar rekomendasi.")
                true
            }
        }
    }

    private fun showResult(index: Int, selectedByUser: Boolean): Boolean {
        val current = _uiState.value
        val candidate = current.candidates.getOrNull(index)
        if (candidate == null) {
            announceError("Rekomendasi tidak tersedia.")
            return false
        }
        _uiState.update {
            it.copy(
                step = PickOutfitStep.RESULT,
                selectedCandidate = candidate,
                currentCandidateIndex = index,
                statusMessage = if (selectedByUser) "Pakaian dipilih untuk sesi ini." else "Detail rekomendasi ${index + 1}."
            )
        }
        hapticManager?.performSuccess()
        speak(
            (if (selectedByUser) "Pakaian dipilih. " else "Detail rekomendasi. ") +
                description(candidate, index, current.candidates.size) + STORAGE_NOTE
        )
        return true
    }

    private fun description(candidate: OutfitCandidate, index: Int, total: Int): String {
        val top = candidate.topWear
        val bottom = candidate.bottomWear
        val outer = candidate.outerWear
        val occasion = candidate.occasion.displayName
        val activityReason = when {
            candidate.occasionScore >= 0.8 -> "Sebagian besar pakaian sesuai untuk aktivitas $occasion."
            candidate.occasion == Occasion.EXERCISE -> "Koleksi demo belum berisi pakaian olahraga khusus; ini pilihan terdekat dari pakaian yang tersedia."
            else -> "Kecocokan aktivitas dihitung dari kategori acara setiap pakaian."
        }
        val outerDescription = outer?.let {
            "Pakaian luar: ${it.name}, warna ${it.color.colorName}."
        } ?: "Tanpa pakaian luar, karena pakaian luar bersifat opsional."
        return "Rekomendasi ${index + 1} dari $total untuk $occasion. " +
            "Atasan: ${top?.name ?: "tidak tersedia"}, warna ${top?.color?.colorName ?: "tidak diketahui"}. " +
            "Bawahan: ${bottom?.name ?: "tidak tersedia"}, warna ${bottom?.color?.colorName ?: "tidak diketahui"}. " +
            "$outerDescription " +
            "Alasan: $activityReason ${candidate.harmonyType}, nilai kecocokan warna ${candidate.colorScorePercent} persen."
    }

    private fun announceError(message: String) {
        _uiState.update { it.copy(errorMessage = message, statusMessage = message) }
        hapticManager?.performError()
        speak(message)
    }

    private fun speak(text: String) {
        ttsManager?.speak(text)
    }

    private val OutfitCandidate.colorScorePercent: Int
        get() = (colorScore * 100).toInt()

    private companion object {
        val REQUIRED_CATEGORIES = setOf(ClothingCategory.TOP_WEAR, ClothingCategory.BOTTOM_WEAR)
        const val MAX_RECOMMENDATIONS = 8
        const val STORAGE_NOTE = " Pilihan ini hanya tersimpan sementara selama sesi aplikasi dan belum disimpan ke riwayat."
    }
}
