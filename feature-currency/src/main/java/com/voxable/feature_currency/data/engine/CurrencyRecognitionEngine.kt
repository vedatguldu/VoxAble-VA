package com.voxable.feature_currency.data.engine

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.voxable.feature_currency.data.local.CurrencyDataProvider
import com.voxable.feature_currency.domain.model.Currency
import com.voxable.feature_currency.domain.model.CurrencyRecognitionResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class CurrencyRecognitionEngine @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    private val symbolPatterns = listOf(
        "₺" to "TRY", "$" to "USD", "€" to "EUR", "£" to "GBP",
        "¥" to "JPY", "₹" to "INR", "₽" to "RUB", "₩" to "KRW",
        "฿" to "THB", "﷼" to "SAR"
    )

    suspend fun recognizeFromImage(imageUri: Uri): CurrencyRecognitionResult {
        val extractedText = extractTextFromImage(imageUri)
        return analyzeText(extractedText)
    }

    fun recognizeFromText(text: String): CurrencyRecognitionResult {
        return analyzeText(text)
    }

    private suspend fun extractTextFromImage(imageUri: Uri): String {
        return suspendCancellableCoroutine { continuation ->
            try {
                val image = InputImage.fromFilePath(context, imageUri)
                textRecognizer.process(image)
                    .addOnSuccessListener { visionText ->
                        continuation.resume(visionText.text)
                    }
                    .addOnFailureListener {
                        continuation.resume("")
                    }
            } catch (e: Exception) {
                continuation.resume("")
            }
        }
    }

    private fun analyzeText(text: String): CurrencyRecognitionResult {
        if (text.isBlank()) {
            return CurrencyRecognitionResult(
                currency = null,
                detectedDenomination = null,
                detectedText = "",
                confidence = 0f,
                summary = "Görüntüden metin okunamadı"
            )
        }

        // 1. Para birimi sembollerini ara
        val symbolMatch = findBySymbol(text)

        // 2. Banknot anahtar kelimelerini ara
        val keywordMatches = CurrencyDataProvider.matchByKeywords(text)

        // 3. Sonuçları birleştir ve en iyi eşleşmeyi bul
        val bestMatch = determineBestMatch(symbolMatch, keywordMatches, text)

        return bestMatch
    }

    private fun findBySymbol(text: String): Pair<Currency, Float>? {
        for ((symbol, code) in symbolPatterns) {
            if (text.contains(symbol)) {
                val currency = CurrencyDataProvider.findByCode(code) ?: continue
                return currency to 0.7f
            }
        }
        return null
    }

    private fun determineBestMatch(
        symbolMatch: Pair<Currency, Float>?,
        keywordMatches: List<Pair<Currency, Int>>,
        fullText: String
    ): CurrencyRecognitionResult {
        // Hem sembol hem anahtar kelime eşleşmesi
        if (symbolMatch != null && keywordMatches.isNotEmpty()) {
            val topKeyword = keywordMatches.first()
            val matchedCurrency = if (topKeyword.first.code == symbolMatch.first.code) {
                // Aynı para birimi — yüksek güven
                topKeyword.first
            } else if (topKeyword.second >= 2) {
                // Anahtar kelime daha güçlü
                topKeyword.first
            } else {
                symbolMatch.first
            }
            val denomination = CurrencyDataProvider.findDenomination(fullText, matchedCurrency)
            val confidence = calculateConfidence(matchedCurrency, symbolMatch, topKeyword.second)
            return CurrencyRecognitionResult(
                currency = matchedCurrency,
                detectedDenomination = denomination,
                detectedText = fullText,
                confidence = confidence,
                summary = buildSummary(matchedCurrency, denomination, confidence)
            )
        }

        // Sadece anahtar kelime eşleşmesi
        if (keywordMatches.isNotEmpty()) {
            val top = keywordMatches.first()
            val denomination = CurrencyDataProvider.findDenomination(fullText, top.first)
            val confidence = (top.second.toFloat() / top.first.banknoteKeywords.size).coerceAtMost(0.95f)
            return CurrencyRecognitionResult(
                currency = top.first,
                detectedDenomination = denomination,
                detectedText = fullText,
                confidence = confidence,
                summary = buildSummary(top.first, denomination, confidence)
            )
        }

        // Sadece sembol eşleşmesi
        if (symbolMatch != null) {
            val denomination = CurrencyDataProvider.findDenomination(fullText, symbolMatch.first)
            return CurrencyRecognitionResult(
                currency = symbolMatch.first,
                detectedDenomination = denomination,
                detectedText = fullText,
                confidence = symbolMatch.second,
                summary = buildSummary(symbolMatch.first, denomination, symbolMatch.second)
            )
        }

        // Eşleşme yok
        return CurrencyRecognitionResult(
            currency = null,
            detectedDenomination = null,
            detectedText = fullText,
            confidence = 0f,
            summary = "Para birimi tanınamadı"
        )
    }

    private fun calculateConfidence(
        currency: Currency,
        symbolMatch: Pair<Currency, Float>?,
        keywordCount: Int
    ): Float {
        var confidence = 0f
        if (symbolMatch != null && symbolMatch.first.code == currency.code) {
            confidence += 0.4f
        }
        confidence += (keywordCount.toFloat() * 0.15f).coerceAtMost(0.55f)
        return confidence.coerceAtMost(0.98f)
    }

    private fun buildSummary(currency: Currency, denomination: Int?, confidence: Float): String {
        val conf = "%.0f".format(confidence * 100)
        val denomText = denomination?.let { "$it " } ?: ""
        return "$denomText${currency.name} (${currency.code}) — %$conf güven"
    }

    fun release() {
        textRecognizer.close()
    }
}
