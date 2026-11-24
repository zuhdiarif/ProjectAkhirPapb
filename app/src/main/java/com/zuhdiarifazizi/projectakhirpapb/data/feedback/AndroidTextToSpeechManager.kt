package com.zuhdiarifazizi.projectakhirpapb.data.feedback

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.zuhdiarifazizi.projectakhirpapb.domain.feedback.TextToSpeechManager
import java.util.Locale

class AndroidTextToSpeechManager(
    context: Context,
    private val onInitComplete: ((Boolean) -> Unit)? = null
) : TextToSpeechManager, TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var isInitialized: Boolean = false
    private var pendingSpeech: String? = null
    private var _lastSpeech: String? = null
    private var _isSpeaking: Boolean = false

    override val isSpeaking: Boolean
        get() = _isSpeaking || (tts?.isSpeaking == true)

    override val lastSpeech: String?
        get() = _lastSpeech

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val indonesian = Locale("id", "ID")
            val result = tts?.setLanguage(indonesian)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Fallback to default locale if Indonesian voice data is missing
                tts?.language = Locale.getDefault()
            }
            tts?.setSpeechRate(0.95f) // slightly comfortable pace for screen accessibility
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking = true
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking = false
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _isSpeaking = false
                }
            })
            isInitialized = true
            onInitComplete?.invoke(true)
            pendingSpeech?.let {
                speak(it)
                pendingSpeech = null
            }
        } else {
            isInitialized = false
            onInitComplete?.invoke(false)
        }
    }

    override fun speak(text: String, queueMode: Int) {
        _lastSpeech = text
        if (!isInitialized) {
            pendingSpeech = text
            return
        }
        val utteranceId = "aura_tts_${System.currentTimeMillis()}"
        tts?.speak(text, queueMode, null, utteranceId)
    }

    override fun repeatLastSpeech() {
        _lastSpeech?.let { speak(it) }
    }

    override fun stop() {
        tts?.stop()
        _isSpeaking = false
    }

    override fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
        _isSpeaking = false
    }
}
