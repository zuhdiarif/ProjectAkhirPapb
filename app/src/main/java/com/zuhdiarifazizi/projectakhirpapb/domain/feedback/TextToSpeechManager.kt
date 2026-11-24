package com.zuhdiarifazizi.projectakhirpapb.domain.feedback

import android.speech.tts.TextToSpeech

interface TextToSpeechManager {
    fun speak(text: String, queueMode: Int = TextToSpeech.QUEUE_FLUSH)
    fun repeatLastSpeech()
    fun stop()
    fun shutdown()
    val isSpeaking: Boolean
    val lastSpeech: String?
}
