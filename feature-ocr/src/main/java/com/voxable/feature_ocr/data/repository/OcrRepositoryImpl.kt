package com.voxable.feature_ocr.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import com.google.mlkit.vision.text.devanagari.DevanagariTextRecognizerOptions
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.googlecode.tesseract.android.TessBaseAPI
import com.voxable.core.util.Resource
import com.voxable.feature_ocr.domain.repository.OcrRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OcrRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : OcrRepository {

    override suspend fun recognizeTextFromImage(imageUri: Uri, language: String): Resource<String> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val bitmap = context.contentResolver.openInputStream(imageUri)?.use(BitmapFactory::decodeStream)
                    ?: return@withContext Resource.Error("Görüntü açılamadı")
                try {
                    val text = recognizeHybrid(bitmap, language)
                    if (text.isBlank()) Resource.Error("Metin tanınamadı")
                    else Resource.Success(correctText(text, language))
                } finally {
                    if (!bitmap.isRecycled) bitmap.recycle()
                }
            }.getOrElse { throwable ->
                Resource.Error(throwable.message ?: "OCR sırasında hata oluştu", throwable)
            }
        }
    }

    private suspend fun recognizeHybrid(bitmap: Bitmap, language: String): String {
        val mlKitText = runCatching { recognizeWithMlKit(bitmap, language) }.getOrDefault("")
        val tessText = if (shouldTryTesseract(mlKitText)) {
            runCatching { recognizeWithTesseract(bitmap, language) }.getOrDefault("")
        } else {
            ""
        }
        return if (qualityScore(tessText) > qualityScore(mlKitText)) tessText else mlKitText
    }

    private suspend fun recognizeWithMlKit(bitmap: Bitmap, language: String): String {
        val recognizer = getRecognizer(language)
        return try {
            recognizer.process(InputImage.fromBitmap(bitmap, 0)).await().text.orEmpty().trim()
        } finally {
            recognizer.close()
        }
    }

    private suspend fun recognizeWithTesseract(bitmap: Bitmap, language: String): String = withContext(Dispatchers.IO) {
        val tessLanguage = mapLanguage(language)
        val tessDataDir = File(context.filesDir, "tesseract/tessdata")
        if (!ensureTrainedData(tessDataDir, tessLanguage)) return@withContext ""

        val api = TessBaseAPI()
        try {
            val dataPath = tessDataDir.parentFile?.absolutePath.orEmpty()
            if (!api.init(dataPath, tessLanguage)) return@withContext ""
            api.setImage(bitmap)
            api.pageSegMode = TessBaseAPI.PageSegMode.PSM_AUTO
            api.utF8Text.orEmpty().trim()
        } finally {
            api.end()
        }
    }

    private fun getRecognizer(language: String): TextRecognizer {
        return when (language.lowercase()) {
            "zh", "zh-cn", "chinese" -> TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())
            "ja", "japanese" -> TextRecognition.getClient(JapaneseTextRecognizerOptions.Builder().build())
            "ko", "korean" -> TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())
            "hi", "devanagari" -> TextRecognition.getClient(DevanagariTextRecognizerOptions.Builder().build())
            else -> TextRecognition.getClient(TextRecognizerOptions.DEFAULT)
        }
    }

    private fun ensureTrainedData(tessDataDir: File, language: String): Boolean {
        if (!tessDataDir.exists()) tessDataDir.mkdirs()
        val outputFile = File(tessDataDir, "$language.traineddata")
        if (outputFile.exists() && outputFile.length() > 0L) return true
        return runCatching {
            context.assets.open("tessdata/$language.traineddata").use { input ->
                outputFile.outputStream().use { output -> input.copyTo(output) }
            }
            true
        }.getOrDefault(false)
    }

    private fun shouldTryTesseract(text: String): Boolean {
        if (text.isBlank()) return true
        val compact = text.filterNot(Char::isWhitespace)
        return compact.length < 24 || compact.count { it.isLetterOrDigit() } < compact.length * 0.6
    }

    private fun qualityScore(text: String): Int {
        if (text.isBlank()) return 0
        val compact = text.filterNot(Char::isWhitespace)
        return compact.count { it.isLetterOrDigit() } * 2 - compact.count { !it.isLetterOrDigit() } + compact.length
    }

    private fun correctText(text: String, language: String): String {
        var result = text
            .replace("\u00A0", " ")
            .replace(Regex("(?<=[A-Za-zÇĞİÖŞÜçğıöşü])-[\\r\\n]+(?=[A-Za-zÇĞİÖŞÜçğıöşü])"), "")
            .replace(Regex("[\\t ]+"), " ")
            .replace(Regex(" *([,.;:!?])"), "$1")
            .replace(Regex("([,.;:!?])(?=\\S)"), "$1 ")
            .trim()
        if (language.startsWith("tr", ignoreCase = true)) {
            result = result.replace("’", "'").replace("‘", "'").replace("“", "\"").replace("”", "\"")
        }
        return result
    }

    private fun mapLanguage(language: String): String = when (language.lowercase()) {
        "tr", "tr-tr", "tur" -> "tur"
        "en", "eng", "en-us", "en-gb" -> "eng"
        "de", "deu" -> "deu"
        "fr", "fra" -> "fra"
        "es", "spa" -> "spa"
        "it", "ita" -> "ita"
        "pt", "por" -> "por"
        else -> "eng"
    }
}
