package com.zuhdiarifazizi.projectakhirpapb.domain.harmony

import com.zuhdiarifazizi.projectakhirpapb.data.harmony.KotlinProjectHarmonyScorer
import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingCategory
import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingItem
import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingPattern
import com.zuhdiarifazizi.projectakhirpapb.domain.model.HsvColor
import com.zuhdiarifazizi.projectakhirpapb.domain.model.Occasion
import java.io.File
import kotlin.math.abs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProjectHarmonyScorerTest {

    private val scorer = KotlinProjectHarmonyScorer()

    private fun item(
        id: String,
        category: ClothingCategory,
        hue: Float,
        saturation: Float,
        value: Float,
        pattern: ClothingPattern = ClothingPattern.SOLID
    ) = ClothingItem(
        id = id,
        userId = "test_user",
        name = id,
        category = category,
        pattern = pattern,
        occasion = Occasion.HANGOUT,
        color = HsvColor(hue, saturation, value, "Test")
    )

    @Test
    fun encoderCapturesCircularHueContinuityAcross360Wrap() {
        val redWrapA = item("a", ClothingCategory.TOP_WEAR, hue = 359f, saturation = 0.80f, value = 0.70f)
        val redWrapB = item("b", ClothingCategory.BOTTOM_WEAR, hue = 1f, saturation = 0.75f, value = 0.50f)

        val encoded = ProjectHarmonyFeatureEncoder.encode(redWrapA, redWrapB, null)

        assertEquals(ProjectHarmonyFeatureEncoder.INPUT_SIZE, encoded.size)
        // Minimum & maximum circular hue diff between 359° and 1° is 2°, normalized by 180° -> 0.0111
        assertEquals(2f / 180f, encoded[19], 0.0005f)
        assertEquals(2f / 180f, encoded[20], 0.0005f)
        assertEquals(0.20f, encoded[21], 0.0001f)
    }

    @Test
    fun neuralScorerClassifiesAllSixHarmonyTypesAccurately() {
        val neutralTop = item("nt", ClothingCategory.TOP_WEAR, 0f, 0.02f, 0.98f)
        val neutralBottom = item("nb", ClothingCategory.BOTTOM_WEAR, 0f, 0.02f, 0.08f)
        assertEquals("Harmoni Netral Klasik", scorer.evaluate(neutralTop, neutralBottom, null).harmonyType)

        val denimBottom = item("db", ClothingCategory.BOTTOM_WEAR, 211f, 0.54f, 0.56f)
        assertEquals("Paduan Netral Seimbang", scorer.evaluate(neutralTop, denimBottom, null).harmonyType)

        val navyTop = item("nvt", ClothingCategory.TOP_WEAR, 220f, 0.82f, 0.25f)
        assertEquals("Harmoni Monokromatik", scorer.evaluate(navyTop, denimBottom, null).harmonyType)

        val oliveTop = item("olt", ClothingCategory.TOP_WEAR, 80f, 0.55f, 0.38f)
        val warmGoldBottom = item("wgb", ClothingCategory.BOTTOM_WEAR, 50f, 0.45f, 0.80f)
        assertEquals("Harmoni Analogus", scorer.evaluate(oliveTop, warmGoldBottom, null).harmonyType)

        val blueTop = item("blt", ClothingCategory.TOP_WEAR, 215f, 0.72f, 0.48f)
        val terracottaBottom = item("tcb", ClothingCategory.BOTTOM_WEAR, 24f, 0.65f, 0.68f)
        assertEquals("Harmoni Komplementer", scorer.evaluate(blueTop, terracottaBottom, null).harmonyType)

        val mustardTop = item("mt", ClothingCategory.TOP_WEAR, 45f, 0.78f, 0.85f)
        val forestBottom = item("fb", ClothingCategory.BOTTOM_WEAR, 135f, 0.60f, 0.45f)
        assertEquals("Kontras Warna Seimbang", scorer.evaluate(mustardTop, forestBottom, null).harmonyType)
    }

    @Test
    fun neuralScorerPrefersSolidAnchorOverDualLoudPatterns() {
        val solidBlue = item("sb", ClothingCategory.TOP_WEAR, 215f, 0.70f, 0.50f, ClothingPattern.SOLID)
        val plaidGold = item("pg", ClothingCategory.BOTTOM_WEAR, 50f, 0.60f, 0.75f, ClothingPattern.PLAID)
        val graphicBlue = item("gb", ClothingCategory.TOP_WEAR, 215f, 0.70f, 0.50f, ClothingPattern.GRAPHIC)

        val anchored = scorer.evaluate(solidBlue, plaidGold, null)
        val dualLoud = scorer.evaluate(graphicBlue, plaidGold, null)

        assertTrue(
            "Expected solid-anchored (${anchored.colorScore}) > dual-loud (${dualLoud.colorScore})",
            anchored.colorScore > dualLoud.colorScore
        )
    }

    @Test
    fun kotlinWeightsMatchPythonGoldenVectorsAndTfliteAssetExists() {
        val tfliteFile = File("src/main/assets/project_harmony.tflite").takeIf { it.exists() }
            ?: File("app/src/main/assets/project_harmony.tflite")
        assertTrue("Expected project_harmony.tflite asset to exist", tfliteFile.exists())
        val header = tfliteFile.inputStream().use { input -> ByteArray(8).also { input.read(it) } }
        assertEquals("TFL3", String(header, 4, 4, Charsets.US_ASCII))

        val neutralTop = item("nt", ClothingCategory.TOP_WEAR, 0f, 0.02f, 0.98f)
        val neutralBottom = item("nb", ClothingCategory.BOTTOM_WEAR, 0f, 0.02f, 0.08f)
        val pred = scorer.evaluate(neutralTop, neutralBottom, null)
        assertEquals("Harmoni Netral Klasik", pred.harmonyType)
        assertTrue(abs(pred.colorScore - 0.914993) < 0.001)
    }
}
