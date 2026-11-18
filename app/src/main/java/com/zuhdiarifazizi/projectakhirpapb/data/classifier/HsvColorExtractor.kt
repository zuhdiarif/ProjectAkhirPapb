package com.zuhdiarifazizi.projectakhirpapb.data.classifier

import android.graphics.Bitmap
import com.zuhdiarifazizi.projectakhirpapb.domain.model.HsvColor

class HsvColorExtractor {

    fun extractFromBitmap(bitmap: Bitmap): HsvColor {
        throw NotImplementedError("RGB-to-HSV color extraction not implemented")
    }

    fun extractFromImageBytes(imageBytes: ByteArray): HsvColor {
        throw NotImplementedError("Image byte color extraction not implemented")
    }
}
