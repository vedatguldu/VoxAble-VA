package com.voxable.feature_reader.domain.usecase

import android.graphics.Bitmap
import com.voxable.feature_reader.data.ocr.OcrResult
import com.voxable.feature_reader.data.ocr.TextCorrectionUtil
import com.voxable.feature_reader.domain.repository.OcrEngineRepository
import javax.inject.Inject

class OcrCameraUseCase @Inject constructor(
    private val ocrEngineRepository: OcrEngineRepository
) {
    suspend operator fun invoke(cameraBitmap: Bitmap, language: String = "tr"): OcrResult {
        val text = ocrEngineRepository.recognizeHybrid(cameraBitmap, language)
        return if (text.isBlank()) {
            OcrResult.Error("Kamera görüntüsünden metin tanınamadı")
        } else {
            OcrResult.Success(TextCorrectionUtil.correct(text, language))
        }
    }
}
