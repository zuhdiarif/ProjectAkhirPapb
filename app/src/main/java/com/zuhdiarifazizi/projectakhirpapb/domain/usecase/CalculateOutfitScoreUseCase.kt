package com.zuhdiarifazizi.projectakhirpapb.domain.usecase

import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingCategory
import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingItem
import com.zuhdiarifazizi.projectakhirpapb.domain.model.Occasion
import com.zuhdiarifazizi.projectakhirpapb.domain.model.OutfitCandidate

class CalculateOutfitScoreUseCase {

    operator fun invoke(
        clothes: List<ClothingItem>,
        targetOccasion: Occasion,
        selectedCategories: Set<ClothingCategory>,
        recentlyWornSignatures: Set<String> = emptySet()
    ): List<OutfitCandidate> {
        return emptyList()
    }
}
