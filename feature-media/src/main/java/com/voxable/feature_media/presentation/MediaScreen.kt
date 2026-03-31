package com.voxable.feature_media.presentation

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.voxable.core_ui.components.LoadingIndicator
import com.voxable.core_ui.components.VoxAbleTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaScreen(
    onBack: () -> Unit,
    viewModel: MediaViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.onMediaSelected(it.toString())
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is MediaEvent.ShowError -> { /* Snackbar ile gösterilebilir */ }
                is MediaEvent.MediaCompleted -> { /* Tamamlandı */ }
            }
        }
    }

    Scaffold(
        topBar = {
            VoxAbleTopBar(
                title = "Medya Oynatıcı",
                onBack = onBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (state.isLoading) {
                LoadingIndicator()
                return@Column
            }

            // Dosya seç butonu
            OutlinedButton(
                onClick = { filePicker.launch(arrayOf("audio/*", "video/*")) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .semantics { contentDescription = "Medya dosyası seçmek için dokunun" }
            ) {
                Icon(
                    imageVector = Icons.Default.FolderOpen,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Dosya Seç")
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Medya bilgisi
            if (state.mediaUri != null) {
                Text(
                    text = state.currentTitle,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.semantics { heading() }
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = state.currentArtist,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                // İlerleme çubuğu
                val progress = if (state.durationMs > 0) {
                    state.currentPositionMs.toFloat() / state.durationMs.toFloat()
                } else 0f

                Column(modifier = Modifier.fillMaxWidth()) {
                    Slider(
                        value = progress,
                        onValueChange = { fraction ->
                            viewModel.onSeekTo((fraction * state.durationMs).toLong())
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics {
                                contentDescription = "Oynatma konumu: yüzde ${(progress * 100).toInt()}"
                            }
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = formatDuration(state.currentPositionMs),
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = formatDuration(state.durationMs),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Oynatma kontrolleri
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Durdur
                    IconButton(
                        onClick = { viewModel.onStop() },
                        modifier = Modifier
                            .size(56.dp)
                            .semantics { contentDescription = "Durdur" }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // Oynat / Duraklat
                    FloatingActionButton(
                        onClick = { viewModel.onPlayPause() },
                        modifier = Modifier.semantics {
                            contentDescription = if (state.isPlaying) "Duraklat" else "Oynat"
                        }
                    ) {
                        Icon(
                            imageVector = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Ses seviyesi
                Text(
                    text = "Ses Seviyesi",
                    style = MaterialTheme.typography.bodyMedium
                )
                Slider(
                    value = state.volume,
                    onValueChange = { viewModel.onVolumeChange(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics {
                            contentDescription = "Ses seviyesi: yüzde ${(state.volume * 100).toInt()}"
                        }
                )
            } else {
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "Bir medya dosyası seçin",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.semantics {
                        contentDescription = "Henüz bir medya dosyası seçilmedi. Yukarıdaki Dosya Seç butonuna dokunun."
                    }
                )
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

private fun formatDuration(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
