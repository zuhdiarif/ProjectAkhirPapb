package com.zuhdiarifazizi.projectakhirpapb.ui.onboarding

import com.zuhdiarifazizi.projectakhirpapb.data.repository.InMemoryOnboardingRepository
import com.zuhdiarifazizi.projectakhirpapb.ui.home.FakeHapticFeedbackManager
import com.zuhdiarifazizi.projectakhirpapb.ui.home.FakeTextToSpeechManager
import com.zuhdiarifazizi.projectakhirpapb.util.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class OnboardingViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun freshStart_showsFirstSlideAndSpeaksIntro() = runTest {
        val repo = InMemoryOnboardingRepository(initialCompleted = false)
        val tts = FakeTextToSpeechManager()
        val haptics = FakeHapticFeedbackManager()

        val viewModel = OnboardingViewModel(repo, tts, haptics)
        val state = viewModel.uiState.value

        assertFalse(state.isCheckingStatus)
        assertFalse(state.isCompleted)
        assertEquals(0, state.currentPage)
        assertEquals(3, state.slides.size)
        assertTrue(tts.lastSpeech.orEmpty().contains("Selamat Datang di AURA"))
    }

    @Test
    fun nextPageProgressesSlidesAndPersistsCompletionOnLastSlide() = runTest {
        val repo = InMemoryOnboardingRepository(initialCompleted = false)
        val tts = FakeTextToSpeechManager()
        val haptics = FakeHapticFeedbackManager()
        val viewModel = OnboardingViewModel(repo, tts, haptics)

        var finished = false
        viewModel.nextPage { finished = true }
        assertEquals(1, viewModel.uiState.value.currentPage)
        assertFalse(finished)

        viewModel.nextPage { finished = true }
        assertEquals(2, viewModel.uiState.value.currentPage)
        assertFalse(finished)

        viewModel.nextPage { finished = true }
        assertTrue(finished)
        assertTrue(viewModel.uiState.value.isCompleted)
        assertTrue(repo.isOnboardingCompleted())
    }

    @Test
    fun completedUserReopening_resetsSlideIndexToZero() = runTest {
        val repo = InMemoryOnboardingRepository(initialCompleted = true)
        val tts = FakeTextToSpeechManager()
        val viewModel = OnboardingViewModel(repo, tts)

        assertTrue(viewModel.uiState.value.isCompleted)
        viewModel.reopenOnboarding()
        assertEquals(0, viewModel.uiState.value.currentPage)
        assertTrue(tts.lastSpeech.orEmpty().contains("Selamat Datang di AURA"))
    }
}
