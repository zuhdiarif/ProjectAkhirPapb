package com.zuhdiarifazizi.projectakhirpapb.ui.voice

import com.zuhdiarifazizi.projectakhirpapb.domain.model.VoiceIntent

data class VoiceInteractionState(
    val isListening: Boolean = false,
    val lastTranscript: String = "",
    val lastIntent: VoiceIntent? = null
)
