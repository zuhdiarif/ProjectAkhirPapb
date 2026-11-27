package com.zuhdiarifazizi.projectakhirpapb.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zuhdiarifazizi.projectakhirpapb.domain.model.ActivityCategory
import com.zuhdiarifazizi.projectakhirpapb.domain.model.Occasion
import com.zuhdiarifazizi.projectakhirpapb.domain.repository.ActivityCategoryRepository
import com.zuhdiarifazizi.projectakhirpapb.domain.repository.HapticFeedbackManager
import com.zuhdiarifazizi.projectakhirpapb.domain.repository.TextToSpeechManager
import com.zuhdiarifazizi.projectakhirpapb.ui.components.FeedbackMessage
import com.zuhdiarifazizi.projectakhirpapb.ui.voice.VoiceInteractionManager
import com.zuhdiarifazizi.projectakhirpapb.ui.voice.VoiceNavigationCallback
import com.zuhdiarifazizi.projectakhirpapb.ui.voice.VoiceSessionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val activityCategoryRepository: ActivityCategoryRepository,
    private val ttsManager: TextToSpeechManager? = null,
    private val hapticManager: HapticFeedbackManager? = null,
    private val voiceInteractionManager: VoiceInteractionManager? = null,
    private val externalScope: kotlinx.coroutines.CoroutineScope? = null
) : ViewModel() {

    private val scope: kotlinx.coroutines.CoroutineScope
        get() = externalScope ?: viewModelScope

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
        observeVoiceSession()
    }

    private fun loadCategories() {
        val list = activityCategoryRepository.getCategories()
        _uiState.update { it.copy(categories = list) }
    }

    private fun observeVoiceSession() {
        if (voiceInteractionManager == null) return
        scope.launch {
            voiceInteractionManager.state.collect { voiceState ->
                _uiState.update { current ->
                    current.copy(
                        isVoiceListening = voiceState.isListening,
                        feedbackBanner = when {
                            voiceState.status == VoiceSessionStatus.ERROR && voiceState.feedbackMessage != null -> {
                                FeedbackMessage(voiceState.feedbackMessage, isError = true)
                            }
                            voiceState.status == VoiceSessionStatus.SUCCESS && voiceState.feedbackMessage != null -> {
                                FeedbackMessage(voiceState.feedbackMessage, isError = false)
                            }
                            else -> current.feedbackBanner
                        }
                    )
                }
            }
        }
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

    fun selectCategory(
        category: ActivityCategory,
        announceTts: Boolean = true,
        onNavigate: (() -> Unit)? = null
    ) {
        hapticManager?.performCategorySelected()
        _uiState.update { it.copy(selectedCategory = category) }
        if (announceTts) {
            ttsManager?.speak("Tujuan ${category.title} dipilih. Menyiapkan rekomendasi pakaian.")
        }
        onNavigate?.invoke()
    }

    fun selectCategoryByOccasion(
        occasion: Occasion,
        announceTts: Boolean = true
    ): ActivityCategory? {
        val category = activityCategoryRepository.getCategoryByOccasion(occasion)
        if (category != null) {
            selectCategory(category, announceTts = announceTts)
        }
        return category
    }

    fun onVoiceActionClicked(
        hasRecordAudioPermission: Boolean,
        navigationCallback: VoiceNavigationCallback? = null
    ) {
        if (voiceInteractionManager != null) {
            voiceInteractionManager.toggleVoiceInteraction(hasRecordAudioPermission, navigationCallback)
            return
        }

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
        } else {
            ttsManager?.speak("Perintah suara dibatalkan.")
        }
    }

    fun onVoiceActionClicked(
        hasRecordAudioPermission: Boolean,
        onStartListening: () -> Unit
    ) {
        onVoiceActionClicked(hasRecordAudioPermission, navigationCallback = null)
        if (hasRecordAudioPermission && _uiState.value.isVoiceListening) {
            onStartListening()
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
    }
}
