package com.voxable.feature_reader.data.ocr

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
import com.voxable.feature_reader.domain.repository.OcrEngineRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class HybridOcrEngine @Inject constructor(
    @ApplicationContext private val context: Context
) : OcrEngineRepository {

    private fun getRecognizer(language: String): TextRecognizer {
        return when (language.lowercase()) {
            "chinese", "zh" -> TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())
            "japanese", "ja" -> TextRecognition.getClient(JapaneseTextRecognizerOptions.Builder().build())
            "korean", "ko" -> TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())
            "devanagari", "hi" -> TextRecognition.getClient(DevanagariTextRecognizerOptions.Builder().build())
            else -> TextRecognition.getClient(TextRecognizerOptions.Builder().build())
        }
    }

    override suspend fun recognizeWithMlKit(bitmap: Bitmap, language: String): String =
        suspendCancellableCoroutine { cont ->
            val image = InputImage.fromBitmap(bitmap, 0)
            val recognizer = getRecognizer(language)
            recognizer.process(image)
                .addOnSuccessListener { result -> cont.resume(result.text) }
                .addOnFailureListener { e -> cont.resumeWithException(e) }
        }

    override suspend fun recognizeWithTesseract(bitmap: Bitmap, language: String): String =
        withContext(Dispatchers.IO) {
            try {
                val tessDataPath = context.getExternalFilesDir(null)?.absolutePath ?: context.filesDir.absolutePath
                val tessDir = java.io.File(tessDataPath, "tessdata")
                if (!tessDir.exists()) tessDir.mkdirs()

                val tess = com.googlecode.tesseract.android.TessBaseAPI()
                tess.init(tessDataPath, language)
                tess.setImage(bitmap)
                val result = tess.utF8Text ?: ""
                tess.end()
                result
            } catch (e: Exception) {
                ""
            }
        }

    override suspend fun recognizeHybrid(bitmap: Bitmap, language: String): String {
        return try {
            val mlKitResult = recognizeWithMlKit(bitmap, language)
            if (mlKitResult.length >= 20) {
                mlKitResult
            } else {
                val tessResult = recognizeWithTesseract(bitmap, if (language == "latin") "tur" else language)
                if (tessResult.length > mlKitResult.length) tessResult else mlKitResult
            }
        } catch (e: Exception) {
            try {
                recognizeWithTesseract(bitmap, if (language == "latin") "tur" else language)
            } catch (e2: Exception) {
                ""
            }
        }
    }

    override suspend fun recognizeFromUri(uri: Uri): String = withContext(Dispatchers.IO) {
        val inputStream = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()
        if (bitmap != null) {
            recognizeHybrid(bitmap, "latin")
        } else {
            ""
        }
    }
}
