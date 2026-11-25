package com.zuhdiarifazizi.projectakhirpapb.domain.voice

import com.zuhdiarifazizi.projectakhirpapb.data.voice.IndonesianIntentRecognizer
import com.zuhdiarifazizi.projectakhirpapb.domain.model.Occasion
import com.zuhdiarifazizi.projectakhirpapb.ui.home.FakeHapticFeedbackManager
import com.zuhdiarifazizi.projectakhirpapb.ui.home.FakeTextToSpeechManager
import com.zuhdiarifazizi.projectakhirpapb.ui.voice.VoiceSessionStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FakeSpeechRecognizerManager : SpeechRecognizerManager {
    var isStarted = false
    var isDestroyed = false
    override var isListening: Boolean = false

    var onResultCallback: ((String) -> Unit)? = null
    var onErrorCallback: ((VoiceRecognitionError) -> Unit)? = null

    override fun startListening(
        onResult: (String) -> Unit,
        onError: (VoiceRecognitionError) -> Unit,
        onPartialResult: ((String) -> Unit)?,
        onListeningStateChanged: ((Boolean) -> Unit)?
    ) {
        isStarted = true
        isListening = true
        onResultCallback = onResult
        onErrorCallback = onError
        onListeningStateChanged?.invoke(true)
    }

    override fun stopListening() {
        isListening = false
    }

    override fun destroy() {
        isDestroyed = true
        isListening = false
    }

    fun emitResult(text: String) {
        onResultCallback?.invoke(text)
    }

    fun emitError(error: VoiceRecognitionError) {
        onErrorCallback?.invoke(error)
    }
}

class VoiceInteractionManagerTest {

    private lateinit var fakeStt: FakeSpeechRecognizerManager
    private lateinit var intentRecognizer: IndonesianIntentRecognizer
    private lateinit var fakeTts: FakeTextToSpeechManager
    private lateinit var fakeHaptic: FakeHapticFeedbackManager
    private lateinit var voiceManager: VoiceInteractionManager

    @Before
    fun setUp() {
        fakeStt = FakeSpeechRecognizerManager()
        intentRecognizer = IndonesianIntentRecognizer()
        fakeTts = FakeTextToSpeechManager()
        fakeHaptic = FakeHapticFeedbackManager()
        voiceManager = VoiceInteractionManager(
            speechRecognizerManager = fakeStt,
            intentRecognizer = intentRecognizer,
            ttsManager = fakeTts,
            hapticManager = fakeHaptic
        )
    }

    @Test
    fun startListening_permissionDenied_setsErrorAndSpeaksWarning() {
        voiceManager.startListening(hasRecordAudioPermission = false)

        val state = voiceManager.state.value
        assertEquals(VoiceSessionStatus.ERROR, state.status)
        assertFalse(state.isListening)
        assertEquals(1, fakeHaptic.errorCount)
        assertTrue(fakeTts.spokenTexts.any { it.contains("Izin mikrofon belum diberikan", ignoreCase = true) })
    }

    @Test
    fun startListening_permissionGranted_startsSTTAndSpeaksPrompt() {
        voiceManager.startListening(hasRecordAudioPermission = true)

        val state = voiceManager.state.value
        assertEquals(VoiceSessionStatus.LISTENING, state.status)
        assertTrue(state.isListening)
        assertTrue(fakeStt.isStarted)
        assertTrue(fakeTts.spokenTexts.any { it.contains("Mendengarkan", ignoreCase = true) })
    }

    @Test
    fun processRecognizedSpeech_selectWork_triggersCallbackAndSuccess() {
        var selectedOccasion: Occasion? = null
        val callback = object : VoiceNavigationCallback {
            override fun onSelectOccasion(occasion: Occasion) {
                selectedOccasion = occasion
            }
            override fun onOpenHistory() {}
            override fun onOpenWardrobe() {}
        }

        voiceManager.processRecognizedSpeech("Mau pergi kerja kantor", callback)

        val state = voiceManager.state.value
        assertEquals(VoiceSessionStatus.SUCCESS, state.status)
        assertEquals(Occasion.WORK, selectedOccasion)
        assertEquals(1, fakeHaptic.successCount)
        assertTrue(fakeTts.spokenTexts.any { it.contains("Kerja", ignoreCase = true) })
    }

    @Test
    fun processRecognizedSpeech_openHistory_triggersHistoryCallback() {
        var historyOpened = false
        val callback = object : VoiceNavigationCallback {
            override fun onSelectOccasion(occasion: Occasion) {}
            override fun onOpenHistory() { historyOpened = true }
            override fun onOpenWardrobe() {}
        }

        voiceManager.processRecognizedSpeech("Tolong buka riwayat pakaian", callback)

        assertTrue(historyOpened)
        assertEquals(1, fakeHaptic.successCount)
        assertTrue(fakeTts.spokenTexts.any { it.contains("riwayat", ignoreCase = true) })
    }

    @Test
    fun processRecognizedSpeech_openWardrobe_triggersWardrobeCallback() {
        var wardrobeOpened = false
        val callback = object : VoiceNavigationCallback {
            override fun onSelectOccasion(occasion: Occasion) {}
            override fun onOpenHistory() {}
            override fun onOpenWardrobe() { wardrobeOpened = true }
        }

        voiceManager.processRecognizedSpeech("Buka lemari pakaian saya", callback)

        assertTrue(wardrobeOpened)
        assertEquals(1, fakeHaptic.successCount)
        assertTrue(fakeTts.spokenTexts.any { it.contains("lemari", ignoreCase = true) })
    }

    @Test
    fun processRecognizedSpeech_addClothingUsesExistingNavigationCallback() {
        var formOpened = false
        val callback = object : VoiceNavigationCallback {
            override fun onSelectOccasion(occasion: Occasion) {}
            override fun onOpenHistory() {}
            override fun onOpenWardrobe() {}
            override fun onAddClothing() { formOpened = true }
        }

        voiceManager.processRecognizedSpeech("Tambahkan pakaian", callback)

        assertTrue(formOpened)
        assertEquals(1, fakeHaptic.successCount)
    }

    @Test
    fun processRecognizedSpeech_cancelAction_stopsListening() {
        voiceManager.processRecognizedSpeech("Batalkan aksi", null)

        val state = voiceManager.state.value
        assertEquals(VoiceSessionStatus.IDLE, state.status)
        assertFalse(state.isListening)
        assertTrue(fakeTts.spokenTexts.any { it.contains("dibatalkan", ignoreCase = true) })
    }

    @Test
    fun processRecognizedSpeech_unknownPhrase_speaksClarificationAndErrorHaptic() {
        voiceManager.processRecognizedSpeech("Robot jalan ke mars", null)

        val state = voiceManager.state.value
        assertEquals(VoiceSessionStatus.ERROR, state.status)
        assertEquals(1, fakeHaptic.errorCount)
        assertTrue(fakeTts.spokenTexts.any { it.contains("belum memahami", ignoreCase = true) })
    }

    @Test
    fun emitError_speechTimeout_updatesStateAndSpeaksTimeoutMessage() {
        voiceManager.startListening(hasRecordAudioPermission = true)
        fakeStt.emitError(VoiceRecognitionError.SpeechTimeout)

        val state = voiceManager.state.value
        assertEquals(VoiceSessionStatus.ERROR, state.status)
        assertEquals(1, fakeHaptic.errorCount)
        assertTrue(fakeTts.spokenTexts.any { it.contains("Waktu bicara habis", ignoreCase = true) })
    }
}
