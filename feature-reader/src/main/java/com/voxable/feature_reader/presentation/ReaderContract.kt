package com.voxable.feature_reader.presentation

import com.voxable.core.base.UiEvent
import com.voxable.core.base.UiState

data class ReaderState(
    val inputText: String = "",
    val isSpeaking: Boolean = false,
    val isLoading: Boolean = false
) : UiState

sealed class ReaderEvent : UiEvent {
    data class ShowError(val message: String) : ReaderEvent()
}
