package com.zuhdiarifazizi.projectakhirpapb.ui.manageclothes

import androidx.lifecycle.ViewModel
import com.zuhdiarifazizi.projectakhirpapb.domain.classifier.ClothingClassifier
import com.zuhdiarifazizi.projectakhirpapb.domain.repository.ClothesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ManageClothesViewModel(
    private val clothesRepository: ClothesRepository,
    private val classifier: ClothingClassifier
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManageClothesUiState())
    val uiState: StateFlow<ManageClothesUiState> = _uiState.asStateFlow()
}
