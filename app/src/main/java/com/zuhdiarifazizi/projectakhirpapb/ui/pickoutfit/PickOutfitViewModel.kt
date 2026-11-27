package com.zuhdiarifazizi.projectakhirpapb.ui.pickoutfit

import android.speech.tts.TextToSpeech
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zuhdiarifazizi.projectakhirpapb.data.repository.DemoClothingCatalog
import com.zuhdiarifazizi.projectakhirpapb.domain.feedback.HapticFeedbackManager
import com.zuhdiarifazizi.projectakhirpapb.domain.feedback.TextToSpeechManager
import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingCategory
import com.zuhdiarifazizi.projectakhirpapb.domain.model.Occasion
import com.zuhdiarifazizi.projectakhirpapb.domain.model.OutfitCandidate
import com.zuhdiarifazizi.projectakhirpapb.domain.model.OutfitHistory
import com.zuhdiarifazizi.projectakhirpapb.domain.repository.ClothesRepository
import com.zuhdiarifazizi.projectakhirpapb.domain.repository.OutfitRepository
import com.zuhdiarifazizi.projectakhirpapb.domain.usecase.AnalyzeOutfitRotationUseCase
import com.zuhdiarifazizi.projectakhirpapb.domain.usecase.CalculateOutfitScoreUseCase
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PickOutfitViewModel(
    private val clothesRepository: ClothesRepository,
    private val ttsManager: TextToSpeechManager? = null,
    private val hapticManager: HapticFeedbackManager? = null,
    private val calculateOutfitScoreUseCase: CalculateOutfitScoreUseCase = CalculateOutfitScoreUseCase(),
    private val outfitRepository: OutfitRepository? = null,
    private val analyzeOutfitRotationUseCase: AnalyzeOutfitRotationUseCase = AnalyzeOutfitRotationUseCase(),
    private val userIdProvider: () -> String = { DemoClothingCatalog.USER_ID }
) : ViewModel() {

    private val _uiState = MutableStateFlow(PickOutfitUiState())
    val uiState: StateFlow<PickOutfitUiState> = _uiState.asStateFlow()

    fun beginSelection(occasion: Occasion, queueFollowUpSpeech: Boolean = false) {
        val activeUserId = userIdProvider().ifBlank { DemoClothingCatalog.USER_ID }
        _uiState.update {
            it.copy(
                step = PickOutfitStep.CATEGORY_SELECTION,
                selectedOccasion = occasion,
                selectedCategories = REQUIRED_CATEGORIES,
                candidates = emptyList(),
                selectedCandidate = null,
                isSavedToHistory = false,
                currentCandidateIndex = 0,
                isLoading = true,
                errorMessage = null,
                statusMessage = null
            )
        }
        speak(
            text = "Kategori aktivitas ${occasion.displayName}. Pilih atasan dan bawahan. Pakaian luar opsional. Gunakan kotak centang untuk memilih kategori.",
            queueMode = if (queueFollowUpSpeech) TextToSpeech.QUEUE_ADD else TextToSpeech.QUEUE_FLUSH
        )
        viewModelScope.launch {
            runCatching {
                val now = System.currentTimeMillis()
                val clothes = clothesRepository.getClothesByUser(activeUserId)
                val history = outfitRepository
                    ?.getHistorySince(activeUserId, now - THIRTY_DAYS_MILLIS)
                    .orEmpty()
                val recentSignatures = analyzeOutfitRotationUseCase(
                    clothes = clothes,
                    historyEntries = history,
                    nowEpochMillis = now
                ).recentlyWornOutfitSignatures
                clothes to recentSignatures
            }
                .onSuccess { (clothes, recentSignatures) ->
                    _uiState.update {
                        it.copy(
                            availableClothes = clothes,
                            recentlyWornSignatures = recentSignatures,
                            isLoading = false
                        )
                    }
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
            selectedCategories = current.selectedCategories,
            recentlyWornSignatures = current.recentlyWornSignatures
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
                isSavedToHistory = false,
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

    fun confirmSelectedCandidate(): Boolean {
        val current = _uiState.value
        if (current.selectedCandidate == null) {
            announceError("Belum ada pakaian yang dipilih.")
            return false
        }
        return showResult(current.currentCandidateIndex, selectedByUser = true)
    }

    fun selectFirstRecommendationByVoice(): Boolean {
        if (_uiState.value.candidates.isEmpty()) {
            announceError("Belum ada rekomendasi. Pilih kategori pakaian dan minta rekomendasi terlebih dahulu.")
            return false
        }
        return chooseRecommendation(0)
    }

    fun selectCurrentRecommendationByVoice(): Boolean {
        val current = _uiState.value
        if (current.candidates.isEmpty()) {
            announceError("Belum ada rekomendasi. Pilih kategori pakaian dan minta rekomendasi terlebih dahulu.")
            return false
        }
        return chooseRecommendation(current.currentCandidateIndex)
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
        val suffix = if (current.isSavedToHistory) SAVED_NOTE else PREVIEW_NOTE
        speak(description(candidate, current.currentCandidateIndex, current.candidates.size) + suffix)
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
                isSavedToHistory = selectedByUser,
                statusMessage = if (selectedByUser) {
                    "Pakaian dipilih dan disimpan ke Riwayat."
                } else {
                    "Detail rekomendasi ${index + 1}."
                }
            )
        }
        if (selectedByUser) {
            persistChosenOutfit(candidate)
        }
        hapticManager?.performSuccess()
        val note = if (selectedByUser) SAVED_NOTE else PREVIEW_NOTE
        speak(
            (if (selectedByUser) "Pakaian dipilih. " else "Detail rekomendasi. ") +
                description(candidate, index, current.candidates.size) + note
        )
        return true
    }

    private fun persistChosenOutfit(candidate: OutfitCandidate) {
        val activeUserId = userIdProvider().ifBlank { DemoClothingCatalog.USER_ID }
        val wornAt = System.currentTimeMillis()
        val wornItems = listOfNotNull(candidate.topWear, candidate.bottomWear, candidate.outerWear)
        val entry = OutfitHistory(
            id = "hist_${UUID.randomUUID()}",
            userId = activeUserId,
            outfitId = candidate.id,
            items = wornItems.map { it.copy(lastWornEpochMillis = wornAt) },
            occasion = candidate.occasion,
            score = candidate.totalScore,
            wornAtEpochMillis = wornAt
        )
        _uiState.update { state ->
            val wornIds = wornItems.map { it.id }.toSet()
            state.copy(
                recentlyWornSignatures = state.recentlyWornSignatures + candidate.id,
                availableClothes = state.availableClothes.map { item ->
                    if (item.id in wornIds) item.copy(lastWornEpochMillis = wornAt) else item
                }
            )
        }
        viewModelScope.launch {
            runCatching {
                outfitRepository?.saveOutfitHistory(entry)
                wornItems.forEach { item ->
                    clothesRepository.upsertClothing(item.copy(lastWornEpochMillis = wornAt))
                }
            }
        }
    }

    private fun description(candidate: OutfitCandidate, index: Int, total: Int): String {
        val top = candidate.topWear
        val bottom = candidate.bottomWear
        val outer = candidate.outerWear
        val occasion = candidate.occasion.displayName
        val activityReason = when {
            candidate.occasionScore >= 0.8 -> "Sebagian besar pakaian sesuai untuk aktivitas $occasion."
            candidate.occasion == Occasion.EXERCISE -> "Ini pilihan alternatif terdekat karena pakaian olahraga khusus belum mencukupi."
            else -> "Kecocokan aktivitas dihitung dari kategori acara setiap pakaian."
        }
        val rotationNote = if (candidate.historyPenalty > 0.0) {
            " Kombinasi ini baru dipakai dalam tujuh hari terakhir sehingga mendapat penyesuaian skor rotasi."
        } else {
            ""
        }
        val outerDescription = outer?.let {
            "Pakaian luar: ${it.name}, warna ${it.color.colorName}."
        } ?: "Tanpa pakaian luar, karena pakaian luar bersifat opsional."
        return "Rekomendasi ${index + 1} dari $total untuk $occasion. " +
            "Atasan: ${top?.name ?: "tidak tersedia"}, warna ${top?.color?.colorName ?: "tidak diketahui"}. " +
            "Bawahan: ${bottom?.name ?: "tidak tersedia"}, warna ${bottom?.color?.colorName ?: "tidak diketahui"}. " +
            "$outerDescription " +
            "Alasan: $activityReason ${candidate.harmonyType}, nilai kecocokan warna ${candidate.colorScorePercent} persen.$rotationNote"
    }

    private fun announceError(message: String) {
        _uiState.update { it.copy(errorMessage = message, statusMessage = message) }
        hapticManager?.performError()
        speak(message)
    }

    private fun speak(text: String, queueMode: Int = TextToSpeech.QUEUE_FLUSH) {
        ttsManager?.speak(text, queueMode = queueMode)
    }

    private val OutfitCandidate.colorScorePercent: Int
        get() = (colorScore * 100).toInt()

    private companion object {
        val REQUIRED_CATEGORIES = setOf(ClothingCategory.TOP_WEAR, ClothingCategory.BOTTOM_WEAR)
        const val MAX_RECOMMENDATIONS = 8
        const val THIRTY_DAYS_MILLIS = 30L * 24L * 60L * 60L * 1000L
        const val SAVED_NOTE = " Pilihan ini sudah disimpan ke Riwayat & Rotasi Pakaian."
        const val PREVIEW_NOTE = " Mode pratinjau detail. Tekan Pakai Pakaian Ini untuk menyimpan ke Riwayat."
    }
}
