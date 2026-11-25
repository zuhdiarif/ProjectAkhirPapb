package com.zuhdiarifazizi.projectakhirpapb.ui.home

import androidx.lifecycle.ViewModel
import com.zuhdiarifazizi.projectakhirpapb.domain.feedback.HapticFeedbackManager
import com.zuhdiarifazizi.projectakhirpapb.domain.feedback.TextToSpeechManager
import com.zuhdiarifazizi.projectakhirpapb.domain.model.ActivityCategory
import com.zuhdiarifazizi.projectakhirpapb.domain.model.Occasion
import com.zuhdiarifazizi.projectakhirpapb.domain.repository.ActivityCategoryRepository
import com.zuhdiarifazizi.projectakhirpapb.ui.components.FeedbackMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class HomeViewModel(
    private val activityCategoryRepository: ActivityCategoryRepository,
    private val ttsManager: TextToSpeechManager? = null,
    private val hapticManager: HapticFeedbackManager? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
    }

    private fun loadCategories() {
        val list = activityCategoryRepository.getCategories()
        _uiState.update { it.copy(categories = list) }
    }

    fun playWelcomeGuidance(force: Boolean = false) {
        if (!force && _uiState.value.hasPlayedWelcome) return

        val welcomeText = "Selamat datang di Aura. Anda berada di Beranda. Silakan pilih tujuan pakaian: Kerja, Nongkrong, Olahraga, Streetwear, atau Lainnya. Anda juga dapat menggunakan tombol perintah suara untuk berbicara."
        ttsManager?.speak(welcomeText)
        _uiState.update { it.copy(hasPlayedWelcome = true) }
    }

    fun repeatGuidance() {
        hapticManager?.performButtonClick()
        playWelcomeGuidance(force = true)
    }

    fun selectCategory(category: ActivityCategory, onNavigate: (() -> Unit)? = null) {
        hapticManager?.performCategorySelected()
        _uiState.update { it.copy(selectedCategory = category) }
        ttsManager?.speak("Tujuan ${category.title} dipilih. Menyiapkan rekomendasi pakaian.")
        onNavigate?.invoke()
    }

    fun selectCategoryByOccasion(occasion: Occasion): ActivityCategory? {
        val category = activityCategoryRepository.getCategoryByOccasion(occasion)
        if (category != null) {
            selectCategory(category)
        }
        return category
    }

    fun onVoiceActionClicked(hasRecordAudioPermission: Boolean, onStartListening: () -> Unit) {
        hapticManager?.performButtonClick()

        if (!hasRecordAudioPermission) {
            hapticManager?.performError()
            val errorMessage = "Izin mikrofon diperlukan untuk menggunakan perintah suara."
            _uiState.update {
                it.copy(
                    feedbackBanner = FeedbackMessage(
                        message = errorMessage,
                        isError = true
                    )
                )
            }
            ttsManager?.speak("Maaf, izin mikrofon belum diberikan. Silakan izinkan akses mikrofon untuk menggunakan perintah suara.")
            return
        }

        val willListen = !_uiState.value.isVoiceListening
        _uiState.update { it.copy(isVoiceListening = willListen) }

        if (willListen) {
            hapticManager?.performSuccess()
            ttsManager?.speak("Mendengarkan. Silakan katakan tujuan Anda.")
            onStartListening()
        } else {
            ttsManager?.speak("Perintah suara dibatalkan.")
        }
    }

    fun onVoiceError(errorMessage: String) {
        hapticManager?.performError()
        _uiState.update {
            it.copy(
                isVoiceListening = false,
                feedbackBanner = FeedbackMessage(message = errorMessage, isError = true)
            )
        }
        ttsManager?.speak(errorMessage)
    }

    fun dismissFeedbackBanner() {
        hapticManager?.performButtonClick()
        _uiState.update { it.copy(feedbackBanner = null) }
    }

    fun setVoiceListening(isListening: Boolean) {
        _uiState.update { it.copy(isVoiceListening = isListening) }
    }

    fun setFeedbackBanner(message: String, isError: Boolean = false) {
        if (isError) {
            hapticManager?.performError()
        } else {
            hapticManager?.performSuccess()
        }
        _uiState.update {
            it.copy(feedbackBanner = FeedbackMessage(message, isError))
        }
        ttsManager?.speak(message)
    }

    override fun onCleared() {
        super.onCleared()
        // Note: Lifecycle cleanup for TTS when ViewModel is destroyed
        ttsManager?.stop()
    }
}
