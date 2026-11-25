package com.zuhdiarifazizi.projectakhirpapb.ui.voice

import com.zuhdiarifazizi.projectakhirpapb.domain.model.VoiceIntent
import com.zuhdiarifazizi.projectakhirpapb.domain.voice.VoiceCommand

enum class VoiceSessionStatus {
    IDLE,
    LISTENING,
    PROCESSING,
    SUCCESS,
    ERROR
}

data class VoiceInteractionState(
    val status: VoiceSessionStatus = VoiceSessionStatus.IDLE,
    val isListening: Boolean = false,
    val lastTranscript: String = "",
    val lastCommand: VoiceCommand? = null,
    val lastIntent: VoiceIntent? = null,
    val feedbackMessage: String? = null
)
