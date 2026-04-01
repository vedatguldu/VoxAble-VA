package com.voxable.feature_currency.domain.usecase

import com.voxable.feature_currency.domain.model.CurrencyRecognitionResult
import com.voxable.feature_currency.domain.repository.CurrencyRecognitionRepository
import javax.inject.Inject

class SpeakCurrencyResultUseCase @Inject constructor(
    private val repository: CurrencyRecognitionRepository
) {
    operator fun invoke(result: CurrencyRecognitionResult) {
        repository.speakResult(result.toSpokenText())
    }

    fun speakText(text: String) {
        repository.speakResult(text)
    }

    fun stop() {
        repository.stopSpeaking()
    }

    fun isSpeaking(): Boolean = repository.isSpeaking()

    fun shutdown() {
        repository.shutdown()
    }
}
