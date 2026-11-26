package com.zuhdiarifazizi.projectakhirpapb.data.feedback

import android.content.Context
import android.media.AudioAttributes
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.zuhdiarifazizi.projectakhirpapb.domain.feedback.TextToSpeechManager
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AndroidTextToSpeechManager(
    context: Context,
    private val onInitComplete: ((Boolean) -> Unit)? = null
) : TextToSpeechManager, TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var isInitialized: Boolean = false
    private var pendingSpeech: String? = null
    private var _lastSpeech: String? = null
    @Volatile private var activeUtteranceId: String? = null

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
                    if (utteranceId == activeUtteranceId) {
                        activeUtteranceId = null
                        _isSpeakingFlow.value = false
                        _currentSpeechFlow.value = null
                    }
                }

                override fun onStop(utteranceId: String?, interrupted: Boolean) {
                    if (utteranceId == activeUtteranceId) {
                        activeUtteranceId = null
                        _isSpeakingFlow.value = false
                        _currentSpeechFlow.value = null
                    }
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    if (utteranceId == activeUtteranceId) {
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

    override fun speak(text: String, queueMode: Int) {
        if (text.isBlank()) return
        _lastSpeech = text
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

    override fun repeatLastSpeech() {
        _lastSpeech?.let { speak(it) }
    }

    override fun stop() {
        activeUtteranceId = null
        pendingSpeech = null
        tts?.stop()
        _isSpeakingFlow.value = false
        _currentSpeechFlow.value = null
    }

    override fun shutdown() {
        activeUtteranceId = null
        pendingSpeech = null
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
        _isSpeakingFlow.value = false
        _currentSpeechFlow.value = null
    }
}
