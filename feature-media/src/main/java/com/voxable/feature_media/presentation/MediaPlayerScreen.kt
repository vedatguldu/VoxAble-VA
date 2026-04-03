package com.voxable.feature_media.presentation

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.ui.PlayerView
import com.voxable.core_ui.components.VoxAbleTopBar
import com.voxable.feature_media.presentation.components.MediaPlayerControls
import com.voxable.feature_media.presentation.components.SubtitleSelector

@Composable
fun MediaPlayerScreen(
    onBack: () -> Unit,
    viewModel: MediaPlayerViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val playlistPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.loadPlaylist(it) }
    }

    val mediaPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val mediaItem = com.voxable.feature_media.domain.model.MediaItem(
                id = it.lastPathSegment ?: "media",
                title = it.lastPathSegment ?: "Ortam",
                uri = it,
                type = com.voxable.feature_media.domain.model.MediaType.AUDIO
            )
            viewModel.playMedia(mediaItem)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is MediaPlayerEvent.ShowError -> snackbarHostState.showSnackbar(event.message)
                is MediaPlayerEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
                is MediaPlayerEvent.PlaylistLoaded -> snackbarHostState.showSnackbar("Çalma listesi yüklendi")
                is MediaPlayerEvent.SubtitlesLoaded -> snackbarHostState.showSnackbar("Altyazılar yüklendi")
                is MediaPlayerEvent.PlaybackEnded -> snackbarHostState.showSnackbar("Oynatma bitti")
            }
        }
    }

    val contentDescription = if (state.currentMediaItem != null) state.currentMediaItem!!.title else "Ortam Oynatıcı"

    Scaffold(
        topBar = {
            VoxAbleTopBar(
                title = contentDescription,
                showBackButton = true,
                onBackClick = onBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (state.currentMediaItem != null) {
                // Media info card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.currentMediaItem.title,
                            style = MaterialTheme.typography.titleLarge
                        )
                        if (state.currentMediaItem.metadata?.album != null) {
                            Text(
                                text = state.currentMediaItem.metadata.album ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                // Empty state with media picker
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AudioFile,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text("Ortam seçin", style = MaterialTheme.typography.bodyLarge)

                        Button(
                            onClick = { mediaPicker.launch("audio/*") },
                            modifier = Modifier.semantics {
                                contentDescription = "Ses dosyası aç"
                                role = Role.Button
                            }
                        ) {
                            Icon(Icons.Default.AudioFile, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Ses Aç")
                        }

                        Button(
                            onClick = { mediaPicker.launch("video/*") },
                            modifier = Modifier.semantics {
                                contentDescription = "Video dosyası aç"
                                role = Role.Button
                            }
                        ) {
                            Icon(Icons.Default.VideoFile, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Video Aç")
                        }

                        Button(
                            onClick = { playlistPicker.launch("application/x-mpegurl") },
                            modifier = Modifier.semantics {
                                contentDescription = "M3U çalma listesi aç"
                                role = Role.Button
                            }
                        ) {
                            Icon(Icons.Default.PlaylistPlay, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("M3U Aç")
                        }
                    }
                }
            }

            // Media controls
            if (state.currentMediaItem != null) {
                MediaPlayerControls(
                    isPlaying = state.isPlaying,
                    currentPosition = state.currentPosition,
                    duration = state.duration,
                    onPlayPause = viewModel::onPlayPauseClick,
                    onSeek = viewModel::onSeek,
                    onNext = { /* TODO: Implement next */ },
                    onPrevious = { /* TODO: Implement previous */ },
                    onSubtitleClick = viewModel::toggleSubtitleSettings,
                    selectedSubtitle = state.selectedSubtitleTrack,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Subtitle selector
            if (state.showSubtitleSettings && state.availableSubtitleTracks.isNotEmpty()) {
                SubtitleSelector(
                    subtitleTracks = state.availableSubtitleTracks,
                    selectedTrack = state.selectedSubtitleTrack,
                    onSelectTrack = viewModel::selectSubtitleTrack
                )
            }

            // Loading indicator
            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.primary)
                )
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            // Error display
            if (state.error != null) {
                Text(
                    text = "Hata: ${state.error}",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}