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
    fun onOpenHome() {}
    fun onNavigateBack() {}
    fun onContinueRecommendation() {}
    fun onNextRecommendation() {}
    fun onSelectCurrentRecommendation() {}
    fun onListenRecommendations() {}
    fun onSelectFirstRecommendation() {}
    fun onAddClothing() {}
    fun onOpenClothingCamera() {}
    fun onScanRfid() {}
    fun onQuickLogin() {}
    fun onLogoutAccount() {}
}

class VoiceInteractionManager(
    private val speechRecognizerManager: SpeechRecognizerManager,
    private val intentRecognizer: IntentRecognizer,
    private val ttsManager: TextToSpeechManager,
    private val hapticManager: HapticFeedbackManager
) {
    private val _state = MutableStateFlow(VoiceInteractionState())
    val state: StateFlow<VoiceInteractionState> = _state.asStateFlow()

    private var listenSessionToken: Long = 0L

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
            ++listenSessionToken
            hapticManager.performError()
            val msg = "Izin mikrofon diperlukan untuk menggunakan perintah suara."
            _state.update {
                it.copy(
                    status = VoiceSessionStatus.ERROR,
                    isListening = false,
                    lastTranscript = "",
                    feedbackMessage = msg
                )
            }
            ttsManager.speak(
                text = "Izin mikrofon belum diberikan. Silakan izinkan akses mikrofon.",
                recordInHistory = false
            )
            return
        }

        val token = ++listenSessionToken
        hapticManager.performButtonClick()
        _state.update {
            it.copy(
                status = VoiceSessionStatus.LISTENING,
                isListening = true,
                lastTranscript = "",
                feedbackMessage = "Mendengarkan..."
            )
        }

        // Wait for the short audio cue to finish before opening the microphone so STT
        // does not capture our own TTS output, and keep lastSpeech intact for "Ulangi".
        ttsManager.speakCue("Mendengarkan.") {
            if (token != listenSessionToken) return@speakCue
            speechRecognizerManager.startListening(
                onResult = { recognizedText ->
                    if (token != listenSessionToken) return@startListening
                    processRecognizedSpeech(recognizedText, navigationCallback)
                },
                onError = { error ->
                    if (token != listenSessionToken) return@startListening
                    handleSpeechError(error)
                },
                onPartialResult = { partial ->
                    if (token != listenSessionToken) return@startListening
                    _state.update { it.copy(lastTranscript = partial) }
                },
                onListeningStateChanged = { listening ->
                    if (token != listenSessionToken) return@startListening
                    _state.update { it.copy(isListening = listening) }
                }
            )
        }
    }

    fun cancelListening() {
        ++listenSessionToken
        speechRecognizerManager.stopListening()
        hapticManager.performButtonClick()
        _state.update {
            it.copy(
                status = VoiceSessionStatus.IDLE,
                isListening = false,
                lastTranscript = "",
                feedbackMessage = null
            )
        }
        ttsManager.speak("Perintah suara dibatalkan.", recordInHistory = false)
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
            is VoiceCommand.OpenHome -> {
                hapticManager.performSuccess()
                ttsManager.speak("Membuka halaman Beranda.")
                _state.update {
                    it.copy(
                        status = VoiceSessionStatus.SUCCESS,
                        feedbackMessage = "Membuka Beranda"
                    )
                }
                navigationCallback?.onOpenHome()
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
            is VoiceCommand.NavigateBack -> {
                hapticManager.performButtonClick()
                _state.update {
                    it.copy(
                        status = VoiceSessionStatus.SUCCESS,
                        feedbackMessage = "Kembali ke halaman sebelumnya"
                    )
                }
                if (navigationCallback != null) {
                    navigationCallback.onNavigateBack()
                } else {
                    ttsManager.speak("Kembali ke halaman sebelumnya.")
                }
            }
            is VoiceCommand.RepeatResponse -> {
                hapticManager.performButtonClick()
                ttsManager.repeatLastSpeech()
                _state.update {
                    it.copy(
                        status = VoiceSessionStatus.SUCCESS,
                        feedbackMessage = "Mengulang penjelasan terakhir"
                    )
                }
            }
            is VoiceCommand.ContinueRecommendation -> {
                hapticManager.performSuccess()
                _state.update {
                    it.copy(
                        status = VoiceSessionStatus.SUCCESS,
                        feedbackMessage = "Membuat rekomendasi pakaian"
                    )
                }
                if (navigationCallback != null) {
                    navigationCallback.onContinueRecommendation()
                } else {
                    ttsManager.speak("Pilih kategori aktivitas terlebih dahulu untuk membuat rekomendasi.")
                }
            }
            is VoiceCommand.NextRecommendation -> {
                hapticManager.performButtonClick()
                _state.update {
                    it.copy(
                        status = VoiceSessionStatus.SUCCESS,
                        feedbackMessage = "Menampilkan rekomendasi berikutnya"
                    )
                }
                if (navigationCallback != null) {
                    navigationCallback.onNextRecommendation()
                } else {
                    ttsManager.speak("Buka daftar rekomendasi untuk melihat pilihan berikutnya.")
                }
            }
            is VoiceCommand.ListenRecommendations -> {
                hapticManager.performSuccess()
                _state.update {
                    it.copy(
                        status = VoiceSessionStatus.SUCCESS,
                        feedbackMessage = "Membacakan rekomendasi pakaian"
                    )
                }
                if (navigationCallback != null) {
                    navigationCallback.onListenRecommendations()
                } else {
                    ttsManager.speak("Buka halaman rekomendasi pakaian untuk mendengarkan rekomendasi.")
                }
            }
            is VoiceCommand.SelectFirstRecommendation -> {
                hapticManager.performSuccess()
                _state.update {
                    it.copy(
                        status = VoiceSessionStatus.SUCCESS,
                        feedbackMessage = "Memilih rekomendasi pertama"
                    )
                }
                if (navigationCallback != null) {
                    navigationCallback.onSelectFirstRecommendation()
                } else {
                    ttsManager.speak("Buka halaman rekomendasi pakaian untuk memilih rekomendasi pertama.")
                }
            }
            is VoiceCommand.SelectCurrentRecommendation -> {
                hapticManager.performSuccess()
                _state.update {
                    it.copy(
                        status = VoiceSessionStatus.SUCCESS,
                        feedbackMessage = "Memilih rekomendasi saat ini"
                    )
                }
                if (navigationCallback != null) {
                    navigationCallback.onSelectCurrentRecommendation()
                } else {
                    ttsManager.speak("Buka halaman rekomendasi pakaian untuk memilih pakaian ini.")
                }
            }
            is VoiceCommand.AddClothing -> {
                hapticManager.performSuccess()
                _state.update { it.copy(status = VoiceSessionStatus.SUCCESS, feedbackMessage = "Membuka form tambah pakaian") }
                if (navigationCallback != null) navigationCallback.onAddClothing()
                else ttsManager.speak("Buka halaman Kelola Pakaian untuk menambahkan baju.")
            }
            is VoiceCommand.OpenClothingCamera -> {
                hapticManager.performSuccess()
                _state.update { it.copy(status = VoiceSessionStatus.SUCCESS, feedbackMessage = "Membuka kamera pakaian") }
                if (navigationCallback != null) navigationCallback.onOpenClothingCamera()
                else ttsManager.speak("Buka form tambah pakaian untuk menggunakan kamera.")
            }
            is VoiceCommand.ScanRfid -> {
                hapticManager.performSuccess()
                _state.update { it.copy(status = VoiceSessionStatus.SUCCESS, feedbackMessage = "Membuka pemindai RFID dan AI") }
                if (navigationCallback != null) navigationCallback.onScanRfid()
                else ttsManager.speak("Membuka pemindai RFID untuk mengenali pakaian Anda.")
            }
            is VoiceCommand.QuickLogin -> {
                hapticManager.performSuccess()
                _state.update { it.copy(status = VoiceSessionStatus.SUCCESS, feedbackMessage = "Masuk cepat ke Beranda") }
                if (navigationCallback != null) navigationCallback.onQuickLogin()
                else ttsManager.speak("Membuka Beranda Aura dengan akun lokal.")
            }
            is VoiceCommand.LogoutAccount -> {
                hapticManager.performButtonClick()
                _state.update { it.copy(status = VoiceSessionStatus.SUCCESS, feedbackMessage = "Keluar dari akun") }
                if (navigationCallback != null) navigationCallback.onLogoutAccount()
                else ttsManager.speak("Keluar dari akun.")
            }
            is VoiceCommand.CancelAction -> {
                cancelListening()
            }
            is VoiceCommand.Unknown -> {
                hapticManager.performError()
                val response =
                    "Maaf, saya belum memahami perintah Anda. Silakan coba katakan: Kerja, Nongkrong, Olahraga, Streetwear, Buka Lemari, Lanjutkan, Ulangi, atau Kembali."
                ttsManager.speak(response, recordInHistory = false)
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
        ttsManager.speak(userMsg, recordInHistory = false)
    }

    fun cleanup() {
        ++listenSessionToken
        speechRecognizerManager.destroy()
        ttsManager.stop()
    }
}
