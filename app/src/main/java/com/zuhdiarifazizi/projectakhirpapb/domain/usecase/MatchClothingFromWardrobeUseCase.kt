package com.zuhdiarifazizi.projectakhirpapb.domain.usecase

import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingCategory
import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingItem
import com.zuhdiarifazizi.projectakhirpapb.domain.model.HsvColor
import com.zuhdiarifazizi.projectakhirpapb.domain.security.SecurityInputValidator
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class WardrobeVisualMatch(
    val item: ClothingItem,
    val similarityScore: Double
)

/**
 * Matches a physical garment to the stored wardrobe using either its 13.56 MHz RFID/NFC UID
 * or cylindrical HSV + category visual similarity as an AI camera fallback.
 */
class MatchClothingFromWardrobeUseCase {

    fun matchByRfid(clothes: List<ClothingItem>, rawUid: String): ClothingItem? {
        val normalized = SecurityInputValidator.normalizeNfcUidString(rawUid) ?: return null
        return clothes.firstOrNull { item ->
            item.rfidTagUid?.equals(normalized, ignoreCase = true) == true
        }
    }

    fun matchByVisualSimilarity(
        clothes: List<ClothingItem>,
        detectedColor: HsvColor,
        detectedCategory: ClothingCategory? = null
    ): WardrobeVisualMatch? {
        if (clothes.isEmpty()) return null
        return clothes.map { item ->
            val colorSim = hsvSimilarity(item.color, detectedColor)
            val categoryBonus = when {
                detectedCategory == null -> 0.0
                item.category == detectedCategory -> 0.15
                else -> -0.15
            }
            val score = (colorSim + categoryBonus).coerceIn(0.0, 1.0)
            WardrobeVisualMatch(item = item, similarityScore = score)
        }.maxByOrNull { it.similarityScore }
    }

    private fun hsvSimilarity(a: HsvColor, b: HsvColor): Double {
        val radA = a.hue * (PI / 180.0)
        val radB = b.hue * (PI / 180.0)
        val dx = a.saturation * cos(radA) - b.saturation * cos(radB)
        val dy = a.saturation * sin(radA) - b.saturation * sin(radB)
        val dz = a.value - b.value
        val dist = sqrt(dx * dx + dy * dy + dz * dz)
        return (1.0 - (dist / 2.25)).coerceIn(0.0, 1.0)
    }
}
