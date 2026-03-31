package com.voxable.feature_media.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.voxable.feature_media.domain.model.SubtitleTrack

@Composable
fun MediaPlayerControls(
    isPlaying: Boolean,
    currentPosition: Long,
    duration: Long,
    onPlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSubtitleClick: () -> Unit,
    selectedSubtitle: SubtitleTrack?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Progress bar
        ProgressSlider(
            currentPosition = currentPosition,
            duration = duration,
            onSeek = onSeek
        )

        // Timing
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formatTime(currentPosition),
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.semantics {
                    contentDescription = "Güncel konum: ${formatTime(currentPosition)}"
                }
            )
            Text(
                text = formatTime(duration),
                style = MaterialTheme.typography.labelSmall
            )
        }

        // Control buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "Oynatıcı kontrolleri" },
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onPrevious,
                modifier = Modifier
                    .size(40.dp)
                    .semantics { contentDescription = "Önceki"; role = Role.Button }
            ) {
                Icon(Icons.Default.SkipPrevious, contentDescription = null)
            }

            IconButton(
                onClick = onPlayPause,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .semantics {
                        contentDescription = if (isPlaying) "Duraklat" else "Oynat"
                        role = Role.Button
                    }
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }

            IconButton(
                onClick = onNext,
                modifier = Modifier
                    .size(40.dp)
                    .semantics { contentDescription = "Sonraki"; role = Role.Button }
            ) {
                Icon(Icons.Default.SkipNext, contentDescription = null)
            }

            Spacer(modifier = Modifier.weight(1f))

            IconButton(
                onClick = onSubtitleClick,
                modifier = Modifier
                    .size(40.dp)
                    .semantics {
                        contentDescription = if (selectedSubtitle != null) "Altyazı: ${selectedSubtitle.language}" else "Altyazı"
                        role = Role.Button
                    }
            ) {
                Icon(
                    imageVector = if (selectedSubtitle != null) Icons.Default.ClosedCaption else Icons.Default.ClosedCaptionDisabled,
                    contentDescription = null
                )
            }
        }
    }
}

@Composable
fun ProgressSlider(
    currentPosition: Long,
    duration: Long,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Slider(
        value = if (duration > 0) (currentPosition.toFloat() / duration) else 0f,
        onValueChange = { value ->
            onSeek((value * duration).toLong())
        },
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "İlerleme: ${(currentPosition.toFloat() / maxOf(duration, 1L)) * 100}%"
            },
        valueRange = 0f..1f
    )
}

@Composable
fun SubtitleSelector(
    subtitleTracks: List<SubtitleTrack>,
    selectedTrack: SubtitleTrack?,
    onSelectTrack: (SubtitleTrack?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Altyazı Seç",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.semantics { contentDescription = "Altyazı seçimi" }
        )

        // No subtitle option
        SelectableChip(
            label = "Altyazı Yok",
            isSelected = selectedTrack == null,
            onClick = { onSelectTrack(null) }
        )

        // Subtitle tracks
        subtitleTracks.forEach { track ->
            SelectableChip(
                label = "${track.label} (${track.format})",
                isSelected = selectedTrack?.id == track.id,
                onClick = { onSelectTrack(track) }
            )
        }
    }
}

@Composable
fun SelectableChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = if (isSelected) "$label seçili" else label
                role = Role.Button
            },
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Text(label)
    }
}

fun formatTime(milliseconds: Long): String {
    val totalSeconds = milliseconds / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return when {
        hours > 0 -> String.format("%d:%02d:%02d", hours, minutes, seconds)
        else -> String.format("%02d:%02d", minutes, seconds)
    }
}