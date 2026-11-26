package com.zuhdiarifazizi.projectakhirpapb.domain.classifier

import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingCategory
import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingPattern
import com.zuhdiarifazizi.projectakhirpapb.domain.model.HsvColor
import com.zuhdiarifazizi.projectakhirpapb.domain.model.Occasion

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

interface ClothingClassifier {
    suspend fun classify(imageBytes: ByteArray): Result<ClassificationResult>
}
