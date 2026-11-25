package com.zuhdiarifazizi.projectakhirpapb.data.storage

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Stores imported and captured garment photos in app-private storage. */
class ClothingPhotoStorage(context: Context) {
    private val appContext = context.applicationContext
    private val photosDirectory = File(appContext.filesDir, PHOTO_DIRECTORY)

    suspend fun importPhoto(sourceUri: String): String = withContext(Dispatchers.IO) {
        val source = Uri.parse(sourceUri)
        val target = newPhotoFile()
        try {
            val input = appContext.contentResolver.openInputStream(source)
                ?: error("Foto tidak dapat dibuka.")
            input.use { inputStream ->
                FileOutputStream(target).use { output -> inputStream.copyTo(output) }
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

    suspend fun deleteStoredPhoto(photoUri: String?) = withContext(Dispatchers.IO) {
        if (photoUri.isNullOrBlank()) return@withContext
        runCatching {
            val uri = Uri.parse(photoUri)
            if (uri.scheme != "file") return@runCatching
            val file = File(requireNotNull(uri.path)).canonicalFile
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
    }
}
