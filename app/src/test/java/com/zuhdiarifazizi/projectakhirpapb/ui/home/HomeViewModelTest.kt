package com.zuhdiarifazizi.projectakhirpapb.ui.home

import com.zuhdiarifazizi.projectakhirpapb.data.repository.DefaultActivityCategoryRepository
import com.zuhdiarifazizi.projectakhirpapb.domain.feedback.HapticFeedbackManager
import com.zuhdiarifazizi.projectakhirpapb.domain.feedback.TextToSpeechManager
import com.zuhdiarifazizi.projectakhirpapb.domain.model.Occasion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FakeHapticFeedbackManager : HapticFeedbackManager {
    var buttonClickCount = 0
    var categorySelectedCount = 0
    var successCount = 0
    var errorCount = 0

    override fun performButtonClick() { buttonClickCount++ }
    override fun performCategorySelected() { categorySelectedCount++ }
    override fun performSuccess() { successCount++ }
    override fun performError() { errorCount++ }
}

class FakeTextToSpeechManager : TextToSpeechManager {
    val spokenTexts = mutableListOf<String>()
    override var isSpeaking: Boolean = false
    override var lastSpeech: String? = null

    override fun speak(text: String, queueMode: Int) {
        spokenTexts.add(text)
        lastSpeech = text
        isSpeaking = true
    }

    override fun repeatLastSpeech() {
        lastSpeech?.let { spokenTexts.add(it) }
    }

    override fun stop() {
        isSpeaking = false
    }

    override fun shutdown() {
        isSpeaking = false
    }
}

class HomeViewModelTest {

    private lateinit var viewModel: HomeViewModel
    private lateinit var fakeHaptic: FakeHapticFeedbackManager
    private lateinit var fakeTts: FakeTextToSpeechManager

    @Before
    fun setUp() {
        fakeHaptic = FakeHapticFeedbackManager()
        fakeTts = FakeTextToSpeechManager()
        viewModel = HomeViewModel(
            activityCategoryRepository = DefaultActivityCategoryRepository(),
            ttsManager = fakeTts,
            hapticManager = fakeHaptic
        )
    }

    @Test
    fun uiState_initiallyLoadsAllFiveCategories() {
        val state = viewModel.uiState.value
        assertEquals(5, state.categories.size)
        assertEquals("Kerja", state.categories[0].title)
    }

    @Test
    fun playWelcomeGuidance_speaksWelcomeMessageViaTTS() {
        viewModel.playWelcomeGuidance()
        assertTrue(viewModel.uiState.value.hasPlayedWelcome)
        assertEquals(1, fakeTts.spokenTexts.size)
        assertTrue(fakeTts.spokenTexts[0].contains("Selamat datang di Aura"))
    }

    @Test
    fun selectCategory_triggersHapticAndSpeaksCategoryConfirmation() {
        val target = viewModel.uiState.value.categories[0] // Kerja
        var navigated = false
        viewModel.selectCategory(target) {
            navigated = true
        }

        assertTrue(navigated)
        assertEquals(target, viewModel.uiState.value.selectedCategory)
        assertEquals(1, fakeHaptic.categorySelectedCount)
        assertTrue(fakeTts.spokenTexts.any { it.contains("Tujuan Kerja dipilih") })
    }

    @Test
    fun onVoiceActionClicked_whenPermissionDenied_showsErrorBannerAndSpeaksError() {
        var startListeningCalled = false
        viewModel.onVoiceActionClicked(hasRecordAudioPermission = false) {
            startListeningCalled = true
        }

        assertFalse(startListeningCalled)
        assertFalse(viewModel.uiState.value.isVoiceListening)
        assertNotNull(viewModel.uiState.value.feedbackBanner)
        assertTrue(viewModel.uiState.value.feedbackBanner!!.isError)
        assertEquals(1, fakeHaptic.errorCount)
        assertTrue(fakeTts.spokenTexts.any { it.contains("izin mikrofon", ignoreCase = true) })
    }

    @Test
    fun onVoiceActionClicked_whenPermissionGranted_enablesListeningAndSpeaksPrompt() {
        var startListeningCalled = false
        viewModel.onVoiceActionClicked(hasRecordAudioPermission = true) {
            startListeningCalled = true
        }

        assertTrue(startListeningCalled)
        assertTrue(viewModel.uiState.value.isVoiceListening)
        assertEquals(1, fakeHaptic.successCount)
        assertTrue(fakeTts.spokenTexts.any { it.contains("Mendengarkan") })
    }

    @Test
    fun dismissFeedbackBanner_clearsBannerAndClicksHaptic() {
        viewModel.setFeedbackBanner("Error test", isError = true)
        assertNotNull(viewModel.uiState.value.feedbackBanner)

        viewModel.dismissFeedbackBanner()
        assertNull(viewModel.uiState.value.feedbackBanner)
        assertTrue(fakeHaptic.buttonClickCount > 0)
    }

    @Test
    fun homeViewModel_withVoiceInteractionManager_triggersVoiceWorkflow() {
        val fakeStt = com.zuhdiarifazizi.projectakhirpapb.domain.voice.FakeSpeechRecognizerManager()
        val intentRecognizer = com.zuhdiarifazizi.projectakhirpapb.data.voice.IndonesianIntentRecognizer()
        val voiceManager = com.zuhdiarifazizi.projectakhirpapb.domain.voice.VoiceInteractionManager(
            speechRecognizerManager = fakeStt,
            intentRecognizer = intentRecognizer,
            ttsManager = fakeTts,
            hapticManager = fakeHaptic
        )

        val vmWithVoice = HomeViewModel(
            activityCategoryRepository = DefaultActivityCategoryRepository(),
            ttsManager = fakeTts,
            hapticManager = fakeHaptic,
            voiceInteractionManager = voiceManager,
            externalScope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Unconfined)
        )

        var selectedOccasion: Occasion? = null
        val callback = object : com.zuhdiarifazizi.projectakhirpapb.domain.voice.VoiceNavigationCallback {
            override fun onSelectOccasion(occasion: Occasion) {
                selectedOccasion = occasion
            }
            override fun onOpenHistory() {}
            override fun onOpenWardrobe() {}
        }

        vmWithVoice.onVoiceActionClicked(hasRecordAudioPermission = true, navigationCallback = callback)
        assertTrue(vmWithVoice.uiState.value.isVoiceListening)

        // STT emits user utterance
        fakeStt.emitResult("Mau pergi kerja kantor")

        assertEquals(Occasion.WORK, selectedOccasion)
        assertFalse(vmWithVoice.uiState.value.isVoiceListening)
    }
}
