package com.voxable.feature_media.di

import android.content.Context
import com.voxable.feature_media.data.loader.SubtitleLoader
import com.voxable.feature_media.data.manager.ExoPlayerManager
import com.voxable.feature_media.data.parser.M3UPlaylistParser
import com.voxable.feature_media.data.repository.MediaPlayerRepositoryImpl
import com.voxable.feature_media.domain.repository.MediaPlayerRepository
import com.voxable.feature_media.domain.usecase.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MediaModule {

    @Provides
    @Singleton
    fun provideExoPlayerManager(@ApplicationContext context: Context): ExoPlayerManager {
        return ExoPlayerManager(context)
    }

    @Provides
    @Singleton
    fun provideM3UPlaylistParser(): M3UPlaylistParser {
        return M3UPlaylistParser()
    }

    @Provides
    @Singleton
    fun provideSubtitleLoader(@ApplicationContext context: Context): SubtitleLoader {
        return SubtitleLoader(context)
    }

    @Provides
    @Singleton
    fun provideMediaPlayerRepository(
        @ApplicationContext context: Context,
        exoPlayerManager: ExoPlayerManager,
        playlistParser: M3UPlaylistParser,
        subtitleLoader: SubtitleLoader
    ): MediaPlayerRepository {
        return MediaPlayerRepositoryImpl(context, exoPlayerManager, playlistParser, subtitleLoader)
    }

    @Provides
    @Singleton
    fun providePlayMediaUseCase(repository: MediaPlayerRepository): PlayMediaUseCase {
        return PlayMediaUseCase(repository)
    }

    @Provides
    @Singleton
    fun providePauseMediaUseCase(repository: MediaPlayerRepository): PauseMediaUseCase {
        return PauseMediaUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideResumeMediaUseCase(repository: MediaPlayerRepository): ResumeMediaUseCase {
        return ResumeMediaUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideLoadPlaylistUseCase(repository: MediaPlayerRepository): LoadPlaylistUseCase {
        return LoadPlaylistUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideLoadSubtitlesUseCase(repository: MediaPlayerRepository): LoadSubtitlesUseCase {
        return LoadSubtitlesUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideSeekToUseCase(repository: MediaPlayerRepository): SeekToUseCase {
        return SeekToUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideSetPlaybackSpeedUseCase(repository: MediaPlayerRepository): SetPlaybackSpeedUseCase {
        return SetPlaybackSpeedUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideSetVolumeUseCase(repository: MediaPlayerRepository): SetVolumeUseCase {
        return SetVolumeUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideSelectSubtitleTrackUseCase(repository: MediaPlayerRepository): SelectSubtitleTrackUseCase {
        return SelectSubtitleTrackUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetCurrentPositionUseCase(repository: MediaPlayerRepository): GetCurrentPositionUseCase {
        return GetCurrentPositionUseCase(repository)
    }
}
