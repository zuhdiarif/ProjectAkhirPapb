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
    context: Context
) : SpeechRecognizerManager {

    private val appContext = context.applicationContext
    private var speechRecognizer: SpeechRecognizer? = null
    private var _isListening: Boolean = false
    private var activeSessionId: Long = 0L

    override val isListening: Boolean
        get() = _isListening

    override fun startListening(
        onResult: (String) -> Unit,
        onError: (VoiceRecognitionError) -> Unit,
        onPartialResult: ((String) -> Unit)?,
        onListeningStateChanged: ((Boolean) -> Unit)?
    ) {
        if (!SpeechRecognizer.isRecognitionAvailable(appContext)) {
            _isListening = false
            onListeningStateChanged?.invoke(false)
            onError(VoiceRecognitionError.ServiceUnavailable)
            return
        }

        val sessionId = ++activeSessionId
        releaseRecognizer()

        val recognizer = try {
            SpeechRecognizer.createSpeechRecognizer(appContext)
        } catch (e: Exception) {
            _isListening = false
            onListeningStateChanged?.invoke(false)
            onError(VoiceRecognitionError.Unknown(-1, e.message ?: "Failed to create SpeechRecognizer"))
            return
        }

        recognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                if (sessionId != activeSessionId) return
                _isListening = true
                onListeningStateChanged?.invoke(true)
            }

            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                if (sessionId != activeSessionId) return
                _isListening = false
                onListeningStateChanged?.invoke(false)
            }

            override fun onError(error: Int) {
                if (sessionId != activeSessionId) return
                _isListening = false
                onListeningStateChanged?.invoke(false)
                releaseRecognizer()
                onError(mapErrorCode(error))
            }

            override fun onResults(results: Bundle?) {
                if (sessionId != activeSessionId) return
                _isListening = false
                onListeningStateChanged?.invoke(false)
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.firstOrNull()?.trim()
                releaseRecognizer()
                if (!text.isNullOrEmpty()) {
                    onResult(text)
                } else {
                    onError(VoiceRecognitionError.NoMatch)
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                if (sessionId != activeSessionId) return
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                matches?.firstOrNull()?.let { onPartialResult?.invoke(it) }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        speechRecognizer = recognizer

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "id-ID")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "id-ID")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        }

        try {
            _isListening = true
            onListeningStateChanged?.invoke(true)
            recognizer.startListening(intent)
        } catch (e: Exception) {
            releaseRecognizer()
            onListeningStateChanged?.invoke(false)
            onError(VoiceRecognitionError.Unknown(-1, e.message ?: "Unknown error"))
        }
    }

    override fun stopListening() {
        ++activeSessionId
        releaseRecognizer()
    }

    override fun destroy() {
        ++activeSessionId
        releaseRecognizer()
    }

    private fun releaseRecognizer() {
        _isListening = false
        val current = speechRecognizer
        speechRecognizer = null
        try {
            current?.cancel()
        } catch (_: Exception) {}
        try {
            current?.destroy()
        } catch (_: Exception) {}
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
