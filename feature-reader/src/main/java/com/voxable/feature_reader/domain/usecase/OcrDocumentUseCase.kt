package com.voxable.feature_reader.domain.usecase

import android.net.Uri
import com.voxable.feature_reader.domain.repository.OcrEngineRepository
import javax.inject.Inject

class OcrDocumentUseCase @Inject constructor(
    private val repository: OcrEngineRepository
) {
    suspend operator fun invoke(uri: Uri): String {
        return repository.recognizeFromUri(uri)
    }
}
