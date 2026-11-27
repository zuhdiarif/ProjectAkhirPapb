package com.zuhdiarifazizi.projectakhirpapb.data.device

import android.graphics.ImageFormat
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.zuhdiarifazizi.projectakhirpapb.domain.model.LumaChromaGrid

/**
 * Samples live CameraX YUV_420_888 video frames into a compact 16x16 luma/chroma grid
 * for real-time blind-accessible positioning and lighting feedback.
 */
class CameraXGarmentAnalyzer(
    private val onGridReady: (LumaChromaGrid) -> Unit
) : ImageAnalysis.Analyzer {

    private var lastSampleMillis = 0L

    override fun analyze(image: ImageProxy) {
        try {
            val now = System.currentTimeMillis()
            if (now - lastSampleMillis < FRAME_INTERVAL_MS) return
            if (image.format != ImageFormat.YUV_420_888 || image.planes.size < 3) return
            lastSampleMillis = now

            val width = image.width
            val height = image.height
            if (width <= 0 || height <= 0) return

            val yPlane = image.planes[0]
            val uPlane = image.planes[1]
            val vPlane = image.planes[2]

            val yBuf = yPlane.buffer
            val uBuf = uPlane.buffer
            val vBuf = vPlane.buffer

            val yRowStride = yPlane.rowStride
            val yPixelStride = yPlane.pixelStride
            val uvRowStride = uPlane.rowStride
            val uvPixelStride = uPlane.pixelStride

            val gridW = GRID_SIZE
            val gridH = GRID_SIZE
            val yLuma = IntArray(gridW * gridH)
            val centerPixels = IntArray((gridW / 2) * (gridH / 2))
            var centerIdx = 0

            for (gy in 0 until gridH) {
                val py = ((gy + 0.5f) * height / gridH).toInt().coerceIn(0, height - 1)
                for (gx in 0 until gridW) {
                    val px = ((gx + 0.5f) * width / gridW).toInt().coerceIn(0, width - 1)
                    val yOffset = py * yRowStride + px * yPixelStride
                    val yVal = if (yOffset in 0 until yBuf.limit()) (yBuf.get(yOffset).toInt() and 0xFF) else 128
                    yLuma[gy * gridW + gx] = yVal

                    if (gx in (gridW / 4) until (gridW * 3 / 4) && gy in (gridH / 4) until (gridH * 3 / 4)) {
                        val uvX = px / 2
                        val uvY = py / 2
                        val uOffset = uvY * uvRowStride + uvX * uvPixelStride
                        val vOffset = uvY * vPlane.rowStride + uvX * vPlane.pixelStride
                        val uVal = if (uOffset in 0 until uBuf.limit()) (uBuf.get(uOffset).toInt() and 0xFF) - 128 else 0
                        val vVal = if (vOffset in 0 until vBuf.limit()) (vBuf.get(vOffset).toInt() and 0xFF) - 128 else 0
                        centerPixels[centerIdx++] = yuvToArgb(yVal, uVal, vVal)
                    }
                }
            }

            onGridReady(
                LumaChromaGrid(
                    width = gridW,
                    height = gridH,
                    yLuma = yLuma,
                    centerRgbPixels = centerPixels,
                    centerWidth = gridW / 2,
                    centerHeight = gridH / 2
                )
            )
        } finally {
            image.close()
        }
    }

    private fun yuvToArgb(y: Int, u: Int, v: Int): Int {
        val r = (y + (1.370705f * v)).toInt().coerceIn(0, 255)
        val g = (y - (0.337633f * u) - (0.698001f * v)).toInt().coerceIn(0, 255)
        val b = (y + (1.732446f * u)).toInt().coerceIn(0, 255)
        return (0xFF shl 24) or (r shl 16) or (g shl 8) or b
    }

    private companion object {
        const val GRID_SIZE = 16
        const val FRAME_INTERVAL_MS = 250L
    }
}
