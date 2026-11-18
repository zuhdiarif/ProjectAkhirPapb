package com.zuhdiarifazizi.projectakhirpapb.ui.pickoutfit

import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingCategory
import com.zuhdiarifazizi.projectakhirpapb.domain.model.Occasion
import com.zuhdiarifazizi.projectakhirpapb.domain.model.OutfitCandidate

data class PickOutfitUiState(
    val selectedOccasion: Occasion? = null,
    val selectedCategories: Set<ClothingCategory> = emptySet(),
    val candidates: List<OutfitCandidate> = emptyList()
)
