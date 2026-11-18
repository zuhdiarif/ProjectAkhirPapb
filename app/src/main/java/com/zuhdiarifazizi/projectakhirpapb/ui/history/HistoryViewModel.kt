package com.zuhdiarifazizi.projectakhirpapb.ui.history

import androidx.lifecycle.ViewModel
import com.zuhdiarifazizi.projectakhirpapb.domain.repository.ClothesRepository
import com.zuhdiarifazizi.projectakhirpapb.domain.repository.OutfitRepository
import com.zuhdiarifazizi.projectakhirpapb.domain.usecase.AnalyzeOutfitRotationUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HistoryViewModel(
    private val clothesRepository: ClothesRepository,
    private val outfitRepository: OutfitRepository,
    private val analyzeOutfitRotationUseCase: AnalyzeOutfitRotationUseCase = AnalyzeOutfitRotationUseCase()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()
}
