package com.voxable.feature_ocr.domain.usecase

import android.net.Uri
import com.voxable.core.util.Resource
import com.voxable.feature_ocr.domain.repository.OcrRepository
import javax.inject.Inject

class RecognizeTextUseCase @Inject constructor(
    private val repository: OcrRepository
) {
    suspend operator fun invoke(imageUri: Uri): Resource<String> {
        return repository.recognizeTextFromImage(imageUri)
    }
}
