package com.zuhdiarifazizi.projectakhirpapb.domain.repository

import com.zuhdiarifazizi.projectakhirpapb.domain.model.VoiceRecognitionError

interface SpeechRecognizerManager {
    fun startListening(
        onResult: (String) -> Unit,
        onError: (VoiceRecognitionError) -> Unit,
        onPartialResult: ((String) -> Unit)? = null,
        onListeningStateChanged: ((Boolean) -> Unit)? = null
    )
    fun stopListening()
    fun destroy()
    val isListening: Boolean
}
