package com.voxable.feature_settings.presentation

import com.voxable.core.base.UiEvent
import com.voxable.core.base.UiState

data class SettingsState(
    val isDarkMode: Boolean = false,
    val isHighContrast: Boolean = false,
    val fontSize: Float = 1.0f, // 1.0 = normal, büyütme/küçültme çarpanı
    val talkBackHintsEnabled: Boolean = true,
    val selectedLanguage: String = "tr",
    val userName: String = "",
    val userEmail: String = "",
    val isLoading: Boolean = false
) : UiState

sealed interface SettingsEvent : UiEvent {
    data object SignOutSuccess : SettingsEvent
    data class ShowError(val message: String) : SettingsEvent
    data class ShowMessage(val message: String) : SettingsEvent
}
