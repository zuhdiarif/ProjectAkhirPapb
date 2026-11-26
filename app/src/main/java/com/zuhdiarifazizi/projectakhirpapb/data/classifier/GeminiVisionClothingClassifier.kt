package com.zuhdiarifazizi.projectakhirpapb.data.classifier

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.zuhdiarifazizi.projectakhirpapb.domain.classifier.ClassificationResult
import com.zuhdiarifazizi.projectakhirpapb.domain.classifier.ClothingClassifier
import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingCategory
import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingPattern
import com.zuhdiarifazizi.projectakhirpapb.domain.model.HsvColor
import com.zuhdiarifazizi.projectakhirpapb.domain.model.Occasion
import com.zuhdiarifazizi.projectakhirpapb.domain.security.SecurityInputValidator
import java.io.ByteArrayOutputStream
import java.net.URL
import java.security.MessageDigest
import javax.net.ssl.HttpsURLConnection
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

fun interface GeminiVisionTransport {
    suspend fun generateJson(apiKey: String, imageBase64: String, prompt: String): String
}

class HttpGeminiVisionTransport : GeminiVisionTransport {
    internal val candidateEndpoints: List<String> = listOf(
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-lite-latest:generateContent",
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.6-flash:generateContent",
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.8-flash:generateContent"
    )

    override suspend fun generateJson(apiKey: String, imageBase64: String, prompt: String): String {
        val payloadJson = JSONObject().apply {
            put(
                "contents",
                JSONArray().put(
                    JSONObject()
                        .put("role", "user")
                        .put(
                            "parts",
                            JSONArray()
                                .put(JSONObject().put("text", prompt))
                                .put(
                                    JSONObject().put(
                                        "inlineData",
                                        JSONObject()
                                            .put("mimeType", "image/jpeg")
                                            .put("data", imageBase64)
                                    )
                                )
                        )
                )
            )
            put(
                "generationConfig",
                JSONObject()
                    .put("temperature", 0.1)
                    .put("maxOutputTokens", 512)
                    .put("responseMimeType", "application/json")
            )
        }.toString()

        var lastError: Throwable? = null
        val trimmedKey = apiKey.trim()
        for (urlString in candidateEndpoints) {
            try {
                val (statusCode, body) = executePost(urlString, trimmedKey, payloadJson)
                if (statusCode in 200..299) {
                    return extractAnswerText(body)
                }
                val err = IllegalStateException(
                    SecurityInputValidator.redactSecrets("Layanan AI merespons kode $statusCode")
                )
                lastError = err
                if (statusCode !in RETRYABLE_HTTP_CODES) {
                    throw err
                }
            } catch (t: Throwable) {
                lastError = t
                if (t is IllegalStateException && t.message?.contains("merespons kode") == true) {
                    val code = t.message?.substringAfter("merespons kode ")?.toIntOrNull()
                    if (code != null && code !in RETRYABLE_HTTP_CODES) throw t
                }
            }
        }
        throw lastError ?: IllegalStateException("Layanan AI belum merespons.")
    }

    private fun executePost(urlString: String, apiKey: String, payloadJson: String): Pair<Int, String> {
        val endpoint = URL(urlString)
        val connection = (endpoint.openConnection() as? HttpsURLConnection)
            ?: error("Koneksi HTTPS diperlukan untuk layanan AI.")
        return try {
            connection.requestMethod = "POST"
            connection.connectTimeout = CONNECT_TIMEOUT_MS
            connection.readTimeout = READ_TIMEOUT_MS
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            connection.setRequestProperty("x-goog-api-key", apiKey)
            connection.outputStream.use { stream ->
                stream.write(payloadJson.toByteArray(Charsets.UTF_8))
            }
            val statusCode = connection.responseCode
            val responseStream = if (statusCode in 200..299) connection.inputStream else connection.errorStream
            val body = responseStream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            statusCode to body
        } finally {
            connection.disconnect()
        }
    }

