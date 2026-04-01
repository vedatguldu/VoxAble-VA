package com.voxable.feature_currency.presentation.recognition

import com.voxable.core.base.UiEvent
import com.voxable.core.base.UiState
import com.voxable.feature_currency.domain.model.CurrencyRecognitionResult

data class CurrencyRecognitionState(
    val isLoading: Boolean = false,
    val hasCameraPermission: Boolean = false,
    val isCameraActive: Boolean = false,
    val capturedImageUri: String? = null,
    val recognitionResult: CurrencyRecognitionResult? = null,
    val isSpeaking: Boolean = false,
    val autoSpeak: Boolean = true,
    val recentResults: List<CurrencyRecognitionResult> = emptyList(),
    val error: String? = null
) : UiState

sealed interface CurrencyRecognitionEvent : UiEvent {
    data class ShowError(val message: String) : CurrencyRecognitionEvent
    data class Speak(val text: String) : CurrencyRecognitionEvent
    data object NavigateBack : CurrencyRecognitionEvent
}
