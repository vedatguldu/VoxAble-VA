package com.voxable.feature_ocr.presentation

import android.net.Uri
import com.voxable.core.base.BaseViewModel
import com.voxable.core.util.onError
import com.voxable.core.util.onSuccess
import com.voxable.feature_ocr.domain.usecase.AnalyzeImageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OcrViewModel @Inject constructor(
    private val analyzeImageUseCase: AnalyzeImageUseCase
) : BaseViewModel<OcrState, OcrEvent>(OcrState()) {

    fun onCameraPermissionChanged(granted: Boolean) {
        updateState { copy(hasCameraPermission = granted, isCameraActive = if (granted) isCameraActive else false) }
    }

    fun onLanguageSelected(language: String) {
        updateState { copy(selectedLanguage = language) }
    }

    fun onImageCaptured(uri: Uri) {
        updateState { copy(capturedImageUri = uri.toString(), isCameraActive = false) }
        recognizeText(uri)
    }

    fun onImageSelected(uri: Uri) {
        updateState { copy(capturedImageUri = uri.toString(), isCameraActive = false) }
        recognizeText(uri)
    }

    fun onToggleCamera() {
        updateState {
            if (!hasCameraPermission) copy(error = "Kamera izni gerekli")
            else copy(isCameraActive = !isCameraActive, error = null)
        }
    }

    fun onClearText() {
        updateState {
            copy(
                recognizedText = "",
                analysisSummary = "",
                detectedColors = emptyList(),
                detectedBarcodes = emptyList(),
                capturedImageUri = null,
                error = null
            )
        }
    }

    private fun recognizeText(uri: Uri) {
        launch {
            updateState {
                copy(
                    isProcessing = true,
                    error = null,
                    recognizedText = "",
                    analysisSummary = "",
                    detectedColors = emptyList(),
                    detectedBarcodes = emptyList()
                )
            }
            analyzeImageUseCase(uri, currentState.selectedLanguage)
                .onSuccess { analysis ->
                    updateState {
                        copy(
                            recognizedText = analysis.recognizedText,
                            analysisSummary = analysis.summary,
                            detectedColors = analysis.detectedColors,
                            detectedBarcodes = analysis.detectedBarcodes,
                            isProcessing = false
                        )
                    }
                    if (analysis.recognizedText.isNotBlank()) {
                        sendEvent(OcrEvent.TextRecognized(analysis.recognizedText))
                    }
                }
                .onError { message ->
                    updateState {
                        copy(
                            isProcessing = false,
                            error = message
                        )
                    }
                    sendEvent(OcrEvent.ShowError(message))
                }
        }
    }
}
