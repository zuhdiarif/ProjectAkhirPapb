package com.zuhdiarifazizi.projectakhirpapb.domain.scanner

import com.zuhdiarifazizi.projectakhirpapb.data.classifier.HsvColorExtractor
import kotlin.math.abs
import kotlin.math.roundToInt

data class LumaChromaGrid(
    val width: Int,
    val height: Int,
    val yLuma: IntArray,
    val centerRgbPixels: IntArray,
    val centerWidth: Int,
    val centerHeight: Int
)

enum class ScannerReadiness {
    LOW_LIGHT,
    NOT_LEVEL,
    NO_GARMENT,
    TOO_FAR,
    TOO_CLOSE_OR_CROPPED,
    READY_HOLD_STEADY
}

enum class ScannerHapticCue {
    TICK,
    WARNING,
    LOCKED_CAPTURE
}

data class ScannerFrameGuidance(
    val lightPercent: Int,
    val needsTorch: Boolean,
    val readiness: ScannerReadiness,
    val stepNumber: Int,
    val stepLabel: String,
    val detectedTitle: String,
    val guidanceText: String,
    val spokenCue: String?,
    val hapticCue: ScannerHapticCue?,
    val isReadyForAutoCapture: Boolean,
    val shouldTriggerAutoCapture: Boolean
)

/**
 * Evaluates live video frames + device tilt state for blind-accessible garment scanning,
 * emitting throttled Indonesian audio instructions, lighting metrics, and auto-capture triggers.
 */
