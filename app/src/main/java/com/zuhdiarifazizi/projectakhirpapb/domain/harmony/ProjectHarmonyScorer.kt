package com.zuhdiarifazizi.projectakhirpapb.domain.harmony

import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingItem
import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingPattern
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin

data class HarmonyPrediction(
    val colorScore: Double,
    val harmonyType: String,
    val confidence: Float,
    val backendName: String
)

fun interface ProjectHarmonyScorer {
    fun evaluate(
        top: ClothingItem,
        bottom: ClothingItem,
        outer: ClothingItem?
    ): HarmonyPrediction
}

object ProjectHarmonyFeatureEncoder {
    const val INPUT_SIZE = 22
    const val OUTPUT_SIZE = 7
    const val HARMONY_CLASS_COUNT = 6

    val HARMONY_LABELS = listOf(
        "Harmoni Netral Klasik",
        "Paduan Netral Seimbang",
        "Harmoni Monokromatik",
        "Harmoni Analogus",
        "Harmoni Komplementer",
        "Kontras Warna Seimbang"
    )

    fun encode(
        top: ClothingItem,
        bottom: ClothingItem,
        outer: ClothingItem?
    ): FloatArray {
        val features = FloatArray(INPUT_SIZE)
        writeGarmentFeatures(features, offset = 0, item = top)
        writeGarmentFeatures(features, offset = 6, item = bottom)
        if (outer != null) {
            writeGarmentFeatures(features, offset = 12, item = outer)
            features[18] = 1f
        }

        val chromatics = listOfNotNull(top, bottom, outer).filterNot(::isNeutral)
        if (chromatics.size >= 2) {
            var minDiff = 180f
            var maxDiff = 0f
            for (i in chromatics.indices) {
                for (j in i + 1 until chromatics.size) {
                    val diff = hueDifferenceDegrees(chromatics[i], chromatics[j])
                    if (diff < minDiff) minDiff = diff
                    if (diff > maxDiff) maxDiff = diff
                }
            }
            features[19] = (minDiff / 180f).coerceIn(0f, 1f)
            features[20] = (maxDiff / 180f).coerceIn(0f, 1f)
        } else {
            features[19] = 0f
            features[20] = 0f
        }

        val topVal = top.color.value.coerceIn(0f, 1f)
        val bottomVal = bottom.color.value.coerceIn(0f, 1f)
        features[21] = abs(topVal - bottomVal)
        return features
    }

    fun decodeLogits(logits: FloatArray, backendName: String): HarmonyPrediction {
        require(logits.size >= OUTPUT_SIZE) {
            "Project Harmony output must have at least $OUTPUT_SIZE elements, got ${logits.size}"
        }
        var maxLogit = Float.NEGATIVE_INFINITY
        for (i in 0 until HARMONY_CLASS_COUNT) {
            if (logits[i] > maxLogit) maxLogit = logits[i]
        }
        var sumExp = 0.0
        val probs = DoubleArray(HARMONY_CLASS_COUNT)
        var bestIdx = 0
        var bestProb = -1.0
        for (i in 0 until HARMONY_CLASS_COUNT) {
            val e = exp((logits[i] - maxLogit).toDouble())
            probs[i] = e
            sumExp += e
        }
        for (i in 0 until HARMONY_CLASS_COUNT) {
            probs[i] /= sumExp
            if (probs[i] > bestProb) {
                bestProb = probs[i]
                bestIdx = i
            }
        }
        val scoreZ = logits[6].toDouble().coerceIn(-20.0, 20.0)
        val colorScore = (1.0 / (1.0 + exp(-scoreZ))).coerceIn(0.0, 1.0)
        return HarmonyPrediction(
            colorScore = colorScore,
            harmonyType = HARMONY_LABELS[bestIdx],
            confidence = bestProb.toFloat().coerceIn(0f, 1f),
            backendName = backendName
        )
    }

    fun isNeutral(item: ClothingItem): Boolean =
        item.color.saturation < 0.16f || item.color.value < 0.16f

    fun patternLoad(pattern: ClothingPattern): Float = when (pattern) {
        ClothingPattern.SOLID -> 0.0f
        ClothingPattern.STRIPED -> 0.5f
        ClothingPattern.PLAID,
        ClothingPattern.FLORAL,
        ClothingPattern.GRAPHIC,
        ClothingPattern.OTHER -> 1.0f
    }

    fun hueDifferenceDegrees(first: ClothingItem, second: ClothingItem): Float {
        val raw = abs(normalizeHue(first.color.hue) - normalizeHue(second.color.hue))
        return minOf(raw, 360f - raw)
    }

    private fun writeGarmentFeatures(target: FloatArray, offset: Int, item: ClothingItem) {
        val hueRad = 2.0 * PI * normalizeHue(item.color.hue) / 360.0
        target[offset] = sin(hueRad).toFloat()
        target[offset + 1] = cos(hueRad).toFloat()
        target[offset + 2] = item.color.saturation.coerceIn(0f, 1f)
        target[offset + 3] = item.color.value.coerceIn(0f, 1f)
        target[offset + 4] = if (isNeutral(item)) 1f else 0f
        target[offset + 5] = patternLoad(item.pattern)
    }

    private fun normalizeHue(hue: Float): Float {
        val mod = hue % 360f
        return if (mod < 0f) mod + 360f else mod
    }
}
