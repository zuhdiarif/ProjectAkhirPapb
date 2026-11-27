package com.zuhdiarifazizi.projectakhirpapb.domain.model

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

data class HsvColor(
    val hue: Float,
    val saturation: Float,
    val value: Float,
    val colorName: String
) {
    companion object {
        private const val BACKGROUND_DIFF_THRESHOLD = 42

        fun defaultNeutral(): HsvColor = HsvColor(0f, 0.02f, 0.52f, "Abu-abu")

        fun fromRgbPixels(pixels: IntArray, width: Int, height: Int): HsvColor {
            if (width <= 0 || height <= 0 || pixels.size < width * height) return defaultNeutral()

            val borderAvg = averageBorderRgb(pixels, width, height)

            val startX = width / 4
            val endX = max(startX + 1, (width * 3) / 4)
            val startY = height / 4
            val endY = max(startY + 1, (height * 3) / 4)
            val stepX = max(1, (endX - startX) / 24)
            val stepY = max(1, (endY - startY) / 24)

            var sumR = 0L
            var sumG = 0L
            var sumB = 0L
            var fgCount = 0L

            var fallbackR = 0L
            var fallbackG = 0L
            var fallbackB = 0L
            var fallbackCount = 0L

            var y = startY
            while (y < endY) {
                var x = startX
                while (x < endX) {
                    val argb = pixels[y * width + x]
                    val alpha = (argb ushr 24) and 0xFF
                    if (alpha >= 128) {
                        val r = (argb ushr 16) and 0xFF
                        val g = (argb ushr 8) and 0xFF
                        val b = argb and 0xFF
                        fallbackR += r
                        fallbackG += g
                        fallbackB += b
                        fallbackCount++

                        if (borderAvg == null || colorDistance(r, g, b, borderAvg) > BACKGROUND_DIFF_THRESHOLD) {
                            sumR += r
                            sumG += g
                            sumB += b
                            fgCount++
                        }
                    }
                    x += stepX
                }
                y += stepY
            }

            val finalCount = if (fgCount > 0L) fgCount else fallbackCount
            if (finalCount == 0L) return defaultNeutral()

            val avgR = ((if (fgCount > 0L) sumR else fallbackR) / finalCount).toInt().coerceIn(0, 255)
            val avgG = ((if (fgCount > 0L) sumG else fallbackG) / finalCount).toInt().coerceIn(0, 255)
            val avgB = ((if (fgCount > 0L) sumB else fallbackB) / finalCount).toInt().coerceIn(0, 255)

            val (hue, saturation, value) = rgbToHsv(avgR, avgG, avgB)
            return HsvColor(
                hue = hue,
                saturation = saturation,
                value = value,
                colorName = resolveIndonesianColorName(hue, saturation, value)
            )
        }

        fun rgbToHsv(red: Int, green: Int, blue: Int): Triple<Float, Float, Float> {
            val r = red.coerceIn(0, 255) / 255f
            val g = green.coerceIn(0, 255) / 255f
            val b = blue.coerceIn(0, 255) / 255f

            val maxVal = max(r, max(g, b))
            val minVal = min(r, min(g, b))
            val delta = maxVal - minVal

            val hue = when {
                delta < 1e-5f -> 0f
                maxVal == r -> 60f * (((g - b) / delta) % 6f)
                maxVal == g -> 60f * (((b - r) / delta) + 2f)
                else -> 60f * (((r - g) / delta) + 4f)
            }.let { h -> if (h < 0f) h + 360f else h }

            val saturation = if (maxVal < 1e-5f) 0f else delta / maxVal
            return Triple(hue, saturation, maxVal)
        }

        fun resolveIndonesianColorName(hue: Float, saturation: Float, value: Float): String {
            if (value <= 0.19f) return "Hitam"
            if (value >= 0.85f && saturation <= 0.16f) return "Putih"
            if (saturation <= 0.16f) return "Abu-abu"

            val deg = ((hue % 360f) + 360f) % 360f
            return when {
                deg in 25f..55f && saturation in 0.16f..0.40f && value >= 0.72f -> "Cream"
                deg < 16f || deg >= 340f -> "Merah"
                deg < 50f -> if (value < 0.65f) "Cokelat" else "Cream"
                deg < 155f -> "Olive"
                deg < 250f -> when {
                    value < 0.38f -> "Biru dongker"
                    saturation <= 0.65f -> "Biru denim"
                    else -> "Biru"
                }
                else -> if (value < 0.35f) "Hitam" else "Merah"
            }
        }

        private fun averageBorderRgb(pixels: IntArray, width: Int, height: Int): IntArray? {
            if (width < 4 || height < 4) return null
            var r = 0L
            var g = 0L
            var b = 0L
            var count = 0L
            val step = max(1, min(width, height) / 16)
            for (x in 0 until width step step) {
                for (y in intArrayOf(0, height - 1)) {
                    val p = pixels[y * width + x]
                    if (((p ushr 24) and 0xFF) >= 128) {
                        r += (p ushr 16) and 0xFF
                        g += (p ushr 8) and 0xFF
                        b += p and 0xFF
                        count++
                    }
                }
            }
            return if (count == 0L) null else intArrayOf((r / count).toInt(), (g / count).toInt(), (b / count).toInt())
        }

        private fun colorDistance(r: Int, g: Int, b: Int, border: IntArray): Int =
            abs(r - border[0]) + abs(g - border[1]) + abs(b - border[2])
    }
}
