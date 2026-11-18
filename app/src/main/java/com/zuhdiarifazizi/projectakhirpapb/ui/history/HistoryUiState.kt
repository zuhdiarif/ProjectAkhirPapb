package com.zuhdiarifazizi.projectakhirpapb.ui.history

import com.zuhdiarifazizi.projectakhirpapb.domain.model.OutfitHistory
import com.zuhdiarifazizi.projectakhirpapb.domain.model.RotationRecommendation

data class HistoryUiState(
    val historyEntries: List<OutfitHistory> = emptyList(),
    val rotationRecommendations: List<RotationRecommendation> = emptyList()
)
