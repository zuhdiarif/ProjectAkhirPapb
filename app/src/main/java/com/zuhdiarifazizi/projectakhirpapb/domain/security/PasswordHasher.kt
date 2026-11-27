package com.zuhdiarifazizi.projectakhirpapb.domain.security

import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Hashes and verifies local account passwords with salted PBKDF2.
 * Uses PBKDF2WithHmacSHA256 on API 26+ and falls back to PBKDF2WithHmacSHA1 on API 24-25.
 */
object PasswordHasher {

    private const val SALT_BYTES = 16
    private const val ITERATIONS = 65_536
    private const val KEY_LENGTH_BITS = 256
    private const val PRIMARY_ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val FALLBACK_ALGORITHM = "PBKDF2WithHmacSHA1"

    data class SaltedHash(
        val saltHex: String,
        val hashHex: String
    )

    fun generateSaltHex(): String {
        val bytes = ByteArray(SALT_BYTES)
        SecureRandom().nextBytes(bytes)
        return bytes.toHex()
    }

    fun hashPassword(password: String, saltHex: String = generateSaltHex()): SaltedHash {
        val saltBytes = saltHex.fromHex()
        val spec = PBEKeySpec(password.toCharArray(), saltBytes, ITERATIONS, KEY_LENGTH_BITS)
        return try {
            val encoded = secretKeyFactory().generateSecret(spec).encoded
            SaltedHash(saltHex = saltHex, hashHex = encoded.toHex())
        } finally {
            spec.clearPassword()
        }
    }

    fun verify(password: String, expectedSaltHex: String, expectedHashHex: String): Boolean {
        if (expectedSaltHex.isBlank() || expectedHashHex.isBlank()) return false
        val actual = runCatching { hashPassword(password, expectedSaltHex).hashHex }.getOrNull() ?: return false
        return MessageDigest.isEqual(actual.fromHex(), expectedHashHex.fromHex())
    }

    private fun secretKeyFactory(): SecretKeyFactory = try {
        SecretKeyFactory.getInstance(PRIMARY_ALGORITHM)
    } catch (_: NoSuchAlgorithmException) {
        SecretKeyFactory.getInstance(FALLBACK_ALGORITHM)
    }

    private fun ByteArray.toHex(): String = joinToString(separator = "") { "%02x".format(it) }

    private fun String.fromHex(): ByteArray {
        require(length % 2 == 0) { "Invalid hex length" }
        return ByteArray(length / 2) { index ->
            substring(index * 2, index * 2 + 2).toInt(16).toByte()
        }
    }
}
