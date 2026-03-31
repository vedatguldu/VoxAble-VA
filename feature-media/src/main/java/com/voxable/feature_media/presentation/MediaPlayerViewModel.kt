package com.voxable.feature_media.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voxable.feature_media.domain.repository.MediaPlayerRepository
import com.voxable.feature_media.domain.repository.PlaybackState
import com.voxable.feature_media.domain.usecase.*
import com.voxable.feature_media.domain.model.MediaItem as DomainMediaItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import android.net.Uri

class MediaPlayerViewModel(
    private val repository: MediaPlayerRepository,
    private val playMediaUseCase: PlayMediaUseCase,
    private val pauseMediaUseCase: PauseMediaUseCase,
    private val resumeMediaUseCase: ResumeMediaUseCase,
    private val loadPlaylistUseCase: LoadPlaylistUseCase,
    private val loadSubtitlesUseCase: LoadSubtitlesUseCase,
    private val seekToUseCase: SeekToUseCase,
    private val setPlaybackSpeedUseCase: SetPlaybackSpeedUseCase,
    private val setVolumeUseCase: SetVolumeUseCase,
    private val selectSubtitleTrackUseCase: SelectSubtitleTrackUseCase,
    private val getCurrentPositionUseCase: GetCurrentPositionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MediaPlayerState())
    val uiState: StateFlow<MediaPlayerState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<MediaPlayerEvent>()
    val events: Flow<MediaPlayerEvent> = _events.asSharedFlow()

    init {
        observePlaybackState()
    }

    private fun observePlaybackState() {
        viewModelScope.launch {
            repository.isPlaying().collect { isPlaying ->
                _uiState.update { it.copy(isPlaying = isPlaying) }
            }
        }
        viewModelScope.launch {
            repository.getPlaybackState().collect { state ->
                _uiState.update { it.copy(playbackState = state) }
                if (state == PlaybackState.ENDED) {
                    _events.emit(MediaPlayerEvent.PlaybackEnded)
                }
            }
        }
        viewModelScope.launch {
            repository.getCurrentPosition().collect { position ->
                _uiState.update { it.copy(currentPosition = position) }
            }
        }
        viewModelScope.launch {
            repository.getDuration().collect { duration ->
                _uiState.update { it.copy(duration = duration) }
            }
        }
        viewModelScope.launch {
            repository.getPlaybackSpeed().collect { speed ->
                _uiState.update { it.copy(playbackSpeed = speed) }
            }
        }
        viewModelScope.launch {
            repository.getSelectedSubtitleTrack().collect { track ->
                _uiState.update { it.copy(selectedSubtitleTrack = track) }
            }
        }
        viewModelScope.launch {
            repository.getAvailableSubtitleTracks().collect { tracks ->
                _uiState.update { it.copy(availableSubtitleTracks = tracks) }
            }
        }
    }

    fun playMedia(mediaItem: DomainMediaItem) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                playMediaUseCase(mediaItem)
                _uiState.update { it.copy(isLoading = false, error = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
                _events.emit(MediaPlayerEvent.ShowError(e.message ?: "Oynatma hatası"))
            }
        }
    }

    fun onPlayPauseClick() {
        viewModelScope.launch {
            try {
                if (_uiState.value.isPlaying) {
                    pauseMediaUseCase()
                } else {
                    resumeMediaUseCase()
                }
            } catch (e: Exception) {
                _events.emit(MediaPlayerEvent.ShowError("Oynatma kontrolü hatası"))
            }
        }
    }

    fun onSeek(positionMs: Long) {
        viewModelScope.launch {
            try {
                seekToUseCase(positionMs)
            } catch (e: Exception) {
                _events.emit(MediaPlayerEvent.ShowError("Arama hatası"))
            }
        }
    }

    fun setPlaybackSpeed(speed: Float) {
        viewModelScope.launch {
            try {
                setPlaybackSpeedUseCase(speed)
            } catch (e: Exception) {
                _events.emit(MediaPlayerEvent.ShowError("Hız ayarı hatası"))
            }
        }
    }

    fun setVolume(volumePercent: Float) {
        viewModelScope.launch {
            try {
                setVolumeUseCase(volumePercent)
            } catch (e: Exception) {
                _events.emit(MediaPlayerEvent.ShowError("Ses ayarı hatası"))
            }
        }
    }

    fun loadPlaylist(uri: Uri) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                val playlist = loadPlaylistUseCase(uri)
                repository.playPlaylist(playlist)
                _uiState.update { it.copy(isLoading = false, error = null) }
                _events.emit(MediaPlayerEvent.PlaylistLoaded)
                _events.emit(MediaPlayerEvent.ShowMessage("${playlist.items.size} şarkı yüklendi"))
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
                _events.emit(MediaPlayerEvent.ShowError("Çalma listesi yükleme hatası"))
            }
        }
    }

    fun loadSubtitles(mediaUri: Uri) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                val tracks = loadSubtitlesUseCase(mediaUri)
                _uiState.update { it.copy(isLoading = false, availableSubtitleTracks = tracks) }
                _events.emit(MediaPlayerEvent.SubtitlesLoaded)
                if (tracks.isNotEmpty()) {
                    _events.emit(MediaPlayerEvent.ShowMessage("${tracks.size} altyazı bulundu"))
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
                _events.emit(MediaPlayerEvent.ShowError("Altyazı yükleme hatası"))
            }
        }
    }

    fun selectSubtitleTrack(trackId: String?) {
        viewModelScope.launch {
            try {
                selectSubtitleTrackUseCase(trackId)
                if (trackId != null) {
                    _events.emit(MediaPlayerEvent.ShowMessage("Altyazı seçildi"))
                }
            } catch (e: Exception) {
                _events.emit(MediaPlayerEvent.ShowError("Altyazı seçim hatası"))
            }
        }
    }

    fun toggleControls() {
        _uiState.update { it.copy(showControls = !it.showControls) }
    }

    fun toggleSubtitleSettings() {
        _uiState.update { it.copy(showSubtitleSettings = !it.showSubtitleSettings) }
    }

    fun togglePlaylistSelector() {
        _uiState.update { it.copy(showPlaylistSelector = !it.showPlaylistSelector) }
    }

    override fun onCleared() {
        super.onCleared()
        // Cleanup if needed
    }
}