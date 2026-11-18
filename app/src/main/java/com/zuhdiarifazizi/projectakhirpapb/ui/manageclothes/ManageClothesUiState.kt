package com.zuhdiarifazizi.projectakhirpapb.ui.manageclothes

import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingItem

data class ManageClothesUiState(
    val items: List<ClothingItem> = emptyList(),
    val isLoading: Boolean = false
)
