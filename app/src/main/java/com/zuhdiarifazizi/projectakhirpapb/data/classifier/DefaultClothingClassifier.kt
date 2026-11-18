package com.zuhdiarifazizi.projectakhirpapb.data.classifier

import com.zuhdiarifazizi.projectakhirpapb.domain.classifier.ClassificationResult
import com.zuhdiarifazizi.projectakhirpapb.domain.classifier.ClothingClassifier

class DefaultClothingClassifier(
    private val hsvColorExtractor: HsvColorExtractor = HsvColorExtractor()
) : ClothingClassifier {

    override suspend fun classify(imageBytes: ByteArray): Result<ClassificationResult> {
        return Result.failure(NotImplementedError("Clothing classifier not implemented"))
    }
}
