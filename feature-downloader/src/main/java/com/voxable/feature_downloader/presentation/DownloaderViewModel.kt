package com.voxable.feature_downloader.presentation

import android.webkit.URLUtil
import com.voxable.core.base.BaseViewModel
import com.voxable.core.util.onError
import com.voxable.core.util.onSuccess
import com.voxable.feature_downloader.domain.repository.DownloadRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DownloaderViewModel @Inject constructor(
    private val downloadRepository: DownloadRepository
) : BaseViewModel<DownloaderState, DownloaderEvent>(DownloaderState()) {

    fun onUrlChanged(url: String) {
        updateState { copy(url = url) }
    }

    fun onStartDownload() {
        val url = currentState.url.trim()
        if (url.isEmpty()) {
            sendEvent(DownloaderEvent.ShowError("Lütfen bir URL girin"))
            return
        }
        if (!URLUtil.isValidUrl(url)) {
            sendEvent(DownloaderEvent.ShowError("Geçersiz URL"))
            return
        }

        val fileName = URLUtil.guessFileName(url, null, null)

        launch {
            updateState { copy(isDownloading = true, error = null) }

            val newItem = DownloadItem(
                id = System.currentTimeMillis(),
                fileName = fileName,
                url = url,
                status = DownloadStatus.DOWNLOADING
            )
            updateState {
                copy(downloads = downloads + newItem)
            }

            downloadRepository.startDownload(url, fileName)
                .onSuccess { downloadId ->
                    updateState {
                        copy(
                            isDownloading = false,
                            url = "",
                            downloads = downloads.map {
                                if (it.url == url) it.copy(
                                    id = downloadId,
                                    status = DownloadStatus.COMPLETED,
                                    progress = 1f
                                ) else it
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
                                if (it.url == url) it.copy(status = DownloadStatus.FAILED) else it
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
}
