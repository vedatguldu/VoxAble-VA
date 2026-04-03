package com.voxable.feature_ocr.presentation

import com.voxable.core.base.UiEvent
import com.voxable.core.base.UiState
import com.voxable.feature_ocr.domain.model.DetectedBarcode
import com.voxable.feature_ocr.domain.model.DetectedColor

data class OcrState(
    val recognizedText: String = "",
    val analysisSummary: String = "",
    val detectedColors: List<DetectedColor> = emptyList(),
    val detectedBarcodes: List<DetectedBarcode> = emptyList(),
    val liveScanEnabled: Boolean = false,
    val isProcessing: Boolean = false,
    val isCameraActive: Boolean = false,
    val hasCameraPermission: Boolean = false,
    val capturedImageUri: String? = null,
    val selectedLanguage: String = "tr",
    val error: String? = null
) : UiState

sealed interface OcrEvent : UiEvent {
    data class ShowError(val message: String) : OcrEvent
    data class TextRecognized(val text: String) : OcrEvent
}
