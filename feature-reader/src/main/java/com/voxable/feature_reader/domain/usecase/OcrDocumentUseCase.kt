package com.voxable.feature_reader.domain.usecase

import android.net.Uri
import com.voxable.core.util.Resource
import com.voxable.feature_reader.domain.repository.OcrEngineRepository
import javax.inject.Inject

class OcrDocumentUseCase @Inject constructor(
    private val repository: OcrEngineRepository
) {
    suspend operator fun invoke(uri: Uri): Resource<String> = recognizeHybrid(uri)

    suspend fun recognizeHybrid(uri: Uri, language: String = "tr"): Resource<String> {
        return runCatching { repository.recognizeFromUri(uri) }
            .fold(
                onSuccess = { text ->
                    if (text.isBlank()) Resource.Error("OCR sonucu boş döndü") else Resource.Success(text)
                },
                onFailure = { throwable ->
                    Resource.Error(throwable.message ?: "OCR sırasında hata oluştu", throwable)
                }
            )
    }
}
