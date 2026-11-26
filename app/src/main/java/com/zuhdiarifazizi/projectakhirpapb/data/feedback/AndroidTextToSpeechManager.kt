package com.zuhdiarifazizi.projectakhirpapb.data.feedback

import android.content.Context
import android.media.AudioAttributes
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.zuhdiarifazizi.projectakhirpapb.domain.feedback.TextToSpeechManager
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AndroidTextToSpeechManager(
    context: Context,
    private val onInitComplete: ((Boolean) -> Unit)? = null
) : TextToSpeechManager, TextToSpeech.OnInitListener {

    private val appContext = context.applicationContext
    private val mainHandler = Handler(Looper.getMainLooper())
    private var tts: TextToSpeech? = TextToSpeech(appContext, this)
    private var isInitialized: Boolean = false
    private var pendingSpeech: String? = null
    private var _lastSpeech: String? = null
    @Volatile private var activeUtteranceId: String? = null
    @Volatile private var pendingCueId: String? = null
    private var pendingCueFinish: (() -> Unit)? = null
    private var pendingCueTimeout: Runnable? = null

    private val _isSpeakingFlow = MutableStateFlow(false)
    val isSpeakingFlow: StateFlow<Boolean> = _isSpeakingFlow.asStateFlow()

    private val _currentSpeechFlow = MutableStateFlow<String?>(null)
    val currentSpeechFlow: StateFlow<String?> = _currentSpeechFlow.asStateFlow()

    override val isSpeaking: Boolean
        get() = _isSpeakingFlow.value || (tts?.isSpeaking == true)

    override val lastSpeech: String?
        get() = _lastSpeech

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANT)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
            val indonesian = Locale.forLanguageTag("id-ID")
            val result = tts?.setLanguage(indonesian)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.language = Locale.getDefault()
            }
            tts?.setSpeechRate(0.95f)
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    if (utteranceId == activeUtteranceId) {
                        _isSpeakingFlow.value = true
                    }
                }

                override fun onDone(utteranceId: String?) {
                    if (utteranceId == pendingCueId) {
                        pendingCueFinish?.invoke()
                    } else if (utteranceId == activeUtteranceId) {
                        activeUtteranceId = null
                        _isSpeakingFlow.value = false
                        _currentSpeechFlow.value = null
                    }
                }

                override fun onStop(utteranceId: String?, interrupted: Boolean) {
                    if (utteranceId == pendingCueId) {
                        activeUtteranceId = null
                        _isSpeakingFlow.value = false
                        _currentSpeechFlow.value = null
                    } else if (utteranceId == activeUtteranceId) {
                        activeUtteranceId = null
                        _isSpeakingFlow.value = false
                        _currentSpeechFlow.value = null
                    }
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    if (utteranceId == pendingCueId) {
                        pendingCueFinish?.invoke()
                    } else if (utteranceId == activeUtteranceId) {
                        activeUtteranceId = null
                        _isSpeakingFlow.value = false
                        _currentSpeechFlow.value = null
                    }
                }
            })
            isInitialized = true
            onInitComplete?.invoke(true)
            pendingSpeech?.let { queued ->
                pendingSpeech = null
                speak(queued)
            }
        } else {
            isInitialized = false
            _isSpeakingFlow.value = false
            onInitComplete?.invoke(false)
        }
    }

    override fun speak(text: String, queueMode: Int, recordInHistory: Boolean) {
        if (text.isBlank()) return
        if (queueMode == TextToSpeech.QUEUE_FLUSH) {
            cancelPendingCue()
        }
        if (recordInHistory) {
            _lastSpeech = text
        }
        _currentSpeechFlow.value = text
        _isSpeakingFlow.value = true
        if (!isInitialized) {
            pendingSpeech = text
            return
        }
        val utteranceId = "aura_tts_${System.nanoTime()}"
        activeUtteranceId = utteranceId
        if (queueMode == TextToSpeech.QUEUE_FLUSH) {
            tts?.stop()
        }
        val params = Bundle().apply {
            putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f)
        }
        val status = tts?.speak(text, queueMode, params, utteranceId) ?: TextToSpeech.ERROR
        if (status == TextToSpeech.ERROR) {
            activeUtteranceId = null
            _isSpeakingFlow.value = false
            _currentSpeechFlow.value = null
        }
    }

    override fun speakCue(text: String, onComplete: () -> Unit) {
        cancelPendingCue()
        tts?.stop()
        if (text.isBlank() || !isInitialized) {
            activeUtteranceId = null
            _isSpeakingFlow.value = false
            _currentSpeechFlow.value = null
            onComplete()
            return
        }

        val cueId = "aura_cue_${System.nanoTime()}"
        activeUtteranceId = cueId
        pendingCueId = cueId
        _currentSpeechFlow.value = text
        _isSpeakingFlow.value = true

        val done = AtomicBoolean(false)
        val completeOnce: () -> Unit = {
            if (done.compareAndSet(false, true)) {
                val finishBlock = {
                    if (pendingCueId == cueId) {
                        pendingCueTimeout?.let { mainHandler.removeCallbacks(it) }
                        pendingCueTimeout = null
                        pendingCueId = null
                        pendingCueFinish = null
                    }
                    if (activeUtteranceId == cueId) {
                        activeUtteranceId = null
                    }
                    _isSpeakingFlow.value = false
                    _currentSpeechFlow.value = null
                    onComplete()
                }
                if (Looper.myLooper() == Looper.getMainLooper()) {
                    finishBlock()
                } else {
                    mainHandler.post(finishBlock)
                }
            }
        }

        val timeoutRunnable = Runnable { completeOnce() }
        pendingCueFinish = completeOnce
        pendingCueTimeout = timeoutRunnable
        mainHandler.postDelayed(timeoutRunnable, CUE_TIMEOUT_MILLIS)

        val params = Bundle().apply {
            putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f)
        }
        val status = tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, cueId) ?: TextToSpeech.ERROR
        if (status == TextToSpeech.ERROR) {
            completeOnce()
        }
    }

    override fun repeatLastSpeech() {
        val last = _lastSpeech
        if (!last.isNullOrBlank()) {
            speak(last, recordInHistory = true)
        } else {
            speak("Belum ada penjelasan sebelumnya untuk diulangi.", recordInHistory = false)
        }
    }

    override fun stop() {
        cancelPendingCue()
        activeUtteranceId = null
        pendingSpeech = null
        tts?.stop()
        _isSpeakingFlow.value = false
        _currentSpeechFlow.value = null
    }

    override fun shutdown() {
        cancelPendingCue()
        activeUtteranceId = null
        pendingSpeech = null
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
        _isSpeakingFlow.value = false
        _currentSpeechFlow.value = null
    }

    private fun cancelPendingCue() {
        pendingCueTimeout?.let { mainHandler.removeCallbacks(it) }
        pendingCueTimeout = null
        pendingCueId = null
        pendingCueFinish = null
    }

    private companion object {
        const val CUE_TIMEOUT_MILLIS = 1100L
    }
}
