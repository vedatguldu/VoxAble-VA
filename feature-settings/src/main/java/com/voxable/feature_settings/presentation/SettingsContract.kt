package com.voxable.feature_settings.presentation

import com.voxable.core.base.UiEvent
import com.voxable.core.base.UiState
import com.voxable.feature_settings.domain.model.AppLanguage
import com.voxable.feature_settings.domain.model.TouchTargetSize

data class SettingsState(
    // Görünüm
    val isDarkMode: Boolean = false,
    val isHighContrast: Boolean = false,
    val fontSize: Float = 1.0f,

    // Dil
    val selectedLanguage: AppLanguage = AppLanguage.TURKISH,
    val availableLanguages: List<AppLanguage> = AppLanguage.entries,

    // Ses Ayarları
    val isVoiceEnabled: Boolean = true,
    val voiceSpeed: Float = 1.0f,
    val voicePitch: Float = 1.0f,
    val isAutoRead: Boolean = false,
    val isTestingSpeech: Boolean = false,

    // Erişilebilirlik
    val talkBackHintsEnabled: Boolean = true,
    val hapticFeedbackEnabled: Boolean = true,
    val reduceMotionEnabled: Boolean = false,
    val screenReaderOptEnabled: Boolean = true,
    val touchTargetSize: TouchTargetSize = TouchTargetSize.NORMAL,

    // Hesap
    val userName: String = "",
    val userEmail: String = "",
    val isLoading: Boolean = false
) : UiState

sealed interface SettingsEvent : UiEvent {
    data object SignOutSuccess : SettingsEvent
    data class ShowError(val message: String) : SettingsEvent
    data class ShowMessage(val message: String) : SettingsEvent
    data object LanguageChanged : SettingsEvent
}
