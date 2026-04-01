package com.voxable.feature_downloader.domain.repository

import com.voxable.core.util.Resource
import com.voxable.feature_downloader.domain.model.MediaInfo
import kotlinx.coroutines.flow.Flow

interface DownloadRepository {
    suspend fun detectMedia(url: String): Resource<MediaInfo>
    suspend fun startDownload(url: String, fileName: String): Resource<Long>
    suspend fun cancelDownload(downloadId: Long): Resource<Unit>
    fun observeProgress(downloadId: Long): Flow<Float>
    suspend fun getActiveDownloads(): Resource<List<Long>>
}
