package com.zuhdiarifazizi.projectakhirpapb.domain.voice

interface IntentRecognizer {
    fun recognize(spokenText: String): VoiceCommand
}
