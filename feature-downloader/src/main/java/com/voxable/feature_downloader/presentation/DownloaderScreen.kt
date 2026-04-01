package com.voxable.feature_downloader.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.voxable.core_ui.components.VoxAbleButton
import com.voxable.core_ui.components.VoxAbleTextField
import com.voxable.core_ui.components.VoxAbleTopBar
import com.voxable.feature_downloader.domain.model.MediaFormat
import com.voxable.feature_downloader.domain.model.MediaInfo
import com.voxable.feature_downloader.domain.model.MediaType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloaderScreen(
    onBack: () -> Unit,
    viewModel: DownloaderViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            VoxAbleTopBar(
                title = "Medya İndirici",
                onBack = onBack
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // URL girişi
            item {
                VoxAbleTextField(
                    value = state.url,
                    onValueChange = { viewModel.onUrlChanged(it) },
                    label = "Medya URL'si",
                    placeholder = "https://... veya .m3u8 bağlantısı",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Algıla butonu
            item {
                Button(
                    onClick = { viewModel.onDetectMedia() },
                    enabled = !state.isDetecting && state.url.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = "Medya algıla. URL'deki medyayı tespit et" }
                ) {
                    if (state.isDetecting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Algılanıyor...")
                    } else {
                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Medya Algıla")
                    }
                }
            }

            // Medya bilgi kartı
            state.mediaInfo?.let { mediaInfo ->
                item {
                    MediaInfoCard(mediaInfo = mediaInfo)
                }

                // Format listesi
                item {
                    Text(
                        text = "Mevcut Formatlar",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.semantics { heading() }
                    )
                }

                items(
                    items = mediaInfo.formats,
                    key = { it.formatId }
                ) { format ->
                    FormatCard(
                        format = format,
                        isSelected = state.selectedFormat?.formatId == format.formatId,
                        onClick = { viewModel.onFormatSelected(format) }
                    )
                }

                // İndir butonu
                item {
                    VoxAbleButton(
                        text = if (state.isDownloading) "İndiriliyor..." else "Seçili Formatı İndir",
                        onClick = { viewModel.onStartDownload() },
                        enabled = !state.isDownloading && state.selectedFormat != null,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // İndirme listesi
            if (state.downloads.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "İndirmeler",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.semantics { heading() }
                    )
                }

                items(
                    items = state.downloads,
                    key = { it.id }
                ) { download ->
                    DownloadItemCard(
                        item = download,
                        onCancel = { viewModel.onCancelDownload(download.id) }
                    )
                }
            }

            // Boş durum
            if (state.mediaInfo == null && state.downloads.isEmpty() && !state.isDetecting) {
                item {
                    Spacer(modifier = Modifier.height(48.dp))
                    Text(
                        text = "Medya indirmek için yukarıya URL girin ve Algıla'ya basın.\nm3u8, mp4, mp3 ve diğer formatlar desteklenir.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics {
                                contentDescription =
                                    "Medya indirmek için URL girin. m3u8, mp4, mp3 ve diğer formatlar desteklenir."
                            }
                    )
                }
            }

            // Hata
            state.error?.let { error ->
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        modifier = Modifier.semantics { contentDescription = "Hata: $error" }
                    ) {
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MediaInfoCard(mediaInfo: MediaInfo) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "Algılanan medya: ${mediaInfo.title}, " +
                    "Tip: ${
                        when (mediaInfo.type) {
                            MediaType.VIDEO -> "Video"
                            MediaType.AUDIO -> "Ses"
                            MediaType.HLS_STREAM -> "HLS Yayın"
                            MediaType.DIRECT_FILE -> "Dosya"
                            MediaType.UNKNOWN -> "Bilinmeyen"
                        }
                    }, ${mediaInfo.formats.size} format mevcut"
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (mediaInfo.type) {
                    MediaType.VIDEO -> Icons.Default.VideoFile
                    MediaType.AUDIO -> Icons.Default.AudioFile
                    MediaType.HLS_STREAM -> Icons.Default.LiveTv
                    else -> Icons.Default.Download
                },
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = mediaInfo.title,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = when (mediaInfo.type) {
                        MediaType.VIDEO -> "Video"
                        MediaType.AUDIO -> "Ses Dosyası"
                        MediaType.HLS_STREAM -> "HLS Yayın (m3u8)"
                        MediaType.DIRECT_FILE -> "Dosya"
                        MediaType.UNKNOWN -> "Bilinmeyen"
                    } + " • ${mediaInfo.formats.size} format",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun FormatCard(
    format: MediaFormat,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    OutlinedCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "${format.displayLabel}" +
                    if (format.isAudioOnly) ", sadece ses" else "" +
                        if (isSelected) ", seçili" else ""
            },
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.outline
        ),
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (format.isAudioOnly) Icons.Default.AudioFile else Icons.Default.VideoFile,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = format.quality,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    format.resolution?.let {
                        Text(text = it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    format.codec?.let {
                        Text(text = it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(
                        text = ".${format.extension}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Seçili",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun DownloadItemCard(
    item: DownloadItem,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "${item.fileName}, durum: ${
                    when (item.status) {
                        DownloadStatus.PENDING -> "bekliyor"
                        DownloadStatus.DOWNLOADING -> "indiriliyor, yüzde ${(item.progress * 100).toInt()}"
                        DownloadStatus.COMPLETED -> "tamamlandı"
                        DownloadStatus.FAILED -> "başarısız"
                        DownloadStatus.CANCELLED -> "iptal edildi"
                    }
                }"
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (item.status) {
                    DownloadStatus.COMPLETED -> Icons.Default.CheckCircle
                    DownloadStatus.FAILED -> Icons.Default.Error
                    DownloadStatus.CANCELLED -> Icons.Default.Cancel
                    else -> Icons.Default.Download
                },
                contentDescription = null,
                tint = when (item.status) {
                    DownloadStatus.COMPLETED -> MaterialTheme.colorScheme.primary
                    DownloadStatus.FAILED -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.fileName, style = MaterialTheme.typography.bodyMedium)
                if (item.formatLabel.isNotBlank()) {
                    Text(
                        text = item.formatLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (item.status == DownloadStatus.DOWNLOADING) {
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { item.progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { liveRegion() }
                    )
                }
            }

            if (item.status == DownloadStatus.DOWNLOADING || item.status == DownloadStatus.PENDING) {
                IconButton(
                    onClick = onCancel,
                    modifier = Modifier
                        .size(48.dp)
                        .semantics { contentDescription = "${item.fileName} indirmesini iptal et" }
                ) {
                    Icon(Icons.Default.Cancel, contentDescription = null)
                }
            }
        }
    }
}
    onBack: () -> Unit,
    viewModel: DownloaderViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            VoxAbleTopBar(
                title = "Dosya İndirici",
                onBack = onBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // URL girişi
            VoxAbleTextField(
                value = state.url,
                onValueChange = { viewModel.onUrlChanged(it) },
                label = "İndirme URL'si",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // İndir butonu
            VoxAbleButton(
                text = if (state.isDownloading) "İndiriliyor..." else "İndir",
                onClick = { viewModel.onStartDownload() },
                enabled = !state.isDownloading && state.url.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // İndirme listesi
            if (state.downloads.isNotEmpty()) {
                Text(
                    text = "İndirmeler",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.semantics { heading() }
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = state.downloads,
                        key = { it.id }
                    ) { download ->
                        DownloadItemCard(
                            item = download,
                            onCancel = { viewModel.onCancelDownload(download.id) }
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "Henüz indirme yok",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .semantics {
                            contentDescription = "İndirme listesi boş. Yukarıdaki alana URL girerek dosya indirebilirsiniz."
                        }
                )
                Spacer(modifier = Modifier.weight(1f))
            }

            // Hata
            state.error?.let { error ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.semantics { contentDescription = "Hata: $error" }
                ) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}
