package com.zuhdiarifazizi.projectakhirpapb.domain.model

data class OutfitCandidate(
    val id: String,
    val topWear: ClothingItem? = null,
    val bottomWear: ClothingItem? = null,
    val outerWear: ClothingItem? = null,
    val occasion: Occasion,
    val colorScore: Double,
    val occasionScore: Double,
    val historyPenalty: Double,
    val totalScore: Double,
    val harmonyType: String
)
