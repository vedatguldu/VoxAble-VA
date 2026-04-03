package com.voxable.feature_currency.presentation.recognition

import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.voxable.core.base.BaseViewModel
import com.voxable.core.util.Resource
import com.voxable.feature_currency.domain.model.CurrencyRecognitionResult
import com.voxable.feature_currency.domain.usecase.RecognizeCurrencyUseCase
import com.voxable.feature_currency.domain.usecase.SpeakCurrencyResultUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CurrencyRecognitionViewModel @Inject constructor(
    private val recognizeCurrencyUseCase: RecognizeCurrencyUseCase,
    private val speakCurrencyResultUseCase: SpeakCurrencyResultUseCase
) : BaseViewModel<CurrencyRecognitionState, CurrencyRecognitionEvent>(CurrencyRecognitionState()) {

    fun onCameraPermissionChanged(granted: Boolean) {
        updateState { copy(hasCameraPermission = granted) }
        if (granted && !currentState.isCameraActive) {
            updateState { copy(isCameraActive = true) }
        }
    }

    fun onToggleCamera() {
        if (!currentState.hasCameraPermission) return
        updateState {
            val nextActive = !isCameraActive
            copy(
                isCameraActive = nextActive,
                liveScanEnabled = if (nextActive) liveScanEnabled else false
            )
        }
    }

    fun onToggleLiveScan() {
        updateState {
            if (!isCameraActive) copy(liveScanEnabled = false)
            else copy(liveScanEnabled = !liveScanEnabled)
        }
    }

    fun onImageCaptured(uri: Uri) {
        updateState { copy(capturedImageUri = uri.toString(), isCameraActive = !liveScanEnabled) }
        recognizeFromImage(uri)
    }

    fun onImageSelected(uri: Uri) {
        updateState { copy(capturedImageUri = uri.toString(), isCameraActive = false) }
        recognizeFromImage(uri)
    }

    fun onSpeakResult() {
        val result = currentState.recognitionResult ?: return
        if (currentState.isSpeaking) {
            speakCurrencyResultUseCase.stop()
            updateState { copy(isSpeaking = false) }
        } else {
            speakCurrencyResultUseCase(result)
            updateState { copy(isSpeaking = true) }
        }
    }

    fun onToggleAutoSpeak() {
        updateState { copy(autoSpeak = !autoSpeak) }
    }

    fun onRetry() {
        updateState {
            copy(
                capturedImageUri = null,
                recognitionResult = null,
                error = null,
                isCameraActive = hasCameraPermission
            )
        }
    }

    fun onClearHistory() {
        updateState { copy(recentResults = emptyList()) }
    }

    private fun recognizeFromImage(uri: Uri) {
        viewModelScope.launch {
            updateState { copy(isLoading = true, error = null, recognitionResult = null) }

            when (val result = recognizeCurrencyUseCase.fromImage(uri)) {
                is Resource.Success -> {
                    val recognition = result.data
                    val updatedHistory = (listOf(recognition) + currentState.recentResults).take(10)
                    updateState {
                        copy(
                            isLoading = false,
                            recognitionResult = recognition,
                            recentResults = updatedHistory
                        )
                    }
                    if (currentState.autoSpeak && recognition.isSuccessful) {
                        speakCurrencyResultUseCase(recognition)
                        updateState { copy(isSpeaking = true) }
                    }
                }
                is Resource.Error -> {
                    updateState { copy(isLoading = false, error = result.message) }
                    sendEvent(CurrencyRecognitionEvent.ShowError(result.message))
                }
                is Resource.Loading -> {
                    updateState { copy(isLoading = true) }
                }
            }
        }
    }

    override fun onCleared() {
        speakCurrencyResultUseCase.shutdown()
        super.onCleared()
    }
}
