package com.zuhdiarifazizi.projectakhirpapb.ui.history

import com.zuhdiarifazizi.projectakhirpapb.domain.model.OutfitHistory
import com.zuhdiarifazizi.projectakhirpapb.domain.model.RotationRecommendation

data class HistoryUiState(
    val isLoading: Boolean = true,
    val totalWardrobeCount: Int = 0,
    val historyEntries: List<OutfitHistory> = emptyList(),
    val rotationRecommendations: List<RotationRecommendation> = emptyList(),
    val rarelyWornItems: List<RotationRecommendation> = emptyList(),
    val statusMessage: String? = null,
    val showClearConfirmation: Boolean = false
)
