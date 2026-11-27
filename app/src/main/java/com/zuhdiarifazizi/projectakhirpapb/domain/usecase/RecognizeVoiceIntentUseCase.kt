package com.zuhdiarifazizi.projectakhirpapb.domain.usecase

import com.zuhdiarifazizi.projectakhirpapb.domain.model.VoiceIntent
import com.zuhdiarifazizi.projectakhirpapb.domain.repository.IntentRecognizer

class RecognizeVoiceIntentUseCase(
    private val intentRecognizer: IntentRecognizer
) {

    operator fun invoke(spokenText: String): VoiceIntent {
        return intentRecognizer.recognize(spokenText).toLegacyVoiceIntent()
    }
}
