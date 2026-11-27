package com.zuhdiarifazizi.projectakhirpapb.data.device

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.io.File
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ClothingPhotoStorageTest {
    @Test
    fun importedGalleryPhotoIsCopiedToPrivateStorageAndCanBeReloaded() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val source = File(context.cacheDir, "source-test-photo.png")
        source.outputStream().use { output ->
            Bitmap.createBitmap(32, 48, Bitmap.Config.ARGB_8888)
                .compress(Bitmap.CompressFormat.PNG, 100, output)
        }

        val storage = ClothingPhotoStorage(context)
        val savedUri = kotlinx.coroutines.runBlocking {
            storage.importPhoto(Uri.fromFile(source).toString())
        }
        try {
            assertNotEquals(Uri.fromFile(source).toString(), savedUri)
            val persistedBitmap = context.contentResolver.openInputStream(Uri.parse(savedUri))?.use {
                BitmapFactory.decodeStream(it)
            }
            assertNotNull(persistedBitmap)
            assertTrue(File(Uri.parse(savedUri).path!!).exists())
        } finally {
            kotlinx.coroutines.runBlocking { storage.deleteStoredPhoto(savedUri) }
            source.delete()
        }
    }
}
