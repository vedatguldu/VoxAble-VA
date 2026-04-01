package com.voxable.feature_downloader.presentation

import android.webkit.URLUtil
import com.voxable.core.base.BaseViewModel
import com.voxable.core.util.onError
import com.voxable.core.util.onSuccess
import com.voxable.feature_downloader.domain.model.MediaFormat
import com.voxable.feature_downloader.domain.repository.DownloadRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DownloaderViewModel @Inject constructor(
    private val downloadRepository: DownloadRepository
) : BaseViewModel<DownloaderState, DownloaderEvent>(DownloaderState()) {

    fun onUrlChanged(url: String) {
        updateState { copy(url = url, error = null) }
    }

    /**
     * URL'den medya algılama: tip ve formatları tespit eder.
     */
    fun onDetectMedia() {
        val url = currentState.url.trim()
        if (url.isEmpty()) {
            sendEvent(DownloaderEvent.ShowError("Lütfen bir URL girin"))
            return
        }
        if (!URLUtil.isValidUrl(url) && !url.startsWith("http")) {
            sendEvent(DownloaderEvent.ShowError("Geçersiz URL"))
            return
        }

        launch {
            updateState { copy(isDetecting = true, mediaInfo = null, selectedFormat = null, error = null) }

            downloadRepository.detectMedia(url)
                .onSuccess { mediaInfo ->
                    val defaultFormat = mediaInfo.formats.firstOrNull()
                    updateState {
                        copy(
                            isDetecting = false,
                            mediaInfo = mediaInfo,
                            selectedFormat = defaultFormat
                        )
                    }
                }
                .onError { message ->
                    updateState { copy(isDetecting = false, error = message) }
                    sendEvent(DownloaderEvent.ShowError(message))
                }
        }
    }

    fun onFormatSelected(format: MediaFormat) {
        updateState { copy(selectedFormat = format) }
    }

    /**
     * Seçili formattaki medyayı indir.
     */
    fun onStartDownload() {
        val format = currentState.selectedFormat ?: run {
            sendEvent(DownloaderEvent.ShowError("Lütfen bir format seçin"))
            return
        }
        val mediaInfo = currentState.mediaInfo ?: return

        val ext = format.extension
        val baseName = mediaInfo.title.substringBeforeLast(".")
        val fileName = "${baseName}_${format.quality}.$ext"

        launch {
            updateState { copy(isDownloading = true, error = null) }

            val newItem = DownloadItem(
                id = System.currentTimeMillis(),
                fileName = fileName,
                url = format.url,
                status = DownloadStatus.DOWNLOADING,
                formatLabel = format.displayLabel
            )
            updateState { copy(downloads = downloads + newItem) }

            downloadRepository.startDownload(format.url, fileName)
                .onSuccess { downloadId ->
                    updateState {
                        copy(
                            isDownloading = false,
                            downloads = downloads.map {
                                if (it.url == format.url && it.status == DownloadStatus.DOWNLOADING) {
                                    it.copy(id = downloadId, status = DownloadStatus.COMPLETED, progress = 1f)
                                } else it
                            }
                        )
                    }
                    sendEvent(DownloaderEvent.DownloadComplete(fileName))
                }
                .onError { message ->
                    updateState {
                        copy(
                            isDownloading = false,
                            error = message,
                            downloads = downloads.map {
                                if (it.url == format.url && it.status == DownloadStatus.DOWNLOADING) {
                                    it.copy(status = DownloadStatus.FAILED)
                                } else it
                            }
                        )
                    }
                    sendEvent(DownloaderEvent.ShowError(message))
                }
        }
    }

    fun onCancelDownload(downloadId: Long) {
        launch {
            downloadRepository.cancelDownload(downloadId)
            updateState {
                copy(
                    downloads = downloads.map {
                        if (it.id == downloadId) it.copy(status = DownloadStatus.CANCELLED) else it
                    }
                )
            }
        }
    }

    fun onClearDetection() {
        updateState { copy(mediaInfo = null, selectedFormat = null, error = null) }
    }
}
