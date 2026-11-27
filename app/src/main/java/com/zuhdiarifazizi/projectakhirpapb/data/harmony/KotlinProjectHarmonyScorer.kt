package com.zuhdiarifazizi.projectakhirpapb.data.harmony

import com.zuhdiarifazizi.projectakhirpapb.domain.model.HarmonyPrediction
import com.zuhdiarifazizi.projectakhirpapb.domain.model.ProjectHarmonyFeatureEncoder
import com.zuhdiarifazizi.projectakhirpapb.domain.repository.ProjectHarmonyScorer
import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingItem
import kotlin.math.max

/**
 * Pure Kotlin inference engine mirroring `project_harmony.tflite` weights (`22 -> 48 -> 24 -> 7`).
 * Used for deterministic JVM unit testing and resilient instant fallback on Android.
 */
class KotlinProjectHarmonyScorer : ProjectHarmonyScorer {

    override fun evaluate(
        top: ClothingItem,
        bottom: ClothingItem,
        outer: ClothingItem?
    ): HarmonyPrediction {
        val features = ProjectHarmonyFeatureEncoder.encode(top, bottom, outer)
        val logits = inferLogits(features)
        return ProjectHarmonyFeatureEncoder.decodeLogits(
            logits = logits,
            backendName = "Project Harmony ML (Kotlin)"
        )
    }

    fun inferLogits(input: FloatArray): FloatArray {
        require(input.size == ProjectHarmonyWeights.INPUT_SIZE) {
            "Expected ${ProjectHarmonyWeights.INPUT_SIZE} input features, got ${input.size}"
        }
        val h1 = denseLayer(
            input = input,
            weights = ProjectHarmonyWeights.W1,
            bias = ProjectHarmonyWeights.B1,
            inSize = ProjectHarmonyWeights.INPUT_SIZE,
            outSize = ProjectHarmonyWeights.HIDDEN_1,
            relu = true
        )
        val h2 = denseLayer(
            input = h1,
            weights = ProjectHarmonyWeights.W2,
            bias = ProjectHarmonyWeights.B2,
            inSize = ProjectHarmonyWeights.HIDDEN_1,
            outSize = ProjectHarmonyWeights.HIDDEN_2,
            relu = true
        )
        return denseLayer(
            input = h2,
            weights = ProjectHarmonyWeights.W3,
            bias = ProjectHarmonyWeights.B3,
            inSize = ProjectHarmonyWeights.HIDDEN_2,
            outSize = ProjectHarmonyWeights.OUTPUT_SIZE,
            relu = false
        )
    }

    private fun denseLayer(
        input: FloatArray,
        weights: FloatArray,
        bias: FloatArray,
        inSize: Int,
        outSize: Int,
        relu: Boolean
    ): FloatArray {
        val out = FloatArray(outSize)
        for (o in 0 until outSize) {
            var acc = bias[o]
            val rowOffset = o * inSize
            for (i in 0 until inSize) {
                acc += input[i] * weights[rowOffset + i]
            }
            out[o] = if (relu) max(0f, acc) else acc
        }
        return out
    }
}