    private fun extractAnswerText(body: String): String {
        val root = JSONObject(body)
        val candidates = root.optJSONArray("candidates") ?: error("Respons AI kosong.")
        val parts = candidates.getJSONObject(0)
            .getJSONObject("content")
            .getJSONArray("parts")
        var selectedText: String? = null
        for (i in 0 until parts.length()) {
            val part = parts.getJSONObject(i)
            val isThought = part.optBoolean("thought", false)
            val text = part.optString("text")
            if (!isThought && text.isNotBlank()) {
                selectedText = text
            }
        }
        return selectedText ?: parts.getJSONObject(0).getString("text")
    }

    private companion object {
        val RETRYABLE_HTTP_CODES = setOf(404, 429, 500, 502, 503, 504)
        const val CONNECT_TIMEOUT_MS = 8_000
        const val READ_TIMEOUT_MS = 12_000
    }
}

/**
 * Classifies garments using Gemini Vision API (AI Studio or Vertex AI Express via x-goog-api-key header)
 * paired with local RGB-to-HSV color extraction, SHA-256 quota cache, and automatic offline fallback.
 */
class GeminiVisionClothingClassifier(
    private val apiKey: String,
    private val hsvColorExtractor: HsvColorExtractor = HsvColorExtractor(),
    private val transport: GeminiVisionTransport = HttpGeminiVisionTransport(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val base64Encoder: (ByteArray) -> String = { bytes ->
        Base64.encodeToString(bytes, Base64.NO_WRAP)
    },
    private val colorFromBytes: (ByteArray) -> HsvColor = hsvColorExtractor::extractFromImageBytes
) : ClothingClassifier {

    private val cacheMutex = Mutex()
    private val resultCache = object : LinkedHashMap<String, ClassificationResult>(CACHE_CAPACITY, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, ClassificationResult>?): Boolean =
            size > CACHE_CAPACITY
    }

    override suspend fun classify(imageBytes: ByteArray): Result<ClassificationResult> = withContext(ioDispatcher) {
        if (imageBytes.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("Data foto pakaian kosong."))
        }

        val contentHash = sha256Hex(imageBytes)
        cacheMutex.withLock {
            resultCache[contentHash]?.let { cached ->
                return@withContext Result.success(cached)
            }

            val dominantColor = colorFromBytes(imageBytes)
            if (apiKey.isBlank()) {
                return@withContext Result.success(buildLocalFallback(dominantColor))
            }

            val preparedBytes = downscaleJpegIfNeeded(imageBytes)
            val encodedImage = base64Encoder(preparedBytes)
            val remoteResult = runCatching {
                val rawJson = transport.generateJson(apiKey = apiKey, imageBase64 = encodedImage, prompt = PROMPT_ID)
                parseClassificationJson(rawJson, dominantColor)
            }
            val finalResult = remoteResult.getOrElse { buildLocalFallback(dominantColor) }
            if (finalResult.isAiPowered) {
                resultCache[contentHash] = finalResult
            }
            Result.success(finalResult)
        }
    }

    internal fun parseClassificationJson(rawText: String, dominantColor: HsvColor): ClassificationResult {
        val jsonText = rawText.trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
        val fields = parseFlatJsonFields(jsonText)

        val category = parseCategory(fields["category"].orEmpty())
        val pattern = parsePattern(fields["pattern"].orEmpty())
        val occasion = parseOccasion(fields["occasion"].orEmpty())
        val rawName = SecurityInputValidator.sanitizeText(fields["suggestedName"].orEmpty(), maxLength = 60)
        val rawMaterial = SecurityInputValidator.sanitizeText(fields["material"].orEmpty(), maxLength = 32)
        val aiColorName = SecurityInputValidator.sanitizeText(fields["colorName"].orEmpty(), maxLength = 24)
        val resolvedColor = resolveColorWithAiHint(dominantColor, aiColorName)
        val confidence = (fields["confidence"]?.toDoubleOrNull() ?: 0.85).toFloat().coerceIn(0f, 1f)

        val fallbackName = "${category.displayName} ${resolvedColor.colorName}"
        return ClassificationResult(
            suggestedName = rawName.ifBlank { fallbackName },
            category = category,
            pattern = pattern,
            occasion = occasion,
            dominantColor = resolvedColor,
            confidence = confidence,
            material = rawMaterial.ifBlank { "Katun Halus" },
            isAiPowered = true
        )
    }

    internal fun parseFlatJsonFields(jsonText: String): Map<String, String> {
        val trimmed = jsonText.trim()
        require(trimmed.startsWith("{") && trimmed.endsWith("}")) { "Bukan objek JSON valid." }
        val result = linkedMapOf<String, String>()
        var index = 1
        val end = trimmed.length - 1

        fun skipWhitespace() {
            while (index < end && trimmed[index].isWhitespace()) index++
        }

        fun readQuotedString(): String {
            require(index < end && trimmed[index] == '"') { "String JSON diharapkan." }
            index++
            val sb = StringBuilder()
            while (index < end) {
                val ch = trimmed[index++]
                when (ch) {
                    '"' -> return sb.toString()
                    '\\' -> {
                        require(index < end) { "Escape JSON tidak lengkap." }
                        val esc = trimmed[index++]
                        sb.append(
                            when (esc) {
                                '"' -> '"'
                                '\\' -> '\\'
                                '/' -> '/'
                                'b' -> '\b'
                                'n' -> '\n'
                                'r' -> '\r'
                                't' -> '\t'
                                'u' -> {
                                    require(index + 4 <= end) { "Unicode JSON tidak valid." }
                                    val code = trimmed.substring(index, index + 4).toInt(16)
                                    index += 4
                                    code.toChar()
                                }
                                else -> esc
                            }
                        )
                    }
                    else -> sb.append(ch)
                }
            }
            throw IllegalArgumentException("String JSON tidak ditutup.")
        }

        skipWhitespace()
        while (index < end) {
            val key = readQuotedString()
            skipWhitespace()
            require(index < end && trimmed[index] == ':') { "Pemisah ':' diharapkan." }
            index++
            skipWhitespace()
            val value = if (index < end && trimmed[index] == '"') {
                readQuotedString()
            } else {
                val valStart = index
                while (index < end && trimmed[index] != ',' && !trimmed[index].isWhitespace()) {
                    index++
                }
                val token = trimmed.substring(valStart, index)
                require(token.isNotEmpty()) { "Nilai JSON kosong." }
                if (token == "null") "" else token
            }
            result[key] = value
            skipWhitespace()
            if (index < end) {
                require(trimmed[index] == ',') { "Pemisah ',' diharapkan." }
                index++
                skipWhitespace()
            }
        }
        return result
    }

    private fun buildLocalFallback(dominantColor: HsvColor): ClassificationResult {
        val defaultCategory = ClothingCategory.TOP_WEAR
        return ClassificationResult(
            suggestedName = "Pakaian ${dominantColor.colorName}",
            category = defaultCategory,
            pattern = ClothingPattern.SOLID,
            occasion = Occasion.HANGOUT,
            dominantColor = dominantColor,
            confidence = 0.60f,
            material = "Katun",
            isAiPowered = false
        )
    }

    private fun resolveColorWithAiHint(extracted: HsvColor, aiColorRaw: String): HsvColor {
        if (aiColorRaw.isBlank()) return extracted
        val normalized = when (aiColorRaw.trim().lowercase()) {
            "biru navy", "navy", "dongker", "biru dongker" -> "Biru dongker"
            "denim", "biru denim" -> "Biru denim"
            "krem", "cream", "beige", "khaki" -> "Cream"
            "putih", "white" -> "Putih"
            "hitam", "black" -> "Hitam"
            "abu", "abu-abu", "abu abu", "gray", "grey" -> "Abu-abu"
            "olive", "hijau olive", "hijau" -> "Olive"
            "merah", "red", "maroon" -> "Merah"
            "cokelat", "coklat", "brown" -> "Cokelat"
            "biru", "blue" -> "Biru"
            else -> return extracted
        }
        if (extracted.colorName.equals(normalized, ignoreCase = true)) return extracted
        val preset = COLOR_PRESETS[normalized] ?: return extracted
        return if (extracted.saturation <= 0.25f || extracted.value <= 0.25f || extracted.colorName == "Abu-abu") {
            preset
        } else {
            extracted
        }
    }

    private fun sha256Hex(bytes: ByteArray): String =
        MessageDigest.getInstance("SHA-256")
            .digest(bytes)
            .joinToString("") { "%02x".format(it) }

    private fun parseCategory(value: String): ClothingCategory = when (value.trim().uppercase()) {
        "BOTTOM_WEAR", "BAWAHAN", "CELANA", "ROK" -> ClothingCategory.BOTTOM_WEAR
        "OUTER_WEAR", "LUARAN", "PAKAIAN LUAR", "JAKET", "BLAZER" -> ClothingCategory.OUTER_WEAR
        else -> ClothingCategory.TOP_WEAR
    }

    private fun parsePattern(value: String): ClothingPattern = when (value.trim().uppercase()) {
        "STRIPED", "BERGARIS" -> ClothingPattern.STRIPED
        "PLAID", "KOTAK-KOTAK", "KOTAK" -> ClothingPattern.PLAID
        "FLORAL", "MOTIF BUNGA", "BUNGA" -> ClothingPattern.FLORAL
        "GRAPHIC", "BERGAMBAR" -> ClothingPattern.GRAPHIC
        "OTHER", "LAINNYA" -> ClothingPattern.OTHER
        else -> ClothingPattern.SOLID
    }

    private fun parseOccasion(value: String): Occasion = when (value.trim().uppercase()) {
        "WORK", "KERJA", "FORMAL" -> Occasion.WORK
        "EXERCISE", "OLAHRAGA", "SPORT" -> Occasion.EXERCISE
        "STREETWEAR" -> Occasion.STREETWEAR
        "OTHER", "LAINNYA" -> Occasion.OTHER
        else -> Occasion.HANGOUT
    }

    private fun downscaleJpegIfNeeded(bytes: ByteArray): ByteArray {
        if (bytes.size <= MAX_UPLOAD_BYTES) return bytes
        return runCatching {
            val decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return bytes
            val scale = minOf(1f, MAX_DIMENSION_PX / maxOf(decoded.width, decoded.height))
            val scaled = if (scale < 1f) {
                Bitmap.createScaledBitmap(
                    decoded,
                    (decoded.width * scale).toInt().coerceAtLeast(1),
                    (decoded.height * scale).toInt().coerceAtLeast(1),
                    true
                ).also { decoded.recycle() }
            } else {
                decoded
            }
            val output = ByteArrayOutputStream()
            scaled.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output)
            scaled.recycle()
            output.toByteArray()
        }.getOrDefault(bytes)
    }

    private companion object {
        const val CACHE_CAPACITY = 32
        const val MAX_UPLOAD_BYTES = 180 * 1024
        const val MAX_DIMENSION_PX = 512f
        const val JPEG_QUALITY = 75
        val COLOR_PRESETS = mapOf(
            "Putih" to HsvColor(0f, 0.02f, 0.98f, "Putih"),
            "Hitam" to HsvColor(0f, 0.02f, 0.08f, "Hitam"),
            "Abu-abu" to HsvColor(0f, 0.02f, 0.52f, "Abu-abu"),
            "Biru dongker" to HsvColor(220f, 0.82f, 0.25f, "Biru dongker"),
            "Biru denim" to HsvColor(211f, 0.54f, 0.56f, "Biru denim"),
            "Biru" to HsvColor(211f, 0.54f, 0.56f, "Biru"),
            "Cream" to HsvColor(38f, 0.22f, 0.91f, "Cream"),
            "Olive" to HsvColor(80f, 0.55f, 0.38f, "Olive"),
            "Merah" to HsvColor(0f, 0.78f, 0.72f, "Merah"),
            "Cokelat" to HsvColor(25f, 0.58f, 0.40f, "Cokelat")
        )
        val PROMPT_ID = """
            Identifikasi pakaian pada foto untuk pengguna tunanetra dalam Bahasa Indonesia.
            Balas HANYA JSON valid dengan field:
            {
              "suggestedName": "nama singkat misal Kemeja Katun Biru Navy",
              "category": "TOP_WEAR | BOTTOM_WEAR | OUTER_WEAR",
              "colorName": "Putih | Hitam | Abu-abu | Biru dongker | Biru denim | Biru | Cream | Olive | Merah | Cokelat",
              "material": "bahan atau gaya singkat misal Katun Halus, Denim, Acara Resmi",
              "pattern": "SOLID | STRIPED | PLAID | FLORAL | GRAPHIC | OTHER",
              "occasion": "WORK | HANGOUT | EXERCISE | STREETWEAR | OTHER",
              "confidence": 0.92
            }
        """.trimIndent()
    }
}
