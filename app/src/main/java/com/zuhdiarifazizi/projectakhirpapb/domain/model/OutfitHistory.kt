package com.zuhdiarifazizi.projectakhirpapb.domain.model

data class OutfitHistory(
    val id: String,
    val userId: String,
    val outfitId: String,
    val items: List<ClothingItem>,
    val occasion: Occasion,
    val score: Double,
    val wornAtEpochMillis: Long
)

data class RotationRecommendation(
    val item: ClothingItem,
    val usageCountLast30Days: Int,
    val isRarelyWorn: Boolean
)

data class OutfitHistoryAnalysis(
    val historyLast30Days: List<OutfitHistory>,
    val itemUsageFrequency: Map<String, Int>,
    val recentlyWornOutfitSignatures: Set<String>,
    val rotationCandidates: List<RotationRecommendation>
)
