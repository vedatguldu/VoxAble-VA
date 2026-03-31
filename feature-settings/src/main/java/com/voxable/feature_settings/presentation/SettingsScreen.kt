package com.voxable.feature_settings.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
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
fun SettingsScreen(
    onBack: () -> Unit,
    onSignOut: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is SettingsEvent.SignOutSuccess -> onSignOut()
                is SettingsEvent.ShowError -> { /* Snackbar */ }
                is SettingsEvent.ShowMessage -> { /* Snackbar */ }
            }
        }
    }

    Scaffold(
        topBar = {
            VoxAbleTopBar(
                title = "Ayarlar",
                onBack = onBack
            )
        }
    ) { padding ->
        if (state.isLoading) {
            LoadingIndicator()
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Görünüm bölümü
            SettingsSectionHeader(title = "Görünüm")

            // Karanlık mod
            SettingsToggleItem(
                title = "Karanlık Mod",
                description = "Koyu tema kullan",
                checked = state.isDarkMode,
                onCheckedChange = { viewModel.onDarkModeToggle(it) },
                accessibilityLabel = "Karanlık mod ${if (state.isDarkMode) "açık" else "kapalı"}"
            )

            // Yüksek kontrast
            SettingsToggleItem(
                title = "Yüksek Kontrast",
                description = "Erişilebilirlik için yüksek kontrast renkleri kullan",
                checked = state.isHighContrast,
                onCheckedChange = { viewModel.onHighContrastToggle(it) },
                accessibilityLabel = "Yüksek kontrast ${if (state.isHighContrast) "açık" else "kapalı"}"
            )

            // Yazı boyutu
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Yazı Boyutu",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Çarpan: ${"%.1f".format(state.fontSize)}x",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Slider(
                    value = state.fontSize,
                    onValueChange = { viewModel.onFontSizeChanged(it) },
                    valueRange = 0.8f..2.0f,
                    steps = 5,
                    modifier = Modifier.semantics {
                        contentDescription = "Yazı boyutu çarpanı: ${"%.1f".format(state.fontSize)}"
                    }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // Erişilebilirlik bölümü
            SettingsSectionHeader(title = "Erişilebilirlik")

            SettingsToggleItem(
                title = "TalkBack İpuçları",
                description = "Ekran okuyucu için ek ipuçları göster",
                checked = state.talkBackHintsEnabled,
                onCheckedChange = { viewModel.onTalkBackHintsToggle(it) },
                accessibilityLabel = "TalkBack ipuçları ${if (state.talkBackHintsEnabled) "açık" else "kapalı"}"
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // Dil bölümü
            SettingsSectionHeader(title = "Dil")

            val languages = listOf("tr" to "Türkçe", "en" to "English")
            languages.forEach { (code, label) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = state.selectedLanguage == code,
                        onClick = { viewModel.onLanguageChanged(code) },
                        modifier = Modifier.semantics {
                            contentDescription = "$label dili${if (state.selectedLanguage == code) ", seçili" else ""}"
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = label, style = MaterialTheme.typography.bodyLarge)
                }
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            Spacer(modifier = Modifier.height(16.dp))

            // Çıkış butonu
            OutlinedButton(
                onClick = { viewModel.onSignOut() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .heightIn(min = 56.dp)
                    .semantics { contentDescription = "Oturumu kapat" },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Çıkış Yap")
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .semantics { heading() }
    )
}

@Composable
private fun SettingsToggleItem(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    accessibilityLabel: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.semantics {
                contentDescription = accessibilityLabel
            }
        )
    }
}
