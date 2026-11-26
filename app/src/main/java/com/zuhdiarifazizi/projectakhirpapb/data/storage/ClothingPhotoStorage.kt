package com.zuhdiarifazizi.projectakhirpapb.data.storage

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.URI
import java.util.UUID
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Stores imported and captured garment photos in app-private storage with URI scheme & size guards. */
class ClothingPhotoStorage internal constructor(
    private val photosDirectory: File,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val openSourceStream: (String) -> InputStream?
) {
    constructor(context: Context) : this(
        photosDirectory = File(context.applicationContext.filesDir, PHOTO_DIRECTORY),
        ioDispatcher = Dispatchers.IO,
        openSourceStream = { sourceUri ->
            context.applicationContext.contentResolver.openInputStream(Uri.parse(sourceUri))
        }
    )

    suspend fun importPhoto(sourceUri: String): String = withContext(ioDispatcher) {
        val scheme = runCatching { URI(sourceUri).scheme?.lowercase() }.getOrNull()
        require(scheme == "content" || scheme == "file") {
            "Sumber foto tidak diizinkan."
        }
        val target = newPhotoFile()
        try {
            val input = openSourceStream(sourceUri)
                ?: error("Foto tidak dapat dibuka.")
            input.use { inputStream ->
                FileOutputStream(target).use { output ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    var totalBytes = 0L
                    while (true) {
                        val read = inputStream.read(buffer)
                        if (read <= 0) break
                        totalBytes += read
                        if (totalBytes > MAX_PHOTO_BYTES) {
                            error("Ukuran foto melebihi batas maksimal 15 MB.")
                        }
                        output.write(buffer, 0, read)
                    }
                }
            }
            target.toURI().toString()
        } catch (error: Throwable) {
            target.delete()
            throw error
        }
    }

    fun createCameraOutputFile(): File {
        check(photosDirectory.exists() || photosDirectory.mkdirs()) {
            "Folder foto pakaian tidak dapat dibuat."
        }
        return File(photosDirectory, "${UUID.randomUUID()}.jpg")
    }

    suspend fun deleteStoredPhoto(photoUri: String?) = withContext(ioDispatcher) {
        if (photoUri.isNullOrBlank()) return@withContext
        runCatching {
            val uri = URI(photoUri)
            if (!uri.scheme.equals("file", ignoreCase = true)) return@runCatching
            val file = File(uri).canonicalFile
            if (file.parentFile == photosDirectory.canonicalFile) file.delete()
        }
    }

    private fun newPhotoFile(): File {
        check(photosDirectory.exists() || photosDirectory.mkdirs()) {
            "Folder foto pakaian tidak dapat dibuat."
        }
        return File(photosDirectory, "${UUID.randomUUID()}.jpg")
    }

    private companion object {
        const val PHOTO_DIRECTORY = "clothing-images"
        const val MAX_PHOTO_BYTES = 15L * 1024L * 1024L
    }
}
