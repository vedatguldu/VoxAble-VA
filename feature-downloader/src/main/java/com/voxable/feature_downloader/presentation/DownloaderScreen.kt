package com.voxable.feature_downloader.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
            // Durum ikonu
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
                Text(
                    text = item.fileName,
                    style = MaterialTheme.typography.bodyMedium
                )

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

            // İptal butonu
            if (item.status == DownloadStatus.DOWNLOADING || item.status == DownloadStatus.PENDING) {
                IconButton(
                    onClick = onCancel,
                    modifier = Modifier
                        .size(48.dp)
                        .semantics { contentDescription = "${item.fileName} indirmesini iptal et" }
                ) {
                    Icon(
                        imageVector = Icons.Default.Cancel,
                        contentDescription = null
                    )
                }
            }
        }
    }
}
