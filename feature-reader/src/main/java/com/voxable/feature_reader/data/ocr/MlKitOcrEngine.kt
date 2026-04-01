package com.voxable.feature_reader.data.ocr

import android.content.Context
import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import com.google.mlkit.vision.text.devanagari.DevanagariTextRecognizerOptions
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await

class MlKitOcrEngine(private val context: Context) : OcrEngine {
    override suspend fun recognizeText(bitmap: Bitmap, language: String): OcrResult {
        return try {
            val image = InputImage.fromBitmap(bitmap, 0)
            val recognizer = when (language) {
                "zh", "zh-cn", "chinese" -> TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())
                "ja", "japanese" -> TextRecognition.getClient(JapaneseTextRecognizerOptions.Builder().build())
                "ko", "korean" -> TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())
                "hi", "devanagari" -> TextRecognition.getClient(DevanagariTextRecognizerOptions.Builder().build())
                else -> TextRecognition.getClient(TextRecognizerOptions.DEFAULT)
            }
            val result = recognizer.process(image).await()
            recognizer.close()
            val text = result.text.orEmpty().trim()
            if (text.isBlank()) OcrResult.Error("Metin bulunamadı") else OcrResult.Success(text)
        } catch (e: Exception) {
            OcrResult.Error("ML Kit OCR hatası: ${e.localizedMessage}")
        }
    }
}
