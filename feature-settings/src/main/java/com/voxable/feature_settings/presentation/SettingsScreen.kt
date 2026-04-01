package com.voxable.feature_settings.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.voxable.core_ui.components.LoadingIndicator
import com.voxable.core_ui.components.VoxAbleTopBar
import com.voxable.feature_settings.domain.model.AppLanguage
import com.voxable.feature_settings.domain.model.TouchTargetSize

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onSignOut: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is SettingsEvent.SignOutSuccess -> onSignOut()
                is SettingsEvent.ShowError -> snackbarHostState.showSnackbar(event.message)
                is SettingsEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
                is SettingsEvent.LanguageChanged -> { /* Activity recreation can be triggered here */ }
            }
        }
    }

    Scaffold(
        topBar = {
            VoxAbleTopBar(
                title = "Ayarlar",
                showBackButton = true,
                onBackClick = onBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
            // ─── Dil Ayarları ──────────────────────────────
            LanguageSection(
                selectedLanguage = state.selectedLanguage,
                availableLanguages = state.availableLanguages,
                onLanguageChanged = viewModel::onLanguageChanged
            )

            // ─── Ses Ayarları ──────────────────────────────
            VoiceSection(
                isVoiceEnabled = state.isVoiceEnabled,
                voiceSpeed = state.voiceSpeed,
                voicePitch = state.voicePitch,
                isAutoRead = state.isAutoRead,
                isTestingSpeech = state.isTestingSpeech,
                onVoiceEnabledToggle = viewModel::onVoiceEnabledToggle,
                onVoiceSpeedChanged = viewModel::onVoiceSpeedChanged,
                onVoicePitchChanged = viewModel::onVoicePitchChanged,
                onAutoReadToggle = viewModel::onAutoReadToggle,
                onTestVoice = viewModel::onTestVoice,
                onStopTestVoice = viewModel::onStopTestVoice
            )

            // ─── Erişilebilirlik Ayarları ──────────────────
            AccessibilitySection(
                isDarkMode = state.isDarkMode,
                isHighContrast = state.isHighContrast,
                fontSize = state.fontSize,
                talkBackHintsEnabled = state.talkBackHintsEnabled,
                hapticFeedbackEnabled = state.hapticFeedbackEnabled,
                reduceMotionEnabled = state.reduceMotionEnabled,
                screenReaderOptEnabled = state.screenReaderOptEnabled,
                touchTargetSize = state.touchTargetSize,
                onDarkModeToggle = viewModel::onDarkModeToggle,
                onHighContrastToggle = viewModel::onHighContrastToggle,
                onFontSizeChanged = viewModel::onFontSizeChanged,
                onTalkBackHintsToggle = viewModel::onTalkBackHintsToggle,
                onHapticFeedbackToggle = viewModel::onHapticFeedbackToggle,
                onReduceMotionToggle = viewModel::onReduceMotionToggle,
                onScreenReaderOptToggle = viewModel::onScreenReaderOptToggle,
                onTouchTargetSizeChanged = viewModel::onTouchTargetSizeChanged
            )

            // ─── Hesap ────────────────────────────────────
            AccountSection(onSignOut = viewModel::onSignOut)

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ════════════════════════════════════════════════════════════
// Dil Bölümü
// ════════════════════════════════════════════════════════════

@Composable
private fun LanguageSection(
    selectedLanguage: AppLanguage,
    availableLanguages: List<AppLanguage>,
    onLanguageChanged: (AppLanguage) -> Unit
) {
    SettingsSectionHeader(
        title = "Dil Ayarları",
        icon = Icons.Default.Language
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            availableLanguages.forEach { language ->
                val isSelected = selectedLanguage == language
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onLanguageChanged(language) }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .semantics {
                            contentDescription = "${language.nativeName} dili${if (isSelected) ", seçili" else ""}"
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = { onLanguageChanged(language) }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = language.nativeName,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface
                        )
                        if (language.nativeName != language.englishName) {
                            Text(
                                text = language.englishName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(8.dp))
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
}

// ════════════════════════════════════════════════════════════
// Ses Bölümü
// ════════════════════════════════════════════════════════════

@Composable
private fun VoiceSection(
    isVoiceEnabled: Boolean,
    voiceSpeed: Float,
    voicePitch: Float,
    isAutoRead: Boolean,
    isTestingSpeech: Boolean,
    onVoiceEnabledToggle: (Boolean) -> Unit,
    onVoiceSpeedChanged: (Float) -> Unit,
    onVoicePitchChanged: (Float) -> Unit,
    onAutoReadToggle: (Boolean) -> Unit,
    onTestVoice: () -> Unit,
    onStopTestVoice: () -> Unit
) {
    SettingsSectionHeader(
        title = "Ses Ayarları",
        icon = Icons.Default.RecordVoiceOver
    )

    SettingsToggleItem(
        title = "Sesli Geri Bildirim",
        description = "Uygulama içi sesli rehberlik",
        checked = isVoiceEnabled,
        onCheckedChange = onVoiceEnabledToggle,
        accessibilityLabel = "Sesli geri bildirim ${if (isVoiceEnabled) "açık" else "kapalı"}"
    )

    AnimatedVisibility(
        visible = isVoiceEnabled,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        Column {
            // Ses hızı
            SettingsSliderItem(
                title = "Konuşma Hızı",
                value = voiceSpeed,
                valueRange = 0.5f..2.0f,
                steps = 5,
                valueLabel = "${"%.1f".format(voiceSpeed)}x",
                onValueChange = onVoiceSpeedChanged,
                accessibilityLabel = "Konuşma hızı: ${"%.1f".format(voiceSpeed)}"
            )

            // Ses tonu
            SettingsSliderItem(
                title = "Ses Tonu",
                value = voicePitch,
                valueRange = 0.5f..2.0f,
                steps = 5,
                valueLabel = "${"%.1f".format(voicePitch)}x",
                onValueChange = onVoicePitchChanged,
                accessibilityLabel = "Ses tonu: ${"%.1f".format(voicePitch)}"
            )

            // Otomatik okuma
            SettingsToggleItem(
                title = "Otomatik Okuma",
                description = "Ekran açıldığında içeriği otomatik oku",
                checked = isAutoRead,
                onCheckedChange = onAutoReadToggle,
                accessibilityLabel = "Otomatik okuma ${if (isAutoRead) "açık" else "kapalı"}"
            )

            // Ses testi butonu
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = if (isTestingSpeech) onStopTestVoice else onTestVoice,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .semantics {
                            contentDescription = if (isTestingSpeech) "Sesi durdur" else "Sesi test et"
                        }
                ) {
                    Icon(
                        imageVector = if (isTestingSpeech) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isTestingSpeech) "Durdur" else "Sesi Test Et")
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(8.dp))
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
}

// ════════════════════════════════════════════════════════════
// Erişilebilirlik Bölümü
// ════════════════════════════════════════════════════════════

@Composable
private fun AccessibilitySection(
    isDarkMode: Boolean,
    isHighContrast: Boolean,
    fontSize: Float,
    talkBackHintsEnabled: Boolean,
    hapticFeedbackEnabled: Boolean,
    reduceMotionEnabled: Boolean,
    screenReaderOptEnabled: Boolean,
    touchTargetSize: TouchTargetSize,
    onDarkModeToggle: (Boolean) -> Unit,
    onHighContrastToggle: (Boolean) -> Unit,
    onFontSizeChanged: (Float) -> Unit,
    onTalkBackHintsToggle: (Boolean) -> Unit,
    onHapticFeedbackToggle: (Boolean) -> Unit,
    onReduceMotionToggle: (Boolean) -> Unit,
    onScreenReaderOptToggle: (Boolean) -> Unit,
    onTouchTargetSizeChanged: (TouchTargetSize) -> Unit
) {
    SettingsSectionHeader(
        title = "Erişilebilirlik",
        icon = Icons.Default.Accessibility
    )

    // ── Görünüm alt bölümü ──
    Text(
        text = "Görünüm",
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
    )

    SettingsToggleItem(
        title = "Karanlık Mod",
        description = "Koyu tema kullan",
        checked = isDarkMode,
        onCheckedChange = onDarkModeToggle,
        accessibilityLabel = "Karanlık mod ${if (isDarkMode) "açık" else "kapalı"}"
    )

    SettingsToggleItem(
        title = "Yüksek Kontrast",
        description = "WCAG AAA uyumlu yüksek kontrast renkleri",
        checked = isHighContrast,
        onCheckedChange = onHighContrastToggle,
        accessibilityLabel = "Yüksek kontrast ${if (isHighContrast) "açık" else "kapalı"}"
    )

    SettingsSliderItem(
        title = "Yazı Boyutu",
        value = fontSize,
        valueRange = 0.8f..2.0f,
        steps = 5,
        valueLabel = "${"%.1f".format(fontSize)}x",
        onValueChange = onFontSizeChanged,
        accessibilityLabel = "Yazı boyutu çarpanı: ${"%.1f".format(fontSize)}"
    )

    Spacer(modifier = Modifier.height(8.dp))

    // ── Ekran okuyucu alt bölümü ──
    Text(
        text = "Ekran Okuyucu",
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
    )

    SettingsToggleItem(
        title = "TalkBack İpuçları",
        description = "Ekran okuyucu için ek yardım ipuçları",
        checked = talkBackHintsEnabled,
        onCheckedChange = onTalkBackHintsToggle,
        accessibilityLabel = "TalkBack ipuçları ${if (talkBackHintsEnabled) "açık" else "kapalı"}"
    )

    SettingsToggleItem(
        title = "Ekran Okuyucu Optimizasyonu",
        description = "TalkBack ve sesli erişim için düzen optimizasyonu",
        checked = screenReaderOptEnabled,
        onCheckedChange = onScreenReaderOptToggle,
        accessibilityLabel = "Ekran okuyucu optimizasyonu ${if (screenReaderOptEnabled) "açık" else "kapalı"}"
    )

    Spacer(modifier = Modifier.height(8.dp))

    // ── Etkileşim alt bölümü ──
    Text(
        text = "Etkileşim",
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
    )

    SettingsToggleItem(
        title = "Dokunsal Geri Bildirim",
        description = "Buton tıklamalarında titreşim",
        checked = hapticFeedbackEnabled,
        onCheckedChange = onHapticFeedbackToggle,
        accessibilityLabel = "Dokunsal geri bildirim ${if (hapticFeedbackEnabled) "açık" else "kapalı"}"
    )

    SettingsToggleItem(
        title = "Hareketleri Azalt",
        description = "Animasyon ve geçiş efektlerini kapat",
        checked = reduceMotionEnabled,
        onCheckedChange = onReduceMotionToggle,
        accessibilityLabel = "Hareketleri azalt ${if (reduceMotionEnabled) "açık" else "kapalı"}"
    )

    // Dokunma hedefi boyutu
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Dokunma Hedefi Boyutu",
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "Buton ve kontrol öğelerinin minimum boyutu",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            TouchTargetSize.entries.forEach { size ->
                val isSelected = touchTargetSize == size
                FilterChip(
                    selected = isSelected,
                    onClick = { onTouchTargetSizeChanged(size) },
                    label = {
                        Text(
                            text = "${size.label} (${size.minSizeDp}dp)",
                            style = MaterialTheme.typography.bodySmall
                        )
                    },
                    modifier = Modifier.semantics {
                        contentDescription = "${size.label} dokunma hedefi${if (isSelected) ", seçili" else ""}"
                    }
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(8.dp))
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
}

// ════════════════════════════════════════════════════════════
// Hesap Bölümü
// ════════════════════════════════════════════════════════════

@Composable
private fun AccountSection(onSignOut: () -> Unit) {
    SettingsSectionHeader(
        title = "Hesap",
        icon = Icons.Default.Person
    )

    OutlinedButton(
        onClick = onSignOut,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .heightIn(min = 56.dp)
            .semantics { contentDescription = "Oturumu kapat" },
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.error
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
    ) {
        Icon(
            imageVector = Icons.Default.ExitToApp,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("Çıkış Yap")
    }
}

// ════════════════════════════════════════════════════════════
// Ortak alt bileşenler
// ════════════════════════════════════════════════════════════

@Composable
private fun SettingsSectionHeader(
    title: String,
    icon: ImageVector? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .semantics { heading() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
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
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 10.dp),
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
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.semantics {
                contentDescription = accessibilityLabel
            }
        )
    }
}

@Composable
private fun SettingsSliderItem(
    title: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    valueLabel: String,
    onValueChange: (Float) -> Unit,
    accessibilityLabel: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = valueLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            modifier = Modifier.semantics {
                contentDescription = accessibilityLabel
            }
        )
    }
}
