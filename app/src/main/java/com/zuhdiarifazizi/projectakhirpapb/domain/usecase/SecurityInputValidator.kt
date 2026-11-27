package com.zuhdiarifazizi.projectakhirpapb.domain.usecase

/**
 * Security utilities for sanitizing untrusted AI/user text, validating 13.56 MHz NFC/RFID UIDs,
 * and redacting accidental credential leakage from logs and error messages.
 */
object SecurityInputValidator {

    private const val MIN_UID_BYTES = 4
    private const val MAX_UID_BYTES = 10
    private val NFC_UID_COLON_REGEX = Regex("^([0-9A-F]{2}:){3,9}[0-9A-F]{2}$")
    private val NFC_UID_COMPACT_REGEX = Regex("^[0-9A-F]{8,20}$")
    private val GOOGLE_API_KEY_REGEX = Regex("AIza[0-9A-Za-z_\\-]{20,}")
    private val VERTEX_API_KEY_REGEX = Regex("AQ\\.[0-9A-Za-z_\\-]{20,}")
    private val GITHUB_TOKEN_REGEX = Regex("(ghp_[0-9A-Za-z]{20,}|github_pat_[0-9A-Za-z_]{20,})")
    private val BEARER_TOKEN_REGEX = Regex("Bearer\\s+[A-Za-z0-9._\\-]+", RegexOption.IGNORE_CASE)

    fun sanitizeText(input: String, maxLength: Int = 80): String {
        if (maxLength <= 0) return ""
        val cleaned = buildString(input.length) {
            for (ch in input) {
                if (ch.code >= 0x20 && ch.code != 0x7F) {
                    append(ch)
                } else if (ch == '\n' || ch == '\r' || ch == '\t') {
                    append(' ')
                }
            }
        }
        return cleaned.trim()
            .replace(Regex("\\s+"), " ")
            .take(maxLength)
            .trim()
    }

    fun normalizeAndValidateNfcUid(rawBytes: ByteArray): String? {
        if (rawBytes.size !in MIN_UID_BYTES..MAX_UID_BYTES) return null
        return rawBytes.joinToString(separator = ":") { byte ->
            "%02X".format(byte.toInt() and 0xFF)
        }
    }

    fun normalizeNfcUidString(rawUid: String): String? {
        val compact = rawUid.trim().uppercase().replace(":", "").replace("-", "").replace(" ", "")
        if (!NFC_UID_COMPACT_REGEX.matches(compact) || compact.length % 2 != 0) return null
        val byteCount = compact.length / 2
        if (byteCount !in MIN_UID_BYTES..MAX_UID_BYTES) return null
        val formatted = compact.chunked(2).joinToString(":")
        return if (NFC_UID_COLON_REGEX.matches(formatted)) formatted else null
    }

    fun isValidNfcUid(uid: String): Boolean =
        NFC_UID_COLON_REGEX.matches(uid.trim().uppercase())

    fun redactSecrets(message: String): String = message
        .replace(GOOGLE_API_KEY_REGEX, "[REDACTED_KEY]")
        .replace(VERTEX_API_KEY_REGEX, "[REDACTED_KEY]")
        .replace(GITHUB_TOKEN_REGEX, "[REDACTED_TOKEN]")
        .replace(BEARER_TOKEN_REGEX, "Bearer [REDACTED]")
}
