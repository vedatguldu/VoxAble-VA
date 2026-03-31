package com.voxable.feature_media.presentation

import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.voxable.core.base.BaseViewModel
import com.voxable.core.util.onError
import com.voxable.core.util.onSuccess
import com.voxable.feature_media.domain.repository.MediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MediaViewModel @Inject constructor(
    private val mediaRepository: MediaRepository
) : BaseViewModel<MediaState, MediaEvent>(MediaState()) {

    fun onMediaSelected(uri: String, title: String = "", artist: String = "") {
        updateState {
            copy(
                mediaUri = uri,
                currentTitle = title.ifEmpty { "Bilinmeyen Parça" },
                currentArtist = artist.ifEmpty { "Bilinmeyen Sanatçı" }
            )
        }
        playMedia(uri)
    }

    fun onPlayPause() {
        if (currentState.isPlaying) {
            pauseMedia()
        } else {
            currentState.mediaUri?.let { playMedia(it) }
        }
    }

    fun onStop() {
        launch {
            mediaRepository.stop()
            updateState {
                copy(
                    isPlaying = false,
                    currentPositionMs = 0L
                )
            }
        }
    }

    fun onSeekTo(positionMs: Long) {
        launch {
            mediaRepository.seekTo(positionMs)
            updateState { copy(currentPositionMs = positionMs) }
        }
    }

    fun onVolumeChange(volume: Float) {
        launch {
            mediaRepository.setVolume(volume)
            updateState { copy(volume = volume) }
        }
    }

    private fun playMedia(uri: String) {
        launch {
            updateState { copy(isLoading = true) }
            mediaRepository.play(uri)
                .onSuccess {
                    updateState {
                        copy(
                            isPlaying = true,
                            isLoading = false,
                            error = null,
                            durationMs = mediaRepository.getDurationMs()
                        )
                    }
                    startPositionTracking()
                }
                .onError { message ->
                    updateState { copy(isLoading = false, error = message) }
                    sendEvent(MediaEvent.ShowError(message))
                }
        }
    }

    private fun pauseMedia() {
        launch {
            mediaRepository.pause()
            updateState { copy(isPlaying = false) }
        }
    }

    private fun startPositionTracking() {
        viewModelScope.launch {
            while (isActive && currentState.isPlaying) {
                updateState {
                    copy(
                        currentPositionMs = mediaRepository.getCurrentPositionMs(),
                        durationMs = mediaRepository.getDurationMs()
                    )
                }
                delay(500L)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        mediaRepository.release()
    }
}
