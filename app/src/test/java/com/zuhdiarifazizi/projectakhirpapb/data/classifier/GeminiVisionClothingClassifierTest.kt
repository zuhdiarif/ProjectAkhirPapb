package com.zuhdiarifazizi.projectakhirpapb.data.classifier

import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingCategory
import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingPattern
import com.zuhdiarifazizi.projectakhirpapb.domain.model.HsvColor
import com.zuhdiarifazizi.projectakhirpapb.domain.model.Occasion
import java.io.IOException
import java.util.Base64
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GeminiVisionClothingClassifierTest {

    private val sampleNavyColor = HsvColor(220f, 0.82f, 0.25f, "Biru dongker")
    private val sampleBytes = byteArrayOf(0x01, 0x02, 0x03, 0x04)

    @Test
    fun classify_emptyBytes_returnsFailure() = runTest {
        val classifier = GeminiVisionClothingClassifier(
            apiKey = "test-key",
            transport = { _, _, _ -> error("Should not be called") },
            ioDispatcher = UnconfinedTestDispatcher(testScheduler),
            base64Encoder = { Base64.getEncoder().encodeToString(it) },
            colorFromBytes = { sampleNavyColor }
        )

        val result = classifier.classify(ByteArray(0))

        assertTrue(result.isFailure)
    }

    @Test
    fun classify_blankApiKey_usesLocalDeterministicFallbackWithoutNetworkCall() = runTest {
        var transportCalled = false
        val classifier = GeminiVisionClothingClassifier(
            apiKey = "   ",
            transport = { _, _, _ ->
                transportCalled = true
                "{}"
            },
            ioDispatcher = UnconfinedTestDispatcher(testScheduler),
            base64Encoder = { Base64.getEncoder().encodeToString(it) },
            colorFromBytes = { sampleNavyColor }
        )

        val result = classifier.classify(sampleBytes).getOrThrow()

        assertFalse(transportCalled)
        assertEquals("Pakaian Biru dongker", result.suggestedName)
        assertEquals(sampleNavyColor, result.dominantColor)
        assertEquals(0.60f, result.confidence, 0.001f)
    }

    @Test
    fun classify_validMarkdownWrappedJson_parsesSanitizesAndForwardsApiKey() = runTest {
        var capturedKey = ""
        val classifier = GeminiVisionClothingClassifier(
            apiKey = "secret-gemini-key",
            transport = { key, _, _ ->
                capturedKey = key
                """
                    ```json
                    {
                      "suggestedName": "Jaket\u0007   Flanel Kotak",
                      "category": "OUTER_WEAR",
                      "material": "Wol\nFormal",
                      "pattern": "PLAID",
                      "occasion": "WORK",
                      "confidence": 0.93
                    }
                    ```
                """.trimIndent()
            },
            ioDispatcher = UnconfinedTestDispatcher(testScheduler),
            base64Encoder = { Base64.getEncoder().encodeToString(it) },
            colorFromBytes = { sampleNavyColor }
        )

        val result = classifier.classify(sampleBytes).getOrThrow()

        assertEquals("secret-gemini-key", capturedKey)
        assertEquals("Jaket Flanel Kotak", result.suggestedName)
        assertEquals(ClothingCategory.OUTER_WEAR, result.category)
        assertEquals("Wol Formal", result.material)
        assertEquals(ClothingPattern.PLAID, result.pattern)
        assertEquals(Occasion.WORK, result.occasion)
        assertEquals(0.93f, result.confidence, 0.001f)
        assertEquals(sampleNavyColor, result.dominantColor)
    }

    @Test
    fun classify_transportFailureOrMalformedJson_fallsBackGracefully() = runTest {
        val networkFailureClassifier = GeminiVisionClothingClassifier(
            apiKey = "secret-key",
            transport = { _, _, _ -> throw IOException("No internet") },
            ioDispatcher = UnconfinedTestDispatcher(testScheduler),
            base64Encoder = { Base64.getEncoder().encodeToString(it) },
            colorFromBytes = { sampleNavyColor }
        )
        val networkFallback = networkFailureClassifier.classify(sampleBytes).getOrThrow()
        assertEquals("Pakaian Biru dongker", networkFallback.suggestedName)

        val badJsonClassifier = GeminiVisionClothingClassifier(
            apiKey = "secret-key",
            transport = { _, _, _ -> "{not valid json" },
            ioDispatcher = UnconfinedTestDispatcher(testScheduler),
            base64Encoder = { Base64.getEncoder().encodeToString(it) },
            colorFromBytes = { sampleNavyColor }
        )
        val badJsonFallback = badJsonClassifier.classify(sampleBytes).getOrThrow()
        assertEquals("Pakaian Biru dongker", badJsonFallback.suggestedName)
        assertEquals("Katun", badJsonFallback.material)
    }
}
