package com.voxable.feature_currency.domain.usecase

import android.net.Uri
import com.voxable.core.util.Resource
import com.voxable.feature_currency.domain.model.CurrencyRecognitionResult
import com.voxable.feature_currency.domain.repository.CurrencyRecognitionRepository
import javax.inject.Inject

class RecognizeCurrencyUseCase @Inject constructor(
    private val repository: CurrencyRecognitionRepository
) {
    suspend fun fromImage(imageUri: Uri): Resource<CurrencyRecognitionResult> {
        return repository.recognizeFromImage(imageUri)
    }

    suspend fun fromText(text: String): Resource<CurrencyRecognitionResult> {
        if (text.isBlank()) return Resource.Error("Tanınacak metin bulunamadı")
        return repository.recognizeFromText(text)
    }
}
