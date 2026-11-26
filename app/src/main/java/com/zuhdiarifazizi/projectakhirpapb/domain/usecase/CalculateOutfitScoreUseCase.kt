package com.zuhdiarifazizi.projectakhirpapb.domain.usecase

import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingCategory
import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingItem
import com.zuhdiarifazizi.projectakhirpapb.domain.model.Occasion
import com.zuhdiarifazizi.projectakhirpapb.domain.model.OutfitCandidate
import kotlin.math.abs

class CalculateOutfitScoreUseCase {

    operator fun invoke(
        clothes: List<ClothingItem>,
        targetOccasion: Occasion,
        selectedCategories: Set<ClothingCategory>,
        recentlyWornSignatures: Set<String> = emptySet()
    ): List<OutfitCandidate> {
        if (ClothingCategory.TOP_WEAR !in selectedCategories ||
            ClothingCategory.BOTTOM_WEAR !in selectedCategories
        ) return emptyList()

        val tops = clothes.filter { it.category == ClothingCategory.TOP_WEAR }
        val bottoms = clothes.filter { it.category == ClothingCategory.BOTTOM_WEAR }
        if (tops.isEmpty() || bottoms.isEmpty()) return emptyList()

        val availableOuterwear = clothes.filter { it.category == ClothingCategory.OUTER_WEAR }
        val outerOptions: List<ClothingItem?> = if (
            ClothingCategory.OUTER_WEAR in selectedCategories && availableOuterwear.isNotEmpty()
        ) {
            listOf(null) + availableOuterwear
        } else {
            listOf(null)
        }

        return buildList {
            tops.forEach { top ->
                bottoms.forEach { bottom ->
                    outerOptions.forEach { outer ->
                        val occasionScore = (
                            activityFit(top, targetOccasion) * 0.4 +
                                activityFit(bottom, targetOccasion) * 0.4 +
                                (outer?.let { activityFit(it, targetOccasion) } ?: activityFit(top, targetOccasion)) * 0.2
                            ).coerceIn(0.0, 1.0)

                        val colorScores = mutableListOf(colorFit(top, bottom))
                        if (outer != null) {
                            colorScores += colorFit(top, outer)
                            colorScores += colorFit(bottom, outer)
                        }
                        val colorScore = colorScores.average()
                        val signature = signature(top, bottom, outer)
                        val historyPenalty = if (signature in recentlyWornSignatures) 0.18 else 0.0
                        val total = (occasionScore * 0.55 + colorScore * 0.45 - historyPenalty)
                            .coerceIn(0.0, 1.0)

                        add(
                            OutfitCandidate(
                                id = signature,
                                topWear = top,
                                bottomWear = bottom,
                                outerWear = outer,
                                occasion = targetOccasion,
                                colorScore = colorScore,
                                occasionScore = occasionScore,
                                historyPenalty = historyPenalty,
                                totalScore = total,
                                harmonyType = when {
                                    colorScore >= 0.82 -> "Warna netral atau selaras"
                                    else -> "Kontras warna yang seimbang"
                                }
                            )
                        )
                    }
                }
            }
        }.sortedWith(compareByDescending<OutfitCandidate> { it.totalScore }.thenBy { it.id })
    }

    private fun activityFit(item: ClothingItem, target: Occasion): Double = when {
        item.occasion == target -> 1.0
        item.occasion == Occasion.OTHER -> 0.65
        target == Occasion.EXERCISE && item.category == ClothingCategory.TOP_WEAR -> 0.55
        target == Occasion.EXERCISE && item.category == ClothingCategory.BOTTOM_WEAR -> 0.50
        target == Occasion.OTHER -> 0.55
        else -> 0.30
    }

    private fun colorFit(first: ClothingItem, second: ClothingItem): Double {
        val firstNeutral = first.color.saturation < 0.16f || first.color.value < 0.16f
        val secondNeutral = second.color.saturation < 0.16f || second.color.value < 0.16f
        if (firstNeutral && secondNeutral) return 0.86
        if (firstNeutral || secondNeutral) return 0.82

        val rawDifference = abs(first.color.hue - second.color.hue) % 360f
        val difference = minOf(rawDifference, 360f - rawDifference)
        return when {
            difference <= 35f -> 0.95
            difference >= 145f -> 0.90
            difference <= 75f -> 0.78
            else -> 0.62
        }
    }

    private fun signature(top: ClothingItem, bottom: ClothingItem, outer: ClothingItem?): String =
        listOfNotNull(top.id, bottom.id, outer?.id).joinToString("|")
}
