package com.zuhdiarifazizi.projectakhirpapb.domain.usecase

import com.zuhdiarifazizi.projectakhirpapb.domain.model.LumaChromaGrid
import com.zuhdiarifazizi.projectakhirpapb.domain.model.ScannerReadiness
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GarmentFrameAnalyzerTest {

    private fun sampleGrid(lumaFill: Int = 95, maxPixel: Int = 120): LumaChromaGrid {
        val size = 16
        val y = IntArray(size * size) { idx ->
            when {
                idx == 0 -> maxPixel
                idx % 2 == 0 -> lumaFill
                else -> (lumaFill + 8).coerceAtMost(255)
            }
        }
        val center = IntArray(64) { 0xFF223344.toInt() }
        return LumaChromaGrid(
            width = size,
            height = size,
            yLuma = y,
            centerRgbPixels = center,
            centerWidth = 8,
            centerHeight = 8
        )
    }

    @Test
    fun darkGarmentWithNormalSpecularMaxLumaIsNotBlockedAsLowLight() {
        val analyzer = GarmentFrameAnalyzer(
            holdDurationMillis = 1_000L,
            speechCooldownMillis = 7_500L,
            initialWarmupMillis = 1_400L,
            readinessDebounceFrames = 2
        )
        // Simulate dark black fabric (avg ~24 = ~9% luma, but room light produces maxLuma=90)
        val darkShirtGrid = sampleGrid(lumaFill = 22, maxPixel = 90)

        var latest = analyzer.evaluateFrame(darkShirtGrid, isDeviceLevel = true, nowMillis = 0L)
        latest = analyzer.evaluateFrame(darkShirtGrid, isDeviceLevel = true, nowMillis = 300L)
        latest = analyzer.evaluateFrame(darkShirtGrid, isDeviceLevel = true, nowMillis = 1_500L)

        assertNotEquals(ScannerReadiness.LOW_LIGHT, latest.readiness)
        assertEquals(ScannerReadiness.READY_HOLD_STEADY, latest.readiness)
        assertTrue(latest.shouldTriggerAutoCapture)
    }

    @Test
    fun speechGuidanceRespectsWarmupAndLongCooldownWithoutSpam() {
        val analyzer = GarmentFrameAnalyzer(
            holdDurationMillis = 5_000L,
            speechCooldownMillis = 7_500L,
            initialWarmupMillis = 1_400L,
            readinessDebounceFrames = 1
        )
        val tiltGrid = sampleGrid()

        // First frame at t=0 starts the 1.4s warmup; at 400ms still silent
        val warmupStart = analyzer.evaluateFrame(tiltGrid, isDeviceLevel = false, nowMillis = 0L)
        assertNull(warmupStart.spokenCue)
        val duringWarmup = analyzer.evaluateFrame(tiltGrid, isDeviceLevel = false, nowMillis = 400L)
        assertNull(duringWarmup.spokenCue)

        // After warmup on first transition to NOT_LEVEL: speaks once cleanly
        val firstCue = analyzer.evaluateFrame(tiltGrid, isDeviceLevel = false, nowMillis = 1_600L)
        assertFalse(firstCue.spokenCue.isNullOrBlank())

        // Quickly shifting states within 7.5s cooldown stays calm and quiet
        val hurriedSwitch = analyzer.evaluateFrame(tiltGrid, isDeviceLevel = true, nowMillis = 3_000L)
        assertNull(hurriedSwitch.spokenCue)

        // Same readiness repeated never spams again even after cooldown
        analyzer.evaluateFrame(tiltGrid, isDeviceLevel = false, nowMillis = 10_000L)
        val suppressedRepeat = analyzer.evaluateFrame(tiltGrid, isDeviceLevel = false, nowMillis = 20_000L)
        assertNull(suppressedRepeat.spokenCue)
    }
}
