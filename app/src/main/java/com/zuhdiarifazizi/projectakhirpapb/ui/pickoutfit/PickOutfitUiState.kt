package com.zuhdiarifazizi.projectakhirpapb.ui.pickoutfit

import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingCategory
import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingItem
import com.zuhdiarifazizi.projectakhirpapb.domain.model.Occasion
import com.zuhdiarifazizi.projectakhirpapb.domain.model.OutfitCandidate

enum class PickOutfitStep {
    CATEGORY_SELECTION,
    RECOMMENDATIONS,
    RESULT
}

data class PickOutfitUiState(
    val step: PickOutfitStep = PickOutfitStep.CATEGORY_SELECTION,
    val selectedOccasion: Occasion? = null,
    val availableClothes: List<ClothingItem> = emptyList(),
    val selectedCategories: Set<ClothingCategory> = setOf(
        ClothingCategory.TOP_WEAR,
        ClothingCategory.BOTTOM_WEAR
    ),
    val candidates: List<OutfitCandidate> = emptyList(),
    val currentCandidateIndex: Int = 0,
    val selectedCandidate: OutfitCandidate? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val statusMessage: String? = null
)
