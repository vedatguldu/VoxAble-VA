package com.voxable.feature_currency.domain.repository

import android.net.Uri
import com.voxable.core.util.Resource
import com.voxable.feature_currency.domain.model.CurrencyRecognitionResult

interface CurrencyRecognitionRepository {
    suspend fun recognizeFromImage(imageUri: Uri): Resource<CurrencyRecognitionResult>
    suspend fun recognizeFromText(extractedText: String): Resource<CurrencyRecognitionResult>
    fun speakResult(text: String)
    fun stopSpeaking()
    fun isSpeaking(): Boolean
    fun shutdown()
}
