package com.zuhdiarifazizi.projectakhirpapb.data.remote

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.zuhdiarifazizi.projectakhirpapb.domain.model.HsvColor
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Extracts dominant RGB pixels from the center garment region (with simple outer-border
 * background suppression) and converts them into cylindrical HSV coordinates.
 */
class HsvColorExtractor {

    fun extractFromImageBytes(imageBytes: ByteArray): HsvColor {
        if (imageBytes.isEmpty()) return defaultNeutralColor()
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size, bounds)
        val sampleSize = computeSampleSize(bounds.outWidth, bounds.outHeight, MAX_DECODE_DIM)
        val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size, options)
            ?: return defaultNeutralColor()
        return try {
            extractFromBitmap(bitmap)
        } finally {
            bitmap.recycle()
        }
    }

    fun extractFromUri(context: Context, uriString: String): HsvColor {
        val uri = Uri.parse(uriString)
        val scheme = uri.scheme?.lowercase()
        if (scheme != "content" && scheme != "file") return defaultNeutralColor()
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: return defaultNeutralColor()
        return extractFromImageBytes(bytes)
    }

    fun extractFromBitmap(bitmap: Bitmap): HsvColor {
        val width = bitmap.width
        val height = bitmap.height
        if (width <= 0 || height <= 0) return defaultNeutralColor()
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        return extractFromRgbPixels(pixels, width, height)
    }

    fun extractFromRgbPixels(pixels: IntArray, width: Int, height: Int): HsvColor =
        HsvColor.fromRgbPixels(pixels, width, height)

    fun rgbToHsv(red: Int, green: Int, blue: Int): Triple<Float, Float, Float> =
        HsvColor.rgbToHsv(red, green, blue)

    fun resolveIndonesianColorName(hue: Float, saturation: Float, value: Float): String =
        HsvColor.resolveIndonesianColorName(hue, saturation, value)

    private fun computeSampleSize(width: Int, height: Int, maxDim: Int): Int {
        var sample = 1
        while (width / sample > maxDim || height / sample > maxDim) {
            sample *= 2
        }
        return sample
    }

    private fun defaultNeutralColor(): HsvColor = HsvColor.defaultNeutral()

    private companion object {
        const val MAX_DECODE_DIM = 512
    }
}
