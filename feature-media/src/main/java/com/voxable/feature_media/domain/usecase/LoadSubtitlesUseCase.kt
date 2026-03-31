package com.voxable.feature_media.domain.usecase

import android.net.Uri
import com.voxable.feature_media.domain.model.SubtitleTrack
import com.voxable.feature_media.domain.repository.MediaPlayerRepository

class LoadSubtitlesUseCase(private val repository: MediaPlayerRepository) {
    suspend operator fun invoke(mediaUri: Uri): List<SubtitleTrack> {
        return repository.loadSubtitles(mediaUri)
    }
}