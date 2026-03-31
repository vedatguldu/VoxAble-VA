package com.voxable.feature_downloader.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.voxable.feature_downloader.presentation.DownloaderScreen

const val DOWNLOADER_ROUTE = "downloader"

fun NavGraphBuilder.downloaderScreen(
    onBack: () -> Unit
) {
    composable(route = DOWNLOADER_ROUTE) {
        DownloaderScreen(onBack = onBack)
    }
}
