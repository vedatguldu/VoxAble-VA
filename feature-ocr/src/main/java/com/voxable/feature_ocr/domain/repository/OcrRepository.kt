package com.voxable.feature_ocr.domain.repository

import android.net.Uri
import com.voxable.core.util.Resource
import com.voxable.feature_ocr.domain.model.OcrImageAnalysis

interface OcrRepository {
    suspend fun recognizeTextFromImage(imageUri: Uri, language: String = "tr"): Resource<String>
    suspend fun analyzeImage(imageUri: Uri, language: String = "tr"): Resource<OcrImageAnalysis>
}
