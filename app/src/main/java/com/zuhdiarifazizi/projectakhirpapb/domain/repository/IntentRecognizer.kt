package com.zuhdiarifazizi.projectakhirpapb.domain.repository

import com.zuhdiarifazizi.projectakhirpapb.domain.model.VoiceCommand
import com.zuhdiarifazizi.projectakhirpapb.domain.model.VoiceScreenContext

interface IntentRecognizer {
    fun recognize(spokenText: String): VoiceCommand =
        recognize(spokenText, VoiceScreenContext())

    fun recognize(spokenText: String, context: VoiceScreenContext): VoiceCommand =
        recognize(spokenText)
}
