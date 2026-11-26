package com.zuhdiarifazizi.projectakhirpapb.data.classifier

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HsvColorExtractorTest {

    private val extractor = HsvColorExtractor()

    @Test
    fun rgbToHsv_computesAccurateCoordinatesForPrimaryAndNeutralColors() {
        val (redH, redS, redV) = extractor.rgbToHsv(255, 0, 0)
        assertEquals(0f, redH, 0.5f)
        assertEquals(1f, redS, 0.01f)
        assertEquals(1f, redV, 0.01f)

        val (greenH, greenS, greenV) = extractor.rgbToHsv(0, 255, 0)
        assertEquals(120f, greenH, 0.5f)
        assertEquals(1f, greenS, 0.01f)
        assertEquals(1f, greenV, 0.01f)

        val (_, whiteS, whiteV) = extractor.rgbToHsv(255, 255, 255)
        assertEquals(0f, whiteS, 0.01f)
        assertEquals(1f, whiteV, 0.01f)

        val (_, _, blackV) = extractor.rgbToHsv(0, 0, 0)
        assertEquals(0f, blackV, 0.01f)
    }

    @Test
    fun resolveIndonesianColorName_mapsAllPaletteCategoriesCorrectly() {
        assertEquals("Hitam", extractor.resolveIndonesianColorName(0f, 0.05f, 0.10f))
        assertEquals("Putih", extractor.resolveIndonesianColorName(0f, 0.04f, 0.95f))
        assertEquals("Abu-abu", extractor.resolveIndonesianColorName(0f, 0.05f, 0.50f))
        assertEquals("Cream", extractor.resolveIndonesianColorName(38f, 0.22f, 0.91f))
        assertEquals("Merah", extractor.resolveIndonesianColorName(5f, 0.85f, 0.70f))
        assertEquals("Cokelat", extractor.resolveIndonesianColorName(30f, 0.65f, 0.42f))
        assertEquals("Olive", extractor.resolveIndonesianColorName(82f, 0.55f, 0.38f))
        assertEquals("Biru dongker", extractor.resolveIndonesianColorName(220f, 0.82f, 0.25f))
        assertEquals("Biru denim", extractor.resolveIndonesianColorName(211f, 0.54f, 0.56f))
        assertEquals("Biru", extractor.resolveIndonesianColorName(215f, 0.88f, 0.78f))
    }

    @Test
    fun extractFromRgbPixels_suppressesWhiteBackgroundBorderAndFindsCenterGarment() {
        val width = 16
        val height = 16
        val white = 0xFFFFFFFF.toInt()
        val navy = 0xFF10204A.toInt()
        val pixels = IntArray(width * height) { index ->
            val x = index % width
            val y = index / width
            if (x in 4..11 && y in 4..11) navy else white
        }

        val extracted = extractor.extractFromRgbPixels(pixels, width, height)

        assertEquals("Biru dongker", extracted.colorName)
        assertTrue(extracted.value < 0.38f)
    }

    @Test
    fun extractFromRgbPixels_handlesEmptyOrUndersizedArraySafely() {
        val fallback = extractor.extractFromRgbPixels(IntArray(0), 0, 0)
        assertEquals("Abu-abu", fallback.colorName)
    }
}
