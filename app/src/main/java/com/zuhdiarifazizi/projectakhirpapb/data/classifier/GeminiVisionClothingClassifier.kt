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
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

fun interface GeminiVisionTransport {
    suspend fun generateJson(apiKey: String, imageBase64: String, prompt: String): String
}

class HttpGeminiVisionTransport : GeminiVisionTransport {
    override suspend fun generateJson(apiKey: String, imageBase64: String, prompt: String): String {
        val endpoint = URL(GEMINI_FLASH_ENDPOINT)
        val connection = (endpoint.openConnection() as? HttpsURLConnection)
            ?: error("Koneksi HTTPS diperlukan untuk layanan AI.")

        try {
            connection.requestMethod = "POST"
            connection.connectTimeout = CONNECT_TIMEOUT_MS
            connection.readTimeout = READ_TIMEOUT_MS
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            connection.setRequestProperty("x-goog-api-key", apiKey)

            val payload = JSONObject().apply {
                put(
                    "contents",
                    JSONArray().put(
                        JSONObject().put(
                            "parts",
                            JSONArray()
                                .put(JSONObject().put("text", prompt))
                                .put(
                                    JSONObject().put(
                                        "inline_data",
                                        JSONObject()
                                            .put("mime_type", "image/jpeg")
                                            .put("data", imageBase64)
                                    )
                                )
                        )
                    )
                )
                put(
                    "generationConfig",
                    JSONObject()
                        .put("temperature", 0.2)
                        .put("responseMimeType", "application/json")
                )
            }

            connection.outputStream.use { stream ->
                stream.write(payload.toString().toByteArray(Charsets.UTF_8))
            }

            val statusCode = connection.responseCode
            val responseStream = if (statusCode in 200..299) connection.inputStream else connection.errorStream
            val body = responseStream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            if (statusCode !in 200..299) {
                error(SecurityInputValidator.redactSecrets("Layanan AI merespons kode $statusCode"))
            }

            val root = JSONObject(body)
            val candidates = root.optJSONArray("candidates") ?: error("Respons AI kosong.")
            val firstPart = candidates.getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
            return firstPart.getString("text")
        } finally {
            connection.disconnect()
        }
    }

    private companion object {
        const val GEMINI_FLASH_ENDPOINT =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent"
        const val CONNECT_TIMEOUT_MS = 10_000
        const val READ_TIMEOUT_MS = 15_000
    }
}

/**
 * Classifies garments using Gemini Vision API (API key strictly via x-goog-api-key header)
 * paired with local RGB-to-HSV color extraction, with automatic offline fallback.
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

    override suspend fun classify(imageBytes: ByteArray): Result<ClassificationResult> = withContext(ioDispatcher) {
        if (imageBytes.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("Data foto pakaian kosong."))
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
        Result.success(remoteResult.getOrElse { buildLocalFallback(dominantColor) })
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
        val confidence = (fields["confidence"]?.toDoubleOrNull() ?: 0.85).toFloat().coerceIn(0f, 1f)

        val fallbackName = "${category.displayName} ${dominantColor.colorName}"
        return ClassificationResult(
            suggestedName = rawName.ifBlank { fallbackName },
            category = category,
            pattern = pattern,
            occasion = occasion,
            dominantColor = dominantColor,
            confidence = confidence,
            material = rawMaterial.ifBlank { "Katun Halus" }
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
            material = "Katun"
        )
    }

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
            val scale = minOf(1f, 768f / maxOf(decoded.width, decoded.height))
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
            scaled.compress(Bitmap.CompressFormat.JPEG, 80, output)
            scaled.recycle()
            output.toByteArray()
        }.getOrDefault(bytes)
    }

    private companion object {
        const val MAX_UPLOAD_BYTES = 512 * 1024
        val PROMPT_ID = """
            Identifikasi pakaian pada foto untuk pengguna tunanetra dalam Bahasa Indonesia.
            Balas HANYA JSON valid dengan field:
            {
              "suggestedName": "nama singkat misal Kemeja Katun Biru Navy",
              "category": "TOP_WEAR | BOTTOM_WEAR | OUTER_WEAR",
              "material": "bahan atau gaya singkat misal Katun Halus, Denim, Acara Resmi",
              "pattern": "SOLID | STRIPED | PLAID | FLORAL | GRAPHIC | OTHER",
              "occasion": "WORK | HANGOUT | EXERCISE | STREETWEAR | OTHER",
              "confidence": 0.92
            }
        """.trimIndent()
    }
}
