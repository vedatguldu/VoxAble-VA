package com.voxable.feature_downloader.presentation

import com.voxable.core.base.UiEvent
import com.voxable.core.base.UiState

data class DownloaderState(
    val url: String = "",
    val downloads: List<DownloadItem> = emptyList(),
    val isDownloading: Boolean = false,
    val error: String? = null
) : UiState

data class DownloadItem(
    val id: Long,
    val fileName: String,
    val url: String,
    val progress: Float = 0f, // 0.0 - 1.0
    val totalBytes: Long = 0L,
    val downloadedBytes: Long = 0L,
    val status: DownloadStatus = DownloadStatus.PENDING
)

enum class DownloadStatus {
    PENDING, DOWNLOADING, COMPLETED, FAILED, CANCELLED
}

sealed interface DownloaderEvent : UiEvent {
    data class ShowError(val message: String) : DownloaderEvent
    data class DownloadComplete(val fileName: String) : DownloaderEvent
}
