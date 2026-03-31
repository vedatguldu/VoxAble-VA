package com.voxable.feature_media.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.voxable.feature_media.presentation.MediaPlayerScreen

fun NavGraphBuilder.mediaNavigation(onBack: () -> Unit) {
    composable("media_player") {
        MediaPlayerScreen(onBack = onBack)
    }
}