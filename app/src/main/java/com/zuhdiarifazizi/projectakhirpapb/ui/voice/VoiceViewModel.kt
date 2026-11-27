package com.zuhdiarifazizi.projectakhirpapb.ui.voice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zuhdiarifazizi.projectakhirpapb.data.feedback.AndroidHapticFeedbackManager
import com.zuhdiarifazizi.projectakhirpapb.data.feedback.AndroidTextToSpeechManager
import com.zuhdiarifazizi.projectakhirpapb.data.nfc.AndroidNfcTagReader
import com.zuhdiarifazizi.projectakhirpapb.data.voice.AndroidSpeechRecognizerManager
import com.zuhdiarifazizi.projectakhirpapb.data.voice.IndonesianIntentRecognizer
import com.zuhdiarifazizi.projectakhirpapb.domain.voice.VoiceInteractionManager
import com.zuhdiarifazizi.projectakhirpapb.ui.components.FeedbackMessage
import com.zuhdiarifazizi.projectakhirpapb.ui.navigation.AuraDestination
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VoiceViewModel(
    val ttsManager: AndroidTextToSpeechManager,
    val hapticManager: AndroidHapticFeedbackManager,
    val speechRecognizerManager: AndroidSpeechRecognizerManager,
    val nfcTagReader: AndroidNfcTagReader,
    intentRecognizer: IndonesianIntentRecognizer = IndonesianIntentRecognizer()
) : ViewModel() {

    val voiceInteractionManager: VoiceInteractionManager = VoiceInteractionManager(
        speechRecognizerManager = speechRecognizerManager,
        intentRecognizer = intentRecognizer,
        ttsManager = ttsManager,
        hapticManager = hapticManager
    )

    val state: StateFlow<VoiceInteractionState> = voiceInteractionManager.state

    private val _currentDestination = MutableStateFlow(AuraDestination.AUTH)
    val currentDestination: StateFlow<AuraDestination> = _currentDestination.asStateFlow()

    private val _globalFeedbackBanner = MutableStateFlow<FeedbackMessage?>(null)
    val globalFeedbackBanner: StateFlow<FeedbackMessage?> = _globalFeedbackBanner.asStateFlow()

    init {
        viewModelScope.launch {
            var lastStatus = VoiceSessionStatus.IDLE
            var lastMessage: String? = null
            voiceInteractionManager.state.collect { voiceState ->
                val msg = voiceState.feedbackMessage
                when {
                    (voiceState.status == VoiceSessionStatus.ERROR || voiceState.status == VoiceSessionStatus.SUCCESS) &&
                        !msg.isNullOrBlank() &&
                        (voiceState.status != lastStatus || msg != lastMessage) -> {
                        _globalFeedbackBanner.value = FeedbackMessage(
                            message = msg,
                            isError = voiceState.status == VoiceSessionStatus.ERROR
                        )
                    }
                    voiceState.status == VoiceSessionStatus.IDLE -> {
                        _globalFeedbackBanner.value = null
                    }
                }
                lastStatus = voiceState.status
                lastMessage = msg
            }
        }
    }

    fun navigateTo(destination: AuraDestination) {
        _currentDestination.value = destination
    }

    fun dismissGlobalBanner() {
        hapticManager.performButtonClick()
        _globalFeedbackBanner.value = null
    }

    override fun onCleared() {
        super.onCleared()
        voiceInteractionManager.cleanup()
        ttsManager.shutdown()
    }
}
