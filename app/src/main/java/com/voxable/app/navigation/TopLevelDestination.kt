package com.voxable.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

enum class TopLevelDestination(
    val route: String,
    val icon: ImageVector,
    val label: String,
    val contentDescription: String
) {
    HOME(
        route = "home",
        icon = Icons.Default.Home,
        label = "Ana Sayfa",
        contentDescription = "Ana Sayfa sekmesi"
    ),
    READER(
        route = "reader",
        icon = Icons.Default.MenuBook,
        label = "Okuyucu",
        contentDescription = "Metin Okuyucu sekmesi"
    ),
    MEDIA(
        route = "media",
        icon = Icons.Default.PlayCircle,
        label = "Medya",
        contentDescription = "Medya Oynatıcı sekmesi"
    ),
    SETTINGS(
        route = "settings",
        icon = Icons.Default.Settings,
        label = "Ayarlar",
        contentDescription = "Ayarlar sekmesi"
    )
}
