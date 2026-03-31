package com.voxable.feature_media.presentation

import com.voxable.core.base.UiEvent
import com.voxable.core.base.UiState

data class MediaState(
    val isPlaying: Boolean = false,
    val currentTitle: String = "",
    val currentArtist: String = "",
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val volume: Float = 1f,
    val mediaUri: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
) : UiState

sealed interface MediaEvent : UiEvent {
    data class ShowError(val message: String) : MediaEvent
    data object MediaCompleted : MediaEvent
}
