package com.zuhdiarifazizi.projectakhirpapb.domain.repository

import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingItem
import com.zuhdiarifazizi.projectakhirpapb.domain.model.HarmonyPrediction
import com.zuhdiarifazizi.projectakhirpapb.domain.model.ProjectHarmonyFeatureEncoder
import kotlin.math.abs

fun interface ProjectHarmonyScorer {
    fun evaluate(
        top: ClothingItem,
        bottom: ClothingItem,
        outer: ClothingItem?
    ): HarmonyPrediction
}

object RuleBasedHarmonyScorer : ProjectHarmonyScorer {
    override fun evaluate(
        top: ClothingItem,
        bottom: ClothingItem,
        outer: ClothingItem?
    ): HarmonyPrediction {
        val items = listOfNotNull(top, bottom, outer)
        val chromatics = items.filterNot(ProjectHarmonyFeatureEncoder::isNeutral)
        val (score, label) = when {
            chromatics.isEmpty() -> 0.90 to ProjectHarmonyFeatureEncoder.HARMONY_LABELS[0]
            chromatics.size == 1 -> 0.88 to ProjectHarmonyFeatureEncoder.HARMONY_LABELS[1]
            else -> {
                val hueDiff = ProjectHarmonyFeatureEncoder.hueDifferenceDegrees(chromatics[0], chromatics[1])
                when {
                    hueDiff <= 22f -> 0.86 to ProjectHarmonyFeatureEncoder.HARMONY_LABELS[2]
                    hueDiff <= 55f -> 0.84 to ProjectHarmonyFeatureEncoder.HARMONY_LABELS[3]
                    abs(hueDiff - 180f) <= 28f -> 0.82 to ProjectHarmonyFeatureEncoder.HARMONY_LABELS[4]
                    else -> 0.68 to ProjectHarmonyFeatureEncoder.HARMONY_LABELS[5]
                }
            }
        }
        return HarmonyPrediction(
            colorScore = score,
            harmonyType = label,
            confidence = 0.85f,
            backendName = "Rule Harmony (Domain)"
        )
    }
}
