package com.zuhdiarifazizi.projectakhirpapb.ui.home

import com.zuhdiarifazizi.projectakhirpapb.data.repository.DefaultActivityCategoryRepository
import com.zuhdiarifazizi.projectakhirpapb.domain.model.Occasion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class HomeViewModelTest {

    private lateinit var viewModel: HomeViewModel

    @Before
    fun setUp() {
        viewModel = HomeViewModel(DefaultActivityCategoryRepository())
    }

    @Test
    fun uiState_initiallyLoadsAllFiveCategories() {
        val state = viewModel.uiState.value
        assertEquals(5, state.categories.size)
        assertEquals("Kerja", state.categories[0].title)
    }

    @Test
    fun selectCategory_updatesSelectedCategoryInState() {
        val target = viewModel.uiState.value.categories[1] // Nongkrong
        viewModel.selectCategory(target)

        val updated = viewModel.uiState.value
        assertEquals(target, updated.selectedCategory)
        assertEquals("Nongkrong", updated.selectedCategory?.title)
    }

    @Test
    fun selectCategoryByOccasion_updatesStateAndReturnsCategory() {
        val result = viewModel.selectCategoryByOccasion(Occasion.EXERCISE)
        assertNotNull(result)
        assertEquals("Olahraga", result?.title)
        assertEquals(viewModel.uiState.value.selectedCategory, result)
    }

    @Test
    fun setVoiceListening_updatesListeningState() {
        viewModel.setVoiceListening(true)
        assertTrue(viewModel.uiState.value.isVoiceListening)
    }
}
