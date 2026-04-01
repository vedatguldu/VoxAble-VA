package com.voxable.feature_reader.domain.usecase

import android.graphics.Bitmap
import com.voxable.feature_reader.data.ocr.OcrResult
import com.voxable.feature_reader.data.ocr.TextCorrectionUtil
import com.voxable.feature_reader.domain.repository.OcrEngineRepository
import javax.inject.Inject

class OcrImageUseCase @Inject constructor(
    private val ocrEngineRepository: OcrEngineRepository
) {
    suspend operator fun invoke(bitmap: Bitmap, language: String = "tr"): OcrResult {
        val text = ocrEngineRepository.recognizeHybrid(bitmap, language)
        return if (text.isBlank()) {
            OcrResult.Error("Görselden metin tanınamadı")
        } else {
            OcrResult.Success(TextCorrectionUtil.correct(text, language))
        }
    }
}
