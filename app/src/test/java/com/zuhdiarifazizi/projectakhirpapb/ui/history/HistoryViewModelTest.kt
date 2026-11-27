package com.zuhdiarifazizi.projectakhirpapb.ui.history

import com.zuhdiarifazizi.projectakhirpapb.data.repository.DemoClothingCatalog
import com.zuhdiarifazizi.projectakhirpapb.data.repository.InMemoryClothesRepository
import com.zuhdiarifazizi.projectakhirpapb.data.repository.InMemoryOutfitRepository
import com.zuhdiarifazizi.projectakhirpapb.domain.model.Occasion
import com.zuhdiarifazizi.projectakhirpapb.domain.model.OutfitHistory
import com.zuhdiarifazizi.projectakhirpapb.ui.home.FakeHapticFeedbackManager
import com.zuhdiarifazizi.projectakhirpapb.ui.home.FakeTextToSpeechManager
import com.zuhdiarifazizi.projectakhirpapb.util.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class HistoryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var clothesRepository: InMemoryClothesRepository
    private lateinit var outfitRepository: InMemoryOutfitRepository
    private lateinit var tts: FakeTextToSpeechManager
    private lateinit var haptics: FakeHapticFeedbackManager
    private lateinit var viewModel: HistoryViewModel

    @Before
    fun setUp() {
        val now = System.currentTimeMillis()
        val clothes = DemoClothingCatalog.items
        val entry = OutfitHistory(
            id = "h_1",
            userId = DemoClothingCatalog.USER_ID,
            outfitId = "${clothes[0].id}|${clothes[1].id}",
            items = listOf(clothes[0], clothes[1]),
            occasion = Occasion.WORK,
            score = 0.91,
            wornAtEpochMillis = now - 86_400_000L
        )
        clothesRepository = InMemoryClothesRepository(clothes)
        outfitRepository = InMemoryOutfitRepository(listOf(entry))
        tts = FakeTextToSpeechManager()
        haptics = FakeHapticFeedbackManager()
        viewModel = HistoryViewModel(
            clothesRepository = clothesRepository,
            outfitRepository = outfitRepository,
            ttsManager = tts,
            hapticManager = haptics
        )
    }

    @Test
    fun uiState_populatesHistoryAndRarelyWornRotationCandidates() = runTest {
        val state = viewModel.uiState.value
        assertEquals(1, state.historyEntries.size)
        assertEquals(22, state.totalWardrobeCount)
        assertTrue(state.rarelyWornItems.isNotEmpty())

        viewModel.speakHistoryAndRotationSummary()
        assertTrue(tts.lastSpeech.orEmpty().contains("Kerja"))
        assertTrue(tts.lastSpeech.orEmpty().contains("rotasi", ignoreCase = true))
    }

    @Test
    fun deleteAndClearHistory_updatesUiStateAndAnnounces() = runTest {
        val entry = viewModel.uiState.value.historyEntries.first()
        viewModel.deleteHistoryEntry(entry)
        assertEquals(0, viewModel.uiState.value.historyEntries.size)
        assertTrue(tts.lastSpeech.orEmpty().contains("dihapus", ignoreCase = true))
    }
}
