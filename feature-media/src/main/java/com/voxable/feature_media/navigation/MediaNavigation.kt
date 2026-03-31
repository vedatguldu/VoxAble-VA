package com.voxable.feature_media.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.voxable.feature_media.presentation.MediaScreen

const val MEDIA_ROUTE = "media"

fun NavGraphBuilder.mediaScreen(
    onBack: () -> Unit
) {
    composable(route = MEDIA_ROUTE) {
        MediaScreen(onBack = onBack)
    }
}
