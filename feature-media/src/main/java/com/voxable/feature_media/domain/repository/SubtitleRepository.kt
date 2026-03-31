package com.voxable.feature_media.domain.repository

import com.voxable.feature_media.domain.model.Subtitle
import com.voxable.feature_media.domain.model.SubtitleFormat
import kotlinx.coroutines.flow.Flow

interface SubtitleRepository {
    suspend fun parseSubtitles(content: String, format: SubtitleFormat): List<Subtitle>
    suspend fun getSubtitleAtTime(subtitles: List<Subtitle>, timeMs: Long): Subtitle?
    fun observeCurrentSubtitle(subtitles: List<Subtitle>, currentTimeMs: Flow<Long>): Flow<Subtitle?>
    suspend fun detectSubtitleFormat(content: String): SubtitleFormat
}