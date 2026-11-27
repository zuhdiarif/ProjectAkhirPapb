package com.zuhdiarifazizi.projectakhirpapb.ui.pickoutfit

import com.zuhdiarifazizi.projectakhirpapb.data.repository.DemoClothingCatalog
import com.zuhdiarifazizi.projectakhirpapb.data.repository.InMemoryClothesRepository
import com.zuhdiarifazizi.projectakhirpapb.data.repository.InMemoryOutfitRepository
import com.zuhdiarifazizi.projectakhirpapb.domain.model.Occasion
import com.zuhdiarifazizi.projectakhirpapb.ui.home.FakeHapticFeedbackManager
import com.zuhdiarifazizi.projectakhirpapb.ui.home.FakeTextToSpeechManager
import com.zuhdiarifazizi.projectakhirpapb.util.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class PickOutfitViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: InMemoryClothesRepository
    private lateinit var outfitRepository: InMemoryOutfitRepository
    private lateinit var tts: FakeTextToSpeechManager
    private lateinit var haptics: FakeHapticFeedbackManager
    private lateinit var viewModel: PickOutfitViewModel

    @Before
    fun setUp() {
        repository = InMemoryClothesRepository()
        outfitRepository = InMemoryOutfitRepository()
        tts = FakeTextToSpeechManager()
        haptics = FakeHapticFeedbackManager()
        viewModel = PickOutfitViewModel(
            clothesRepository = repository,
            ttsManager = tts,
            hapticManager = haptics,
            outfitRepository = outfitRepository
        )
    }

    @Test
    fun speakRecommendation_byIndexUpdatesStatusAndSpeaksTargetCandidate() = runTest {
        viewModel.beginSelection(Occasion.WORK)
        assertTrue(viewModel.generateRecommendations())
        val count = viewModel.uiState.value.candidates.size
        assertTrue(count >= 3)

        val targetIndex = 2
        val targetCandidate = viewModel.uiState.value.candidates[targetIndex]
        val expectedTopName = targetCandidate.topWear?.name.orEmpty()

        viewModel.speakRecommendation(targetIndex)

        assertEquals("Membacakan rekomendasi ${targetIndex + 1} dari $count.", viewModel.uiState.value.statusMessage)
        assertTrue(tts.lastSpeech.orEmpty().contains("Rekomendasi ${targetIndex + 1} dari $count"))
        assertTrue(tts.lastSpeech.orEmpty().contains(expectedTopName))
        assertTrue(haptics.buttonClickCount > 0)
    }

    @Test
    fun chooseRecommendation_persistsOutfitHistoryAndSetsSavedState() = runTest {
        viewModel.beginSelection(Occasion.EXERCISE)
        assertTrue(viewModel.generateRecommendations())
        assertTrue(viewModel.chooseRecommendation(0))

        val state = viewModel.uiState.value
        assertTrue(state.isSavedToHistory)
        val savedHistory = outfitRepository.getHistorySince(DemoClothingCatalog.USER_ID, 0L)
        assertEquals(1, savedHistory.size)
        assertEquals(Occasion.EXERCISE, savedHistory.first().occasion)
        assertTrue(tts.lastSpeech.orEmpty().contains("disimpan ke Riwayat", ignoreCase = true))
    }
}
