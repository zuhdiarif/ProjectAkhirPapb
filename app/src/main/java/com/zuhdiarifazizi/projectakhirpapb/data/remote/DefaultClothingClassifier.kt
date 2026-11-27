package com.zuhdiarifazizi.projectakhirpapb.data.remote

import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClassificationResult
import com.zuhdiarifazizi.projectakhirpapb.domain.repository.ClothingClassifier

class DefaultClothingClassifier(
    apiKey: String = "",
    hsvColorExtractor: HsvColorExtractor = HsvColorExtractor(),
    private val delegate: ClothingClassifier = GeminiVisionClothingClassifier(
        apiKey = apiKey,
        hsvColorExtractor = hsvColorExtractor
    )
) : ClothingClassifier {

    override suspend fun classify(imageBytes: ByteArray): Result<ClassificationResult> =
        delegate.classify(imageBytes)
}
