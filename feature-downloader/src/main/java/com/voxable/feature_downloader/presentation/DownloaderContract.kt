package com.voxable.feature_downloader.presentation

import com.voxable.core.base.UiEvent
import com.voxable.core.base.UiState
import com.voxable.feature_downloader.domain.model.MediaFormat
import com.voxable.feature_downloader.domain.model.MediaInfo

data class DownloaderState(
    val url: String = "",
    val isDetecting: Boolean = false,
    val mediaInfo: MediaInfo? = null,
    val selectedFormat: MediaFormat? = null,
    val downloads: List<DownloadItem> = emptyList(),
    val isDownloading: Boolean = false,
    val error: String? = null
) : UiState

data class DownloadItem(
    val id: Long,
    val fileName: String,
    val url: String,
    val progress: Float = 0f,
    val totalBytes: Long = 0L,
    val downloadedBytes: Long = 0L,
    val status: DownloadStatus = DownloadStatus.PENDING,
    val formatLabel: String = ""
)

enum class DownloadStatus {
    PENDING, DOWNLOADING, COMPLETED, FAILED, CANCELLED
}

sealed interface DownloaderEvent : UiEvent {
    data class ShowError(val message: String) : DownloaderEvent
    data class DownloadComplete(val fileName: String) : DownloaderEvent
}
