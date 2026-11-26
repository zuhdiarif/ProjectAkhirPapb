package com.zuhdiarifazizi.projectakhirpapb.domain.security

import com.zuhdiarifazizi.projectakhirpapb.data.storage.ClothingPhotoStorage
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class SecurityInputValidatorTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun sanitizeText_stripsControlCharsCollapsesWhitespaceAndTruncates() {
        val raw = "  Kemeja\u0000\u0007\nBatik   Biru\tHalus\u007F  "
        val cleaned = SecurityInputValidator.sanitizeText(raw, maxLength = 18)

        assertEquals("Kemeja Batik Biru", cleaned)
        assertEquals("", SecurityInputValidator.sanitizeText("abc", maxLength = 0))
    }

    @Test
    fun normalizeAndValidateNfcUid_handlesByteArraysInIso14443Range() {
        assertEquals(
            "04:AB:12:7E",
            SecurityInputValidator.normalizeAndValidateNfcUid(
                byteArrayOf(0x04, 0xAB.toByte(), 0x12, 0x7E)
            )
        )
        assertEquals(
            "04:11:22:33:44:55:66",
            SecurityInputValidator.normalizeAndValidateNfcUid(
                byteArrayOf(0x04, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66)
            )
        )
        assertNull(SecurityInputValidator.normalizeAndValidateNfcUid(byteArrayOf(0x04, 0x11, 0x22)))
        assertNull(SecurityInputValidator.normalizeAndValidateNfcUid(ByteArray(11) { 0x01 }))
    }

    @Test
    fun normalizeNfcUidString_normalizesFormatsAndRejectsInvalidPayloads() {
        assertEquals("04:AB:12:7E", SecurityInputValidator.normalizeNfcUidString("04ab127e"))
        assertEquals("04:AB:12:7E", SecurityInputValidator.normalizeNfcUidString(" 04-ab-12-7e "))
        assertEquals("04:AB:12:7E", SecurityInputValidator.normalizeNfcUidString("04 ab 12 7e"))

        assertNull(SecurityInputValidator.normalizeNfcUidString("04:AB:12"))
        assertNull(SecurityInputValidator.normalizeNfcUidString("04A"))
        assertNull(SecurityInputValidator.normalizeNfcUidString("ZZ:11:22:33"))

        assertTrue(SecurityInputValidator.isValidNfcUid("04:ab:12:7e"))
        assertFalse(SecurityInputValidator.isValidNfcUid("04AB127E"))
    }

    @Test
    fun redactSecrets_masksGoogleKeysGithubTokensAndBearerTokens() {
        val sampleGoogleKey = "AIza" + "SyDummyKey12345678901234567890"
        val sampleVertexKey = "AQ." + "Ab8DummyVertexKey123456789012345"
        val sampleGitHubPat = "ghp_" + "1234567890abcdefghijklmnop"
        val rawError = "Request failed key=$sampleGoogleKey vertex=$sampleVertexKey token=$sampleGitHubPat auth=Bearer secret.jwt.token"

        val redacted = SecurityInputValidator.redactSecrets(rawError)

        assertFalse(redacted.contains(sampleGoogleKey))
        assertFalse(redacted.contains(sampleVertexKey))
        assertFalse(redacted.contains(sampleGitHubPat))
        assertFalse(redacted.contains("secret.jwt.token"))
        assertTrue(redacted.contains("[REDACTED_KEY]"))
        assertTrue(redacted.contains("[REDACTED_TOKEN]"))
        assertTrue(redacted.contains("Bearer [REDACTED]"))
    }

    @Test
    fun photoStorage_rejectsDisallowedSchemesEnforcesSizeCapAndProtectsSiblingFiles() = runBlocking {
        val photosDir = tempFolder.newFolder("clothing-images")
        val siblingFile = File(tempFolder.root, "secret-outside.jpg").apply { writeText("keep") }

        val storage = ClothingPhotoStorage(photosDir) { uri ->
            when (uri) {
                "content://media/valid" -> ByteArrayInputStream(byteArrayOf(1, 2, 3, 4))
                "content://media/oversized" -> oversizedStream(bytesTotal = 15 * 1024 * 1024 + 128)
                else -> null
            }
        }

        val httpFailure = runCatching { storage.importPhoto("https://evil.example/a.jpg") }
        assertTrue(httpFailure.exceptionOrNull() is IllegalArgumentException)

        val importedUri = storage.importPhoto("content://media/valid")
        val importedFile = File(java.net.URI(importedUri))
        assertTrue(importedFile.exists())
        assertEquals(photosDir.canonicalFile, importedFile.parentFile?.canonicalFile)

        val sizeFailure = runCatching { storage.importPhoto("content://media/oversized") }
        assertTrue(sizeFailure.exceptionOrNull()?.message?.contains("15 MB") == true)
        assertEquals(1, photosDir.listFiles()?.size ?: 0)

        storage.deleteStoredPhoto(importedUri)
        assertFalse(importedFile.exists())

        storage.deleteStoredPhoto(siblingFile.toURI().toString())
        assertTrue(siblingFile.exists())
    }

    private fun oversizedStream(bytesTotal: Int): InputStream = object : InputStream() {
        private var remaining = bytesTotal
        override fun read(): Int {
            if (remaining <= 0) return -1
            remaining--
            return 0x41
        }

        override fun read(b: ByteArray, off: Int, len: Int): Int {
            if (remaining <= 0) return -1
            val chunk = minOf(len, remaining)
            remaining -= chunk
            return chunk
        }
    }
}
