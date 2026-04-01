package com.voxable.feature_currency.data.repository

import android.net.Uri
import com.voxable.core.util.Resource
import com.voxable.feature_currency.data.engine.CurrencyRecognitionEngine
import com.voxable.feature_currency.data.engine.CurrencyTtsEngine
import com.voxable.feature_currency.domain.model.CurrencyRecognitionResult
import com.voxable.feature_currency.domain.repository.CurrencyRecognitionRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CurrencyRecognitionRepositoryImpl @Inject constructor(
    private val recognitionEngine: CurrencyRecognitionEngine,
    private val ttsEngine: CurrencyTtsEngine
) : CurrencyRecognitionRepository {

    override suspend fun recognizeFromImage(imageUri: Uri): Resource<CurrencyRecognitionResult> {
        return try {
            val result = recognitionEngine.recognizeFromImage(imageUri)
            Resource.Success(result)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Görüntü tanıma sırasında hata oluştu")
        }
    }

    override suspend fun recognizeFromText(extractedText: String): Resource<CurrencyRecognitionResult> {
        return try {
            val result = recognitionEngine.recognizeFromText(extractedText)
            Resource.Success(result)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Metin analizi sırasında hata oluştu")
        }
    }

    override fun speakResult(text: String) {
        ttsEngine.speak(text)
    }

    override fun stopSpeaking() {
        ttsEngine.stop()
    }

    override fun isSpeaking(): Boolean = ttsEngine.isSpeakingNow()

    override fun shutdown() {
        ttsEngine.shutdown()
        recognitionEngine.release()
    }
}
