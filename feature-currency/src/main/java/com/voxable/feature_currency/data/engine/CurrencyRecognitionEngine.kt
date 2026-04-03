package com.voxable.feature_currency.data.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import com.voxable.feature_currency.data.local.CurrencyDataProvider
import com.voxable.feature_currency.domain.model.Currency
import com.voxable.feature_currency.domain.model.CurrencyRecognitionResult
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import kotlin.math.exp
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class CurrencyRecognitionEngine @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private data class VisualPrediction(
        val currency: Currency,
        val confidence: Float
    )

    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val allCurrencies by lazy { CurrencyDataProvider.getAllCurrencies() }
    private val modelInterpreter: Interpreter? by lazy { createInterpreter() }

    private val symbolPatterns = listOf(
        "₺" to "TRY", "$" to "USD", "€" to "EUR", "£" to "GBP",
        "¥" to "JPY", "₹" to "INR", "₽" to "RUB", "₩" to "KRW",
        "฿" to "THB", "﷼" to "SAR"
    )

    suspend fun recognizeFromImage(imageUri: Uri): CurrencyRecognitionResult {
        val extractedText = extractTextFromImage(imageUri)
        val visualPrediction = analyzeImageWithModel(imageUri)
        return analyzeText(extractedText, visualPrediction)
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

    private fun analyzeText(text: String, visualPrediction: VisualPrediction? = null): CurrencyRecognitionResult {
        if (text.isBlank()) {
            if (visualPrediction != null && visualPrediction.confidence >= 0.55f) {
                return CurrencyRecognitionResult(
                    currency = visualPrediction.currency,
                    detectedDenomination = null,
                    detectedText = "",
                    confidence = visualPrediction.confidence,
                    summary = "Model tahmini: ${visualPrediction.currency.name} (%${"%.0f".format(visualPrediction.confidence * 100)})"
                )
            }
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
        val bestMatch = determineBestMatch(symbolMatch, keywordMatches, text, visualPrediction)

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
        fullText: String,
        visualPrediction: VisualPrediction?
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
            val baseConfidence = calculateConfidence(matchedCurrency, symbolMatch, topKeyword.second)
            val confidence = applyVisualBoost(baseConfidence, matchedCurrency, visualPrediction)
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
            val baseConfidence = (top.second.toFloat() / top.first.banknoteKeywords.size).coerceAtMost(0.95f)
            val confidence = applyVisualBoost(baseConfidence, top.first, visualPrediction)
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
            val confidence = applyVisualBoost(symbolMatch.second, symbolMatch.first, visualPrediction)
            return CurrencyRecognitionResult(
                currency = symbolMatch.first,
                detectedDenomination = denomination,
                detectedText = fullText,
                confidence = confidence,
                summary = buildSummary(symbolMatch.first, denomination, confidence)
            )
        }

        if (visualPrediction != null && visualPrediction.confidence >= 0.6f) {
            return CurrencyRecognitionResult(
                currency = visualPrediction.currency,
                detectedDenomination = null,
                detectedText = fullText,
                confidence = visualPrediction.confidence,
                summary = "Model tahmini: ${visualPrediction.currency.name} (%${"%.0f".format(visualPrediction.confidence * 100)})"
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

    private fun applyVisualBoost(
        baseConfidence: Float,
        currency: Currency,
        visualPrediction: VisualPrediction?
    ): Float {
        if (visualPrediction == null) return baseConfidence
        val boosted = if (visualPrediction.currency.code == currency.code) {
            baseConfidence + (visualPrediction.confidence * 0.12f)
        } else {
            baseConfidence - 0.05f
        }
        return boosted.coerceIn(0f, 0.99f)
    }

    private fun analyzeImageWithModel(imageUri: Uri): VisualPrediction? {
        val interpreter = modelInterpreter ?: return null
        val bitmap = context.contentResolver.openInputStream(imageUri)?.use(BitmapFactory::decodeStream) ?: return null
        return try {
            val inputTensor = interpreter.getInputTensor(0)
            val inputShape = inputTensor.shape()
            val targetHeight = inputShape.getOrElse(1) { 224 }
            val targetWidth = inputShape.getOrElse(2) { 224 }
            val resized = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
            val inputBuffer = toInputBuffer(resized, inputTensor.dataType())

            val outputTensor = interpreter.getOutputTensor(0)
            val outputShape = outputTensor.shape()
            val outputSize = outputShape.fold(1) { acc, v -> acc * v }
            val rawScores = FloatArray(outputSize)

            when (outputTensor.dataType()) {
                DataType.FLOAT32 -> {
                    interpreter.run(inputBuffer, rawScores)
                }
                DataType.UINT8 -> {
                    val out = ByteArray(outputSize)
                    interpreter.run(inputBuffer, out)
                    for (i in out.indices) {
                        rawScores[i] = (out[i].toInt() and 0xFF) / 255f
                    }
                }
                DataType.INT8 -> {
                    val out = ByteArray(outputSize)
                    interpreter.run(inputBuffer, out)
                    for (i in out.indices) {
                        rawScores[i] = (out[i].toInt() + 128) / 255f
                    }
                }
                else -> return null
            }

            val scores = normalizeScores(rawScores)
            val bestIndex = scores.indices.maxByOrNull { scores[it] } ?: return null
            val bestConfidence = scores[bestIndex]
            val currency = allCurrencies[bestIndex % allCurrencies.size]
            VisualPrediction(currency = currency, confidence = bestConfidence)
        } catch (_: Exception) {
            null
        } finally {
            if (!bitmap.isRecycled) bitmap.recycle()
        }
    }

    private fun toInputBuffer(bitmap: Bitmap, dataType: DataType): ByteBuffer {
        val width = bitmap.width
        val height = bitmap.height
        val channels = 3
        val bytesPerChannel = when (dataType) {
            DataType.FLOAT32 -> 4
            DataType.UINT8, DataType.INT8 -> 1
            else -> 4
        }
        val buffer = ByteBuffer.allocateDirect(width * height * channels * bytesPerChannel)
        buffer.order(ByteOrder.nativeOrder())

        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        for (pixel in pixels) {
            val r = (pixel shr 16 and 0xFF)
            val g = (pixel shr 8 and 0xFF)
            val b = (pixel and 0xFF)
            when (dataType) {
                DataType.FLOAT32 -> {
                    buffer.putFloat(r / 255f)
                    buffer.putFloat(g / 255f)
                    buffer.putFloat(b / 255f)
                }
                DataType.UINT8, DataType.INT8 -> {
                    buffer.put(r.toByte())
                    buffer.put(g.toByte())
                    buffer.put(b.toByte())
                }
                else -> {
                    buffer.putFloat(r / 255f)
                    buffer.putFloat(g / 255f)
                    buffer.putFloat(b / 255f)
                }
            }
        }
        buffer.rewind()
        return buffer
    }

    private fun normalizeScores(values: FloatArray): FloatArray {
        if (values.isEmpty()) return values
        val max = values.maxOrNull() ?: 0f
        val expValues = FloatArray(values.size)
        var sum = 0f
        for (i in values.indices) {
            val e = exp(values[i] - max)
            expValues[i] = e
            sum += e
        }
        if (sum <= 0f) return values
        for (i in expValues.indices) {
            expValues[i] = expValues[i] / sum
        }
        return expValues
    }

    private fun createInterpreter(): Interpreter? {
        return try {
            val fileDescriptor = context.assets.openFd("models/currency_classifier.tflite")
            FileInputStream(fileDescriptor.fileDescriptor).use { input ->
                val channel = input.channel
                val mappedModel = channel.map(
                    FileChannel.MapMode.READ_ONLY,
                    fileDescriptor.startOffset,
                    fileDescriptor.declaredLength
                )
                Interpreter(mappedModel)
            }
        } catch (_: Exception) {
            null
        }
    }

    fun release() {
        textRecognizer.close()
        modelInterpreter?.close()
    }
}
