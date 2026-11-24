package com.zuhdiarifazizi.projectakhirpapb.ui.home

import androidx.lifecycle.ViewModel
import com.zuhdiarifazizi.projectakhirpapb.domain.model.ActivityCategory
import com.zuhdiarifazizi.projectakhirpapb.domain.model.Occasion
import com.zuhdiarifazizi.projectakhirpapb.domain.repository.ActivityCategoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class HomeViewModel(
    private val activityCategoryRepository: ActivityCategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
    }

    private fun loadCategories() {
        val list = activityCategoryRepository.getCategories()
        _uiState.update { it.copy(categories = list) }
    }

    fun selectCategory(category: ActivityCategory) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun selectCategoryByOccasion(occasion: Occasion): ActivityCategory? {
        val category = activityCategoryRepository.getCategoryByOccasion(occasion)
        if (category != null) {
            _uiState.update { it.copy(selectedCategory = category) }
        }
        return category
    }

    fun setVoiceListening(isListening: Boolean) {
        _uiState.update { it.copy(isVoiceListening = isListening) }
    }

    fun setFeedbackMessage(message: String?) {
        _uiState.update { it.copy(feedbackMessage = message) }
    }
}
