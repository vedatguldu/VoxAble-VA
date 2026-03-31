package com.voxable.feature_ocr.presentation

import android.net.Uri
import com.voxable.core.base.BaseViewModel
import com.voxable.core.util.onError
import com.voxable.core.util.onSuccess
import com.voxable.feature_ocr.domain.usecase.RecognizeTextUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OcrViewModel @Inject constructor(
    private val recognizeTextUseCase: RecognizeTextUseCase
) : BaseViewModel<OcrState, OcrEvent>(OcrState()) {

    fun onImageCaptured(uri: Uri) {
        updateState { copy(capturedImageUri = uri.toString(), isCameraActive = false) }
        recognizeText(uri)
    }

    fun onImageSelected(uri: Uri) {
        updateState { copy(capturedImageUri = uri.toString()) }
        recognizeText(uri)
    }

    fun onToggleCamera() {
        updateState { copy(isCameraActive = !isCameraActive) }
    }

    fun onClearText() {
        updateState { copy(recognizedText = "", capturedImageUri = null) }
    }

    private fun recognizeText(uri: Uri) {
        launch {
            updateState { copy(isProcessing = true, error = null) }
            recognizeTextUseCase(uri)
                .onSuccess { text ->
                    updateState {
                        copy(
                            recognizedText = text,
                            isProcessing = false
                        )
                    }
                    sendEvent(OcrEvent.TextRecognized(text))
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
