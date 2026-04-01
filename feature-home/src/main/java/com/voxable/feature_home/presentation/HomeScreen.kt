package com.voxable.feature_home.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.voxable.core_ui.components.VoxAbleTopBar

data class FeatureItem(
    val title: String,
    val icon: ImageVector,
    val description: String,
    val onClick: () -> Unit
)

@Composable
fun HomeScreen(
    onNavigateToReader: () -> Unit,
    onNavigateToMedia: () -> Unit,
    onNavigateToOcr: () -> Unit,
    onNavigateToCurrency: () -> Unit,
    onNavigateToConverter: () -> Unit,
    onNavigateToDownloader: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val features = listOf(
        FeatureItem("Okuyucu", Icons.Default.MenuBook, "Metin okuyucu özelliğini aç", onNavigateToReader),
        FeatureItem("Medya", Icons.Default.PlayCircle, "Medya oynatıcıyı aç", onNavigateToMedia),
        FeatureItem("OCR", Icons.Default.CameraAlt, "Metin tanıma özelliğini aç", onNavigateToOcr),
        FeatureItem("Döviz", Icons.Default.CurrencyExchange, "Döviz kurlarını görüntüle", onNavigateToCurrency),
        FeatureItem("Dönüştürücü", Icons.Default.SwapHoriz, "Birim dönüştürücüyü aç", onNavigateToConverter),
        FeatureItem("İndirici", Icons.Default.Download, "Dosya indiriciyi aç", onNavigateToDownloader),
        FeatureItem("Ayarlar", Icons.Default.Settings, "Uygulama ayarlarını aç", onNavigateToSettings)
    )

    Scaffold(
        topBar = {
            VoxAbleTopBar(title = "VoxAble")
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "Hoş geldiniz, ${state.userName}",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.semantics { heading() }
            )

            Spacer(modifier = Modifier.height(24.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(features) { feature ->
                    FeatureCard(feature = feature)
                }
            }
        }
    }
}

@Composable
private fun FeatureCard(feature: FeatureItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 120.dp)
            .clickable(onClick = feature.onClick)
            .semantics {
                contentDescription = feature.description
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = feature.icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = feature.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}
