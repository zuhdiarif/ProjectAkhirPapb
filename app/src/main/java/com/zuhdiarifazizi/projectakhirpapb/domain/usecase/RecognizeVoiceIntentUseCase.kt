package com.zuhdiarifazizi.projectakhirpapb.domain.usecase

import com.zuhdiarifazizi.projectakhirpapb.domain.model.VoiceIntent

class RecognizeVoiceIntentUseCase {

    operator fun invoke(spokenText: String): VoiceIntent {
        return VoiceIntent.Unknown(spokenText)
    }
}
