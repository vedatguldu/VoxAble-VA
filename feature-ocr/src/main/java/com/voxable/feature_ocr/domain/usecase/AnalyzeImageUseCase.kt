package com.voxable.feature_ocr.domain.usecase

import android.net.Uri
import com.voxable.core.util.Resource
import com.voxable.feature_ocr.domain.model.OcrImageAnalysis
import com.voxable.feature_ocr.domain.repository.OcrRepository
import javax.inject.Inject

class AnalyzeImageUseCase @Inject constructor(
    private val repository: OcrRepository
) {
    suspend operator fun invoke(imageUri: Uri, language: String = "tr"): Resource<OcrImageAnalysis> {
        return repository.analyzeImage(imageUri, language)
    }
}
