package com.voxable.feature_currency.domain.model

data class CurrencyRecognitionResult(
    val currency: Currency?,
    val detectedDenomination: Int?,
    val detectedText: String,
    val confidence: Float,
    val summary: String
) {
    val isSuccessful: Boolean get() = currency != null && confidence > 0.3f

    fun toSpokenText(): String {
        if (!isSuccessful || currency == null) {
            return "Para birimi tanınamadı. Lütfen banknotu daha net gösterin."
        }
        val denominationText = detectedDenomination?.let { "$it " } ?: ""
        return "$denominationText${currency.name}. ${currency.country} para birimi. Sembolü: ${currency.symbol}"
    }
}
