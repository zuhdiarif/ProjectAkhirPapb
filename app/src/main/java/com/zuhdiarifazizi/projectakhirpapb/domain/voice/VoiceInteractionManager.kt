package com.zuhdiarifazizi.projectakhirpapb.domain.voice

import com.zuhdiarifazizi.projectakhirpapb.domain.feedback.HapticFeedbackManager
import com.zuhdiarifazizi.projectakhirpapb.domain.feedback.TextToSpeechManager
import com.zuhdiarifazizi.projectakhirpapb.domain.model.Occasion
import com.zuhdiarifazizi.projectakhirpapb.ui.voice.VoiceInteractionState
import com.zuhdiarifazizi.projectakhirpapb.ui.voice.VoiceSessionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

interface VoiceNavigationCallback {
    fun onSelectOccasion(occasion: Occasion)
    fun onOpenHistory()
    fun onOpenWardrobe()
}

class VoiceInteractionManager(
    private val speechRecognizerManager: SpeechRecognizerManager,
    private val intentRecognizer: IntentRecognizer,
    private val ttsManager: TextToSpeechManager,
    private val hapticManager: HapticFeedbackManager
) {
    private val _state = MutableStateFlow(VoiceInteractionState())
    val state: StateFlow<VoiceInteractionState> = _state.asStateFlow()

    fun toggleVoiceInteraction(
        hasRecordAudioPermission: Boolean,
        navigationCallback: VoiceNavigationCallback? = null
    ) {
        if (_state.value.isListening) {
            cancelListening()
        } else {
            startListening(hasRecordAudioPermission, navigationCallback)
        }
    }

    fun startListening(
        hasRecordAudioPermission: Boolean,
        navigationCallback: VoiceNavigationCallback? = null
    ) {
        if (!hasRecordAudioPermission) {
            hapticManager.performError()
            val msg = "Izin mikrofon diperlukan untuk menggunakan perintah suara."
            _state.update {
                it.copy(
                    status = VoiceSessionStatus.ERROR,
                    isListening = false,
                    feedbackMessage = msg
                )
            }
            ttsManager.speak("Izin mikrofon belum diberikan. Silakan izinkan akses mikrofon.")
            return
        }

        hapticManager.performButtonClick()
        _state.update {
            it.copy(
                status = VoiceSessionStatus.LISTENING,
                isListening = true,
                feedbackMessage = "Mendengarkan..."
            )
        }
        ttsManager.speak("Mendengarkan. Silakan katakan tujuan pakaian Anda.")

        speechRecognizerManager.startListening(
            onResult = { recognizedText ->
                processRecognizedSpeech(recognizedText, navigationCallback)
            },
            onError = { error ->
                handleSpeechError(error)
            },
            onPartialResult = { partial ->
                _state.update { it.copy(lastTranscript = partial) }
            },
            onListeningStateChanged = { listening ->
                _state.update { it.copy(isListening = listening) }
            }
        )
    }

    fun cancelListening() {
        speechRecognizerManager.stopListening()
        hapticManager.performButtonClick()
        _state.update {
            it.copy(
                status = VoiceSessionStatus.IDLE,
                isListening = false,
                feedbackMessage = null
            )
        }
        ttsManager.speak("Perintah suara dibatalkan.")
    }

    fun processRecognizedSpeech(
        spokenText: String,
        navigationCallback: VoiceNavigationCallback?
    ) {
        _state.update {
            it.copy(
                status = VoiceSessionStatus.PROCESSING,
                isListening = false,
                lastTranscript = spokenText
            )
        }

        val command = intentRecognizer.recognize(spokenText)
        _state.update {
            it.copy(
                lastCommand = command,
                lastIntent = command.toLegacyVoiceIntent()
            )
        }

        when (command) {
            is VoiceCommand.SelectWork -> {
                hapticManager.performSuccess()
                ttsManager.speak("Baik, Anda ingin pergi bekerja.")
                _state.update {
                    it.copy(
                        status = VoiceSessionStatus.SUCCESS,
                        feedbackMessage = "Tujuan Kerja dipilih"
                    )
                }
                navigationCallback?.onSelectOccasion(Occasion.WORK)
            }
            is VoiceCommand.SelectHangout -> {
                hapticManager.performSuccess()
                ttsManager.speak("Baik, Anda ingin nongkrong bersama teman.")
                _state.update {
                    it.copy(
                        status = VoiceSessionStatus.SUCCESS,
                        feedbackMessage = "Tujuan Nongkrong dipilih"
                    )
                }
                navigationCallback?.onSelectOccasion(Occasion.HANGOUT)
            }
            is VoiceCommand.SelectSport -> {
                hapticManager.performSuccess()
                ttsManager.speak("Baik, Anda ingin berolahraga.")
                _state.update {
                    it.copy(
                        status = VoiceSessionStatus.SUCCESS,
                        feedbackMessage = "Tujuan Olahraga dipilih"
                    )
                }
                navigationCallback?.onSelectOccasion(Occasion.EXERCISE)
            }
            is VoiceCommand.SelectStreetwear -> {
                hapticManager.performSuccess()
                ttsManager.speak("Baik, Anda memilih gaya streetwear santai.")
                _state.update {
                    it.copy(
                        status = VoiceSessionStatus.SUCCESS,
                        feedbackMessage = "Tujuan Streetwear dipilih"
                    )
                }
                navigationCallback?.onSelectOccasion(Occasion.STREETWEAR)
            }
            is VoiceCommand.SelectOther -> {
                hapticManager.performSuccess()
                ttsManager.speak("Baik, Anda memilih kategori pakaian lainnya.")
                _state.update {
                    it.copy(
                        status = VoiceSessionStatus.SUCCESS,
                        feedbackMessage = "Kategori Lainnya dipilih"
                    )
                }
                navigationCallback?.onSelectOccasion(Occasion.OTHER)
            }
            is VoiceCommand.OpenHistory -> {
                hapticManager.performSuccess()
                ttsManager.speak("Membuka riwayat pakaian Anda.")
                _state.update {
                    it.copy(
                        status = VoiceSessionStatus.SUCCESS,
                        feedbackMessage = "Membuka riwayat pakaian"
                    )
                }
                navigationCallback?.onOpenHistory()
            }
            is VoiceCommand.OpenWardrobe -> {
                hapticManager.performSuccess()
                ttsManager.speak("Membuka lemari pakaian Anda.")
                _state.update {
                    it.copy(
                        status = VoiceSessionStatus.SUCCESS,
                        feedbackMessage = "Membuka lemari pakaian"
                    )
                }
                navigationCallback?.onOpenWardrobe()
            }
            is VoiceCommand.RepeatResponse -> {
                hapticManager.performButtonClick()
                ttsManager.repeatLastSpeech()
                _state.update {
                    it.copy(status = VoiceSessionStatus.SUCCESS)
                }
            }
            is VoiceCommand.CancelAction -> {
                cancelListening()
            }
            is VoiceCommand.Unknown -> {
                hapticManager.performError()
                val response = "Maaf, saya belum memahami perintah Anda. Silakan coba katakan: Kerja, Nongkrong, Olahraga, Streetwear, atau Buka Lemari."
                ttsManager.speak(response)
                _state.update {
                    it.copy(
                        status = VoiceSessionStatus.ERROR,
                        feedbackMessage = response
                    )
                }
            }
        }
    }

    private fun handleSpeechError(error: VoiceRecognitionError) {
        hapticManager.performError()
        val userMsg = error.toUserMessage()
        _state.update {
            it.copy(
                status = VoiceSessionStatus.ERROR,
                isListening = false,
                feedbackMessage = userMsg
            )
        }
        ttsManager.speak(userMsg)
    }

    fun cleanup() {
        speechRecognizerManager.destroy()
        ttsManager.stop()
    }
}
