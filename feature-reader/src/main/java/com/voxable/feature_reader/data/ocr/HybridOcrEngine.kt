package com.voxable.feature_reader.data.ocr

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.voxable.feature_reader.domain.repository.OcrEngineRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HybridOcrEngine @Inject constructor(
    @ApplicationContext private val context: Context
) : OcrEngineRepository {

    private val mlKitEngine = MlKitOcrEngine(context)
    private val tesseractEngine = TesseractOcrEngine(context)

    override suspend fun recognizeWithMlKit(bitmap: Bitmap, language: String): String {
        return when (val result = mlKitEngine.recognizeText(bitmap, language)) {
            is OcrResult.Success -> TextCorrectionUtil.correct(result.text, language)
            is OcrResult.Error -> ""
        }
    }

    override suspend fun recognizeWithTesseract(bitmap: Bitmap, language: String): String {
        return when (val result = tesseractEngine.recognizeText(bitmap, language)) {
            is OcrResult.Success -> TextCorrectionUtil.correct(result.text, language)
            is OcrResult.Error -> ""
        }
    }

    override suspend fun recognizeHybrid(bitmap: Bitmap, language: String): String {
        val normalizedLanguage = language.ifBlank { "tr" }
        val mlKitText = runCatching { recognizeWithMlKit(bitmap, normalizedLanguage) }.getOrDefault("")
        val tessText = if (shouldTryTesseract(mlKitText)) {
            runCatching { recognizeWithTesseract(bitmap, normalizedLanguage) }.getOrDefault("")
        } else {
            ""
        }
        return selectBestResult(mlKitText, tessText)
    }

    override suspend fun recognizeFromUri(uri: Uri): String = withContext(Dispatchers.IO) {
        context.contentResolver.openInputStream(uri)?.use(BitmapFactory::decodeStream)?.let { bitmap ->
            try {
                recognizeHybrid(bitmap, "tr")
            } finally {
                if (!bitmap.isRecycled) bitmap.recycle()
            }
        }.orEmpty()
    }

    private fun shouldTryTesseract(mlKitText: String): Boolean {
        if (mlKitText.isBlank()) return true
        val compact = mlKitText.filterNot(Char::isWhitespace)
        val alphaNumericCount = compact.count { it.isLetterOrDigit() }
        return compact.length < 32 || alphaNumericCount < compact.length * 0.55
    }

    private fun selectBestResult(primary: String, fallback: String): String {
        val primaryScore = qualityScore(primary)
        val fallbackScore = qualityScore(fallback)
        return when {
            fallbackScore > primaryScore -> fallback
            primary.isNotBlank() -> primary
            else -> fallback
        }
    }

    private fun qualityScore(text: String): Int {
        if (text.isBlank()) return 0
        val compact = text.filterNot(Char::isWhitespace)
        val letters = compact.count { it.isLetter() }
        val digits = compact.count { it.isDigit() }
        val punctuation = compact.count { !it.isLetterOrDigit() }
        return (letters * 3) + digits - punctuation + compact.length
    }
}
