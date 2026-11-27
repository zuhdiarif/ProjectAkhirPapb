package com.zuhdiarifazizi.projectakhirpapb.domain.model

data class ClassificationResult(
    val suggestedName: String,
    val category: ClothingCategory,
    val pattern: ClothingPattern,
    val occasion: Occasion,
    val dominantColor: HsvColor,
    val confidence: Float,
    val material: String = "Katun Halus",
    val isAiPowered: Boolean = false
)
