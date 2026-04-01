package com.voxable.feature_reader.data.ocr

import android.graphics.Bitmap

interface OcrEngine {
    /**
     * Verilen bitmap üzerinde OCR işlemi yapar.
     * @param bitmap Görüntü
     * @param language ISO dil kodu (örn. "tr", "en", "de")
     * @return Tanınan metin veya hata mesajı
     */
    suspend fun recognizeText(bitmap: Bitmap, language: String = "tr"): OcrResult
}

sealed class OcrResult {
    data class Success(val text: String) : OcrResult()
    data class Error(val message: String) : OcrResult()
}
