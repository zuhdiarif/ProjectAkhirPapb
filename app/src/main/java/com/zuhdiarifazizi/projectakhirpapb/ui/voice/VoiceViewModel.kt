package com.zuhdiarifazizi.projectakhirpapb.ui.voice

import androidx.lifecycle.ViewModel
import com.zuhdiarifazizi.projectakhirpapb.domain.usecase.RecognizeVoiceIntentUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class VoiceViewModel(
    private val recognizeVoiceIntentUseCase: RecognizeVoiceIntentUseCase = RecognizeVoiceIntentUseCase()
) : ViewModel() {

    private val _state = MutableStateFlow(VoiceInteractionState())
    val state: StateFlow<VoiceInteractionState> = _state.asStateFlow()
}
