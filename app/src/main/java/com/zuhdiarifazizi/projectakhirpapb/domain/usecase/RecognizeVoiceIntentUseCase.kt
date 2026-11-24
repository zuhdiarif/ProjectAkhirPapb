package com.zuhdiarifazizi.projectakhirpapb.domain.usecase

import com.zuhdiarifazizi.projectakhirpapb.data.voice.IndonesianIntentRecognizer
import com.zuhdiarifazizi.projectakhirpapb.domain.model.VoiceIntent
import com.zuhdiarifazizi.projectakhirpapb.domain.voice.IntentRecognizer

class RecognizeVoiceIntentUseCase(
    private val intentRecognizer: IntentRecognizer = IndonesianIntentRecognizer()
) {

    operator fun invoke(spokenText: String): VoiceIntent {
        return intentRecognizer.recognize(spokenText).toLegacyVoiceIntent()
    }
}
