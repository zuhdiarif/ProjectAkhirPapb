package com.zuhdiarifazizi.projectakhirpapb.domain.scanner

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GarmentFrameAnalyzerTest {

    @Test
    fun evaluateFrame_detectsLowLightAndRequestsTorch() {
        val analyzer = GarmentFrameAnalyzer()
        val darkGrid = uniformGrid(lumaValue = 40)

        val result = analyzer.evaluateFrame(darkGrid, isDeviceLevel = true, nowMillis = 1_000L)

        assertEquals(ScannerReadiness.LOW_LIGHT, result.readiness)
        assertTrue(result.needsTorch)
        assertEquals(1, result.stepNumber)
        assertFalse(result.shouldTriggerAutoCapture)
    }

    @Test
    fun evaluateFrame_detectsNoGarmentTooFarTooCloseAndNotLevel() {
        val analyzer = GarmentFrameAnalyzer()

        val emptyRes = analyzer.evaluateFrame(uniformGrid(lumaValue = 200), isDeviceLevel = true, nowMillis = 1_000L)
        assertEquals(ScannerReadiness.NO_GARMENT, emptyRes.readiness)
        assertEquals(2, emptyRes.stepNumber)

        val farRes = analyzer.evaluateFrame(centeredBoxGrid(minCoord = 3, maxCoord = 5, lumaBg = 220, lumaFg = 90), isDeviceLevel = true, nowMillis = 2_000L)
        assertEquals(ScannerReadiness.TOO_FAR, farRes.readiness)
        assertEquals(3, farRes.stepNumber)

        val croppedGrid = LumaChromaGrid(
            width = 8,
            height = 8,
            yLuma = IntArray(64) { index ->
                val x = index % 8
                val y = index / 8
                val isCorner = (x == 0 || x == 7) && (y == 0 || y == 7)
                if (isCorner) 220 else 90
            },
            centerRgbPixels = IntArray(16) { 0xFF10204A.toInt() },
            centerWidth = 4,
            centerHeight = 4
        )
        val croppedRes = analyzer.evaluateFrame(croppedGrid, isDeviceLevel = true, nowMillis = 3_000L)
        assertEquals(ScannerReadiness.TOO_CLOSE_OR_CROPPED, croppedRes.readiness)

        val tiltedRes = analyzer.evaluateFrame(
            grid = centeredBoxGrid(minCoord = 2, maxCoord = 5, lumaBg = 220, lumaFg = 90),
            isDeviceLevel = false,
            tiltHint = "Miringkan ke kiri",
            nowMillis = 4_000L
        )
        assertEquals(ScannerReadiness.NOT_LEVEL, tiltedRes.readiness)
        assertEquals("Miringkan ke kiri", tiltedRes.guidanceText)
        assertEquals(ScannerHapticCue.WARNING, tiltedRes.hapticCue)
    }

    @Test
    fun evaluateFrame_readyHoldDurationThrottlesSpeechFiresAutoCaptureOnceAndResetsCleanly() {
        val analyzer = GarmentFrameAnalyzer(holdDurationMillis = 1_500L, speechCooldownMillis = 2_800L)
        val readyGrid = centeredBoxGrid(minCoord = 2, maxCoord = 5, lumaBg = 220, lumaFg = 90)

        val t0 = analyzer.evaluateFrame(readyGrid, isDeviceLevel = true, nowMillis = 1_000L)
        assertEquals(ScannerReadiness.READY_HOLD_STEADY, t0.readiness)
        assertTrue(t0.isReadyForAutoCapture)
        assertFalse(t0.shouldTriggerAutoCapture)
        assertEquals(ScannerHapticCue.TICK, t0.hapticCue)
        assertNotNull(t0.spokenCue)

        val t1 = analyzer.evaluateFrame(readyGrid, isDeviceLevel = true, nowMillis = 2_000L)
        assertFalse(t1.shouldTriggerAutoCapture)
        assertNull(t1.spokenCue)

        val t2 = analyzer.evaluateFrame(readyGrid, isDeviceLevel = true, nowMillis = 2_600L)
        assertTrue(t2.shouldTriggerAutoCapture)
        assertEquals(ScannerHapticCue.LOCKED_CAPTURE, t2.hapticCue)
        assertTrue(t2.spokenCue.orEmpty().contains("Posisi stabil"))

        val t3 = analyzer.evaluateFrame(readyGrid, isDeviceLevel = true, nowMillis = 3_000L)
        assertFalse(t3.shouldTriggerAutoCapture)

        analyzer.reset()
        val afterResetStart = analyzer.evaluateFrame(readyGrid, isDeviceLevel = true, nowMillis = 5_000L)
        assertFalse(afterResetStart.shouldTriggerAutoCapture)
        val afterResetHold = analyzer.evaluateFrame(readyGrid, isDeviceLevel = true, nowMillis = 6_600L)
        assertTrue(afterResetHold.shouldTriggerAutoCapture)
    }

    private fun uniformGrid(lumaValue: Int): LumaChromaGrid = LumaChromaGrid(
        width = 8,
        height = 8,
        yLuma = IntArray(64) { lumaValue },
        centerRgbPixels = IntArray(16) { 0xFF10204A.toInt() },
        centerWidth = 4,
        centerHeight = 4
    )

    private fun centeredBoxGrid(minCoord: Int, maxCoord: Int, lumaBg: Int, lumaFg: Int): LumaChromaGrid = LumaChromaGrid(
        width = 8,
        height = 8,
        yLuma = IntArray(64) { index ->
            val x = index % 8
            val y = index / 8
            if (x in minCoord..maxCoord && y in minCoord..maxCoord) lumaFg else lumaBg
        },
        centerRgbPixels = IntArray(16) { 0xFF10204A.toInt() },
        centerWidth = 4,
        centerHeight = 4
    )
}
