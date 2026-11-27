package com.zuhdiarifazizi.projectakhirpapb.domain.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface TextToSpeechManager {
    fun speak(
        text: String,
        queueMode: Int = QUEUE_FLUSH,
        recordInHistory: Boolean = true
    )

    fun speakCalm(
        text: String,
        queueMode: Int = QUEUE_FLUSH,
        recordInHistory: Boolean = true
    ) {
        speak(text, queueMode = queueMode, recordInHistory = recordInHistory)
    }

    fun speakCue(text: String, onComplete: () -> Unit) {
        stop()
        speak(text, queueMode = QUEUE_FLUSH, recordInHistory = false)
        onComplete()
    }

    fun repeatLastSpeech()
    fun stop()
    fun shutdown()
    val isSpeaking: Boolean
    val lastSpeech: String?

    val isSpeakingFlow: StateFlow<Boolean>
        get() = MutableStateFlow(isSpeaking)

    val currentSpeechFlow: StateFlow<String?>
        get() = MutableStateFlow(lastSpeech)

    companion object {
        const val QUEUE_FLUSH = 0
        const val QUEUE_ADD = 1
    }
}
