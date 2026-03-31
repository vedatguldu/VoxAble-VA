package com.voxable.feature_media.domain.usecase

import android.net.Uri
import com.voxable.feature_media.domain.model.Playlist
import com.voxable.feature_media.domain.repository.MediaPlayerRepository

class LoadPlaylistUseCase(private val repository: MediaPlayerRepository) {
    suspend operator fun invoke(uri: Uri): Playlist {
        return repository.loadPlaylist(uri)
    }
}