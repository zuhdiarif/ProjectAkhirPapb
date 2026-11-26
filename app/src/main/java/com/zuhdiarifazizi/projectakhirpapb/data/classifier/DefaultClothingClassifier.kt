package com.zuhdiarifazizi.projectakhirpapb.data.classifier

import com.zuhdiarifazizi.projectakhirpapb.domain.classifier.ClassificationResult
import com.zuhdiarifazizi.projectakhirpapb.domain.classifier.ClothingClassifier

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
