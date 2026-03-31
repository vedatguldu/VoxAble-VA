package com.voxable.feature_ocr.presentation

import com.voxable.core.base.UiEvent
import com.voxable.core.base.UiState

data class OcrState(
    val recognizedText: String = "",
    val isProcessing: Boolean = false,
    val isCameraActive: Boolean = false,
    val capturedImageUri: String? = null,
    val error: String? = null
) : UiState

sealed interface OcrEvent : UiEvent {
    data class ShowError(val message: String) : OcrEvent
    data class TextRecognized(val text: String) : OcrEvent
}
