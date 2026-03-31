package com.voxable.feature_media.accessibility

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp

@Composable
fun AccessibleMediaPlayerButton(
    icon: androidx.compose.material.icons.materialIcon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    isEnabled: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isActive: Boolean = false
) {
    IconButton(
        onClick = onClick,
        enabled = isEnabled,
        modifier = modifier
            .size(44.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(
                if (isActive) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .semantics {
                contentDescription = description
                role = Role.Button
                stateDescription = if (isActive) "etkin" else "pasif"
                if (!isEnabled) {
                    disabled()
                }
            }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun AccessibleVolumeControl(
    currentVolume: Float,
    onVolumeChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(12.dp)
            .semantics {
                contentDescription = "Ses kontrolü: %${(currentVolume * 100).toInt()}"
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.VolumeDown,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Slider(
            value = currentVolume,
            onValueChange = onVolumeChange,
            valueRange = 0f..1f,
            modifier = Modifier
                .weight(1f)
                .semantics {
                    contentDescription = "Ses seviyesi: %${(currentVolume * 100).toInt()}"
                }
        )
        Icon(
            imageVector = Icons.Default.VolumeUp,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun AccessiblePlaybackSpeedControl(
    currentSpeed: Float,
    onSpeedChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Oynatma Hızı: ${String.format("%.1f", currentSpeed)}x",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.semantics {
                contentDescription = "Oynatma hızı kontrolü: ${String.format("%.1f", currentSpeed)} kat"
            }
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { speed ->
                Button(
                    onClick = { onSpeedChange(speed) },
                    modifier = Modifier
                        .weight(1f)
                        .semantics {
                            contentDescription = "${String.format("%.1f", speed)}x hıza ayarla"
                            role = Role.Button
                        },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (currentSpeed == speed)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.surface
                    )
                ) {
                    Text("${speed}x", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
fun AccessibleSubtitleSettings(
    isSubtitleEnabled: Boolean,
    onToggleSubtitle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Altyazılar",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.semantics {
                contentDescription = if (isSubtitleEnabled) "Altyazılar açık" else "Altyazılar kapalı"
            }
        )
        Switch(
            checked = isSubtitleEnabled,
            onCheckedChange = onToggleSubtitle,
            modifier = Modifier.semantics {
                contentDescription = if (isSubtitleEnabled) "Altyazıları kapat" else "Altyazıları aç"
                role = Role.Switch
                stateDescription = if (isSubtitleEnabled) "açık" else "kapalı"
            }
        )
    }
}