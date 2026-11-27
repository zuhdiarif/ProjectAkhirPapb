package com.zuhdiarifazizi.projectakhirpapb.domain.usecase

import com.zuhdiarifazizi.projectakhirpapb.domain.model.HsvColor
import com.zuhdiarifazizi.projectakhirpapb.domain.model.LumaChromaGrid
import com.zuhdiarifazizi.projectakhirpapb.domain.model.ScannerFrameGuidance
import com.zuhdiarifazizi.projectakhirpapb.domain.model.ScannerHapticCue
import com.zuhdiarifazizi.projectakhirpapb.domain.model.ScannerReadiness
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Evaluates live video frames + gentle device tilt state for blind & color-blind accessible
 * garment scanning. Deliberately forgiving toward folds, close framing, and dark fabrics while
 * speaking calm, well-spaced Indonesian cues without chaotic repetition.
 */
class GarmentFrameAnalyzer(
    private val holdDurationMillis: Long = 1_100L,
    private val speechCooldownMillis: Long = 7_500L,
    private val initialWarmupMillis: Long = 1_400L,
    private val readinessDebounceFrames: Int = 3
) {
    private var firstFrameMillis: Long? = null
    private var readySinceMillis: Long? = null
    private var hasFiredAutoCapture = false
    private var lastSpokenMillis: Long = Long.MIN_VALUE / 4
    private var lastSpokenReadiness: ScannerReadiness? = null
    private var stableReadiness: ScannerReadiness = ScannerReadiness.NO_GARMENT
    private var candidateReadiness: ScannerReadiness? = null
    private var candidateFrameCount: Int = 0
    private var previousLuma: IntArray? = null

    fun reset() {
        firstFrameMillis = null
        readySinceMillis = null
        hasFiredAutoCapture = false
        lastSpokenMillis = Long.MIN_VALUE / 4
        lastSpokenReadiness = null
        stableReadiness = ScannerReadiness.NO_GARMENT
        candidateReadiness = null
        candidateFrameCount = 0
        previousLuma = null
    }

    fun evaluateFrame(
        grid: LumaChromaGrid,
        isDeviceLevel: Boolean,
        tiltHint: String = "",
        nowMillis: Long = System.currentTimeMillis()
    ): ScannerFrameGuidance {
        val startEpoch = firstFrameMillis ?: nowMillis.also { firstFrameMillis = it }
        val avgLuma = if (grid.yLuma.isEmpty()) 0.0 else grid.yLuma.average()
        val maxLuma = grid.yLuma.maxOrNull() ?: 0
        val lightPercent = ((avgLuma / 255.0) * 100.0).roundToInt().coerceIn(0, 100)
        val isPitchDark = lightPercent < PITCH_DARK_PERCENT && maxLuma < PITCH_DARK_MAX_LUMA
        val needsTorch = lightPercent < TORCH_SUGGEST_PERCENT && maxLuma < 85

        val motionDelta = computeMotionDelta(grid.yLuma)
        previousLuma = grid.yLuma.copyOf()

        val liveColor = HsvColor.fromRgbPixels(
            grid.centerRgbPixels,
            grid.centerWidth,
            grid.centerHeight
        )
        val detectedTitle = "Pakaian ${liveColor.colorName}"

        val instantReadiness = classifyReadiness(
            grid = grid,
            isDeviceLevel = isDeviceLevel,
            isPitchDark = isPitchDark,
            motionDelta = motionDelta
        )
        val readiness = debounceReadiness(instantReadiness)

        val (stepNumber, guidanceText) = when (readiness) {
            ScannerReadiness.LOW_LIGHT -> 1 to "Ruangan gelap. Menyalakan lampu bantu agar warna lebih jelas."
            ScannerReadiness.NOT_LEVEL -> 2 to tiltHint.ifBlank { "Arahkan ponsel lebih menghadap ke pakaian." }
            ScannerReadiness.TOO_FAR -> 3 to "Pakaian bergerak. Tahan ponsel sebentar agar gambar tidak buram."
            ScannerReadiness.NO_GARMENT -> 2 to "Arahkan kamera ke bagian tengah pakaian."
            ScannerReadiness.TOO_CLOSE_OR_CROPPED -> 3 to "Posisi cukup dekat. Tahan sebentar."
            ScannerReadiness.READY_HOLD_STEADY -> 4 to "Bagus, posisi sudah pas. Tahan sebentar untuk memfoto."
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
        }

        val pastWarmup = (nowMillis - startEpoch) >= initialWarmupMillis
        val cooldownElapsed = (nowMillis - lastSpokenMillis) >= speechCooldownMillis
        val shouldSpeak = shouldTriggerAutoCapture ||
            (pastWarmup && cooldownElapsed && readiness != lastSpokenReadiness)

        val spokenCue = if (shouldSpeak) {
            lastSpokenMillis = nowMillis
            lastSpokenReadiness = readiness
            if (shouldTriggerAutoCapture) {
                "Foto berhasil diambil. $detectedTitle."
            } else {
                if (readiness != ScannerReadiness.READY_HOLD_STEADY && hapticCue == null) {
                    hapticCue = ScannerHapticCue.WARNING
                }
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

    private fun debounceReadiness(instant: ScannerReadiness): ScannerReadiness {
        if (instant == stableReadiness) {
            candidateReadiness = null
            candidateFrameCount = 0
            return stableReadiness
        }
        if (candidateReadiness == instant) {
            candidateFrameCount++
        } else {
            candidateReadiness = instant
            candidateFrameCount = 1
        }
        if (candidateFrameCount >= readinessDebounceFrames) {
            stableReadiness = instant
            candidateReadiness = null
            candidateFrameCount = 0
        }
        return stableReadiness
    }

    private fun computeMotionDelta(current: IntArray): Double {
        val prev = previousLuma ?: return 0.0
        if (prev.size != current.size || current.isEmpty()) return 0.0
        var diffSum = 0L
        for (i in current.indices) {
            diffSum += abs(current[i] - prev[i])
        }
        return diffSum.toDouble() / current.size
    }

    private fun classifyReadiness(
        grid: LumaChromaGrid,
        isDeviceLevel: Boolean,
        isPitchDark: Boolean,
        motionDelta: Double
    ): ScannerReadiness {
        if (isPitchDark) return ScannerReadiness.LOW_LIGHT
        if (grid.yLuma.isEmpty()) {
            return if (isDeviceLevel) ScannerReadiness.NO_GARMENT else ScannerReadiness.NOT_LEVEL
        }
        if (!isDeviceLevel) return ScannerReadiness.NOT_LEVEL
        if (motionDelta > MAX_STEADY_MOTION_DELTA) return ScannerReadiness.TOO_FAR

        val minL = grid.yLuma.minOrNull() ?: 0
        val maxL = grid.yLuma.maxOrNull() ?: 0
        val dynamicRange = maxL - minL
        if (dynamicRange < MIN_SCENE_VARIATION && grid.yLuma.average() < 25.0) {
            return ScannerReadiness.LOW_LIGHT
        }
        return ScannerReadiness.READY_HOLD_STEADY
    }

    private companion object {
        const val PITCH_DARK_PERCENT = 8
        const val PITCH_DARK_MAX_LUMA = 45
        const val TORCH_SUGGEST_PERCENT = 18
        const val MAX_STEADY_MOTION_DELTA = 22.0
        const val MIN_SCENE_VARIATION = 4
    }
}
