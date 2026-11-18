package com.zuhdiarifazizi.projectakhirpapb.ui.pickoutfit

import androidx.lifecycle.ViewModel
import com.zuhdiarifazizi.projectakhirpapb.domain.repository.ClothesRepository
import com.zuhdiarifazizi.projectakhirpapb.domain.repository.OutfitRepository
import com.zuhdiarifazizi.projectakhirpapb.domain.usecase.CalculateOutfitScoreUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PickOutfitViewModel(
    private val clothesRepository: ClothesRepository,
    private val outfitRepository: OutfitRepository,
    private val calculateOutfitScoreUseCase: CalculateOutfitScoreUseCase = CalculateOutfitScoreUseCase()
) : ViewModel() {

    private val _uiState = MutableStateFlow(PickOutfitUiState())
    val uiState: StateFlow<PickOutfitUiState> = _uiState.asStateFlow()
}
