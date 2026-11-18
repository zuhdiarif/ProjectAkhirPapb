package com.zuhdiarifazizi.projectakhirpapb.domain.usecase

import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingItem
import com.zuhdiarifazizi.projectakhirpapb.domain.model.OutfitHistory
import com.zuhdiarifazizi.projectakhirpapb.domain.model.OutfitHistoryAnalysis

class AnalyzeOutfitRotationUseCase {

    operator fun invoke(
        clothes: List<ClothingItem>,
        historyEntries: List<OutfitHistory>,
        nowEpochMillis: Long = System.currentTimeMillis()
    ): OutfitHistoryAnalysis {
        return OutfitHistoryAnalysis(
            historyLast30Days = emptyList(),
            itemUsageFrequency = emptyMap(),
            recentlyWornOutfitSignatures = emptySet(),
            rotationCandidates = emptyList()
        )
    }
}
