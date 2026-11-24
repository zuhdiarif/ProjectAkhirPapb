package com.zuhdiarifazizi.projectakhirpapb.data.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.zuhdiarifazizi.projectakhirpapb.domain.voice.SpeechRecognizerManager
import com.zuhdiarifazizi.projectakhirpapb.domain.voice.VoiceRecognitionError

class AndroidSpeechRecognizerManager(
    private val context: Context
) : SpeechRecognizerManager {

    private var speechRecognizer: SpeechRecognizer? = null
    private var _isListening: Boolean = false

    override val isListening: Boolean
        get() = _isListening

    override fun startListening(
        onResult: (String) -> Unit,
        onError: (VoiceRecognitionError) -> Unit,
        onPartialResult: ((String) -> Unit)?,
        onListeningStateChanged: ((Boolean) -> Unit)?
    ) {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onError(VoiceRecognitionError.ServiceUnavailable)
            return
        }

        stopListening()

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    _isListening = true
                    onListeningStateChanged?.invoke(true)
                }

                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    _isListening = false
                    onListeningStateChanged?.invoke(false)
                }

                override fun onError(error: Int) {
                    _isListening = false
                    onListeningStateChanged?.invoke(false)
                    val voiceError = mapErrorCode(error)
                    onError(voiceError)
                }

                override fun onResults(results: Bundle?) {
                    _isListening = false
                    onListeningStateChanged?.invoke(false)
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val text = matches?.firstOrNull()?.trim()
                    if (!text.isNullOrEmpty()) {
                        onResult(text)
                    } else {
                        onError(VoiceRecognitionError.NoMatch)
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    matches?.firstOrNull()?.let { onPartialResult?.invoke(it) }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "id-ID")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "id-ID")
            putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, "id-ID")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        }

        try {
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            _isListening = false
            onListeningStateChanged?.invoke(false)
            onError(VoiceRecognitionError.Unknown(-1, e.message ?: "Unknown error"))
        }
    }

    override fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.cancel()
        } catch (_: Exception) {}
        _isListening = false
    }

    override fun destroy() {
        stopListening()
        try {
            speechRecognizer?.destroy()
        } catch (_: Exception) {}
        speechRecognizer = null
    }

    private fun mapErrorCode(error: Int): VoiceRecognitionError {
        return when (error) {
            SpeechRecognizer.ERROR_AUDIO -> VoiceRecognitionError.AudioError
            SpeechRecognizer.ERROR_CLIENT -> VoiceRecognitionError.Unknown(error, "Client error")
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> VoiceRecognitionError.PermissionDenied
            SpeechRecognizer.ERROR_NETWORK -> VoiceRecognitionError.NetworkError
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> VoiceRecognitionError.NetworkError
            SpeechRecognizer.ERROR_NO_MATCH -> VoiceRecognitionError.NoMatch
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> VoiceRecognitionError.Unknown(error, "Recognizer busy")
            SpeechRecognizer.ERROR_SERVER -> VoiceRecognitionError.ServerDisconnected
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> VoiceRecognitionError.SpeechTimeout
            else -> VoiceRecognitionError.Unknown(error, "Speech error code $error")
        }
    }
}