class GarmentFrameAnalyzer(
    private val holdDurationMillis: Long = 1_500L,
    private val speechCooldownMillis: Long = 2_800L,
    private val hsvColorExtractor: HsvColorExtractor = HsvColorExtractor()
) {
    private var readySinceMillis: Long? = null
    private var hasFiredAutoCapture = false
    private var lastSpokenMillis: Long = -speechCooldownMillis
    private var lastSpokenReadiness: ScannerReadiness? = null

    fun reset() {
        readySinceMillis = null
        hasFiredAutoCapture = false
        lastSpokenMillis = -speechCooldownMillis
        lastSpokenReadiness = null
    }

    fun evaluateFrame(
        grid: LumaChromaGrid,
        isDeviceLevel: Boolean,
        tiltHint: String = "",
        nowMillis: Long = System.currentTimeMillis()
    ): ScannerFrameGuidance {
        val avgLuma = if (grid.yLuma.isEmpty()) 0.0 else grid.yLuma.average()
        val lightPercent = ((avgLuma / 255.0) * 100.0).roundToInt().coerceIn(0, 100)
        val needsTorch = lightPercent < LOW_LIGHT_THRESHOLD_PERCENT

        val liveColor = hsvColorExtractor.extractFromRgbPixels(
            grid.centerRgbPixels,
            grid.centerWidth,
            grid.centerHeight
        )
        val detectedTitle = "Pakaian ${liveColor.colorName}"

        val readiness = classifyReadiness(
            grid = grid,
            isDeviceLevel = isDeviceLevel,
            needsTorch = needsTorch
        )

        val (stepNumber, guidanceText) = when (readiness) {
            ScannerReadiness.LOW_LIGHT -> 1 to "Cahaya redup ($lightPercent%). Menyalakan lampu bantu."
            ScannerReadiness.NO_GARMENT -> 2 to "Belum ada pakaian terdeteksi di tengah frame."
            ScannerReadiness.TOO_FAR -> 3 to "Pakaian terlihat jauh. Turunkan ponsel sedikit."
            ScannerReadiness.TOO_CLOSE_OR_CROPPED -> 3 to "Terlalu dekat atau terpotong. Angkat ponsel sedikit."
            ScannerReadiness.NOT_LEVEL -> 4 to tiltHint.ifBlank { "Pegang ponsel sejajar datar di atas pakaian." }
            ScannerReadiness.READY_HOLD_STEADY -> 4 to "Posisi terdeteksi sejajar. Tahan sebentar untuk identifikasi."
        }

        var shouldTriggerAutoCapture = false
        var hapticCue: ScannerHapticCue? = null

        if (readiness == ScannerReadiness.READY_HOLD_STEADY) {
            val start = readySinceMillis ?: nowMillis.also { readySinceMillis = it }
            if (!hasFiredAutoCapture && nowMillis - start >= holdDurationMillis) {
                hasFiredAutoCapture = true
                shouldTriggerAutoCapture = true
                hapticCue = ScannerHapticCue.LOCKED_CAPTURE
            } else if (lastSpokenReadiness != readiness) {
                hapticCue = ScannerHapticCue.TICK
            }
        } else {
            readySinceMillis = null
            hasFiredAutoCapture = false
            if (lastSpokenReadiness != readiness && readiness != ScannerReadiness.NO_GARMENT) {
                hapticCue = ScannerHapticCue.WARNING
            }
        }

        val shouldSpeak = shouldTriggerAutoCapture ||
            readiness != lastSpokenReadiness ||
            (nowMillis - lastSpokenMillis >= speechCooldownMillis)

        val spokenCue = if (shouldSpeak) {
            lastSpokenMillis = nowMillis
            lastSpokenReadiness = readiness
            if (shouldTriggerAutoCapture) {
                "Posisi stabil. Mengambil foto $detectedTitle sekarang."
            } else {
                guidanceText
            }
        } else {
            null
        }

        return ScannerFrameGuidance(
            lightPercent = lightPercent,
            needsTorch = needsTorch,
            readiness = readiness,
            stepNumber = stepNumber,
            stepLabel = "Langkah $stepNumber / 4",
            detectedTitle = detectedTitle,
            guidanceText = guidanceText,
            spokenCue = spokenCue,
            hapticCue = hapticCue,
            isReadyForAutoCapture = readiness == ScannerReadiness.READY_HOLD_STEADY,
            shouldTriggerAutoCapture = shouldTriggerAutoCapture
        )
    }

    private fun classifyReadiness(
        grid: LumaChromaGrid,
        isDeviceLevel: Boolean,
        needsTorch: Boolean
    ): ScannerReadiness {
        if (needsTorch) return ScannerReadiness.LOW_LIGHT
        val w = grid.width
        val h = grid.height
        if (w < 4 || h < 4 || grid.yLuma.size < w * h) {
            return if (isDeviceLevel) ScannerReadiness.NO_GARMENT else ScannerReadiness.NOT_LEVEL
        }

        val corners = intArrayOf(
            grid.yLuma[0],
            grid.yLuma[w - 1],
            grid.yLuma[(h - 1) * w],
            grid.yLuma[h * w - 1]
        )
        val cornerMean = corners.average()

        var foregroundCount = 0
        var topFg = 0
        var bottomFg = 0
        var leftFg = 0
        var rightFg = 0

        for (y in 0 until h) {
            for (x in 0 until w) {
                val diff = abs(grid.yLuma[y * w + x] - cornerMean)
                if (diff >= FOREGROUND_DIFF) {
                    foregroundCount++
                    if (y == 0) topFg++
                    if (y == h - 1) bottomFg++
                    if (x == 0) leftFg++
                    if (x == w - 1) rightFg++
                }
            }
        }

        val totalCells = (w * h).toFloat()
        val fgRatio = foregroundCount / totalCells
        val touchingSides = listOf(
            topFg > w / 2,
            bottomFg > w / 2,
            leftFg > h / 2,
            rightFg > h / 2
        ).count { it }

        return when {
            fgRatio < 0.08f -> ScannerReadiness.NO_GARMENT
            fgRatio < 0.18f -> ScannerReadiness.TOO_FAR
            touchingSides >= 3 && fgRatio > 0.78f -> ScannerReadiness.TOO_CLOSE_OR_CROPPED
            !isDeviceLevel -> ScannerReadiness.NOT_LEVEL
            else -> ScannerReadiness.READY_HOLD_STEADY
        }
    }

    private companion object {
        const val LOW_LIGHT_THRESHOLD_PERCENT = 30
        const val FOREGROUND_DIFF = 18.0
    }
}
