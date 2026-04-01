package com.voxable.feature_reader.data.ocr

import android.content.Context
import android.graphics.Bitmap
import com.googlecode.tesseract.android.TessBaseAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class TesseractOcrEngine(private val context: Context) : OcrEngine {

    override suspend fun recognizeText(bitmap: Bitmap, language: String): OcrResult = withContext(Dispatchers.IO) {
        val tessLanguage = mapLanguage(language)
        val tessDataDir = File(context.filesDir, "tesseract/tessdata")
        if (!ensureTrainedData(tessDataDir, tessLanguage)) {
            return@withContext OcrResult.Error("Tesseract eğitim verisi bulunamadı: $tessLanguage.traineddata")
        }

        val api = TessBaseAPI()
        try {
            val dataPath = tessDataDir.parentFile?.absolutePath.orEmpty()
            if (!api.init(dataPath, tessLanguage)) {
                return@withContext OcrResult.Error("Tesseract başlatılamadı: $tessLanguage")
            }
            api.setImage(bitmap)
            api.pageSegMode = TessBaseAPI.PageSegMode.PSM_AUTO
            api.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789ÇĞİÖŞÜçğıöşü.,;:!?()[]{}<>@#%&*+-=/\\\"' ₺€$")
            val text = api.utF8Text.orEmpty().trim()
            if (text.isBlank()) OcrResult.Error("Tesseract ile metin bulunamadı") else OcrResult.Success(text)
        } catch (e: Exception) {
            OcrResult.Error("Tesseract OCR hatası: ${e.localizedMessage}")
        } finally {
            api.end()
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

    private fun mapLanguage(language: String): String {
        return when (language.lowercase()) {
            "tr", "tr-tr", "tur", "latin" -> "tur"
            "en", "eng", "en-us", "en-gb" -> "eng"
            "de", "deu" -> "deu"
            "fr", "fra" -> "fra"
            "es", "spa" -> "spa"
            "it", "ita" -> "ita"
            "pt", "por" -> "por"
            else -> language.lowercase().ifBlank { "eng" }
        }
    }
}
