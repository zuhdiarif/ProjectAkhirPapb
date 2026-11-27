package com.zuhdiarifazizi.projectakhirpapb.domain.repository

import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClassificationResult

interface ClothingClassifier {
    suspend fun classify(imageBytes: ByteArray): Result<ClassificationResult>
}
