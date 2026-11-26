package com.zuhdiarifazizi.projectakhirpapb.domain.feedback

import android.speech.tts.TextToSpeech

interface TextToSpeechManager {
    fun speak(
        text: String,
        queueMode: Int = TextToSpeech.QUEUE_FLUSH,
        recordInHistory: Boolean = true
    )

    fun speakCue(text: String, onComplete: () -> Unit) {
        stop()
        speak(text, queueMode = TextToSpeech.QUEUE_FLUSH, recordInHistory = false)
        onComplete()
    }

    fun repeatLastSpeech()
    fun stop()
    fun shutdown()
    val isSpeaking: Boolean
    val lastSpeech: String?
}
