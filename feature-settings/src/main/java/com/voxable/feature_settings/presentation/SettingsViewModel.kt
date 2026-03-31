package com.voxable.feature_settings.presentation

import androidx.lifecycle.viewModelScope
import com.voxable.core.base.BaseViewModel
import com.voxable.core.util.onError
import com.voxable.core.util.onSuccess
import com.voxable.feature_settings.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : BaseViewModel<SettingsState, SettingsEvent>(SettingsState()) {

    init {
        observeSettings()
    }

    private fun observeSettings() {
        settingsRepository.isDarkMode()
            .onEach { updateState { copy(isDarkMode = it) } }
            .launchIn(viewModelScope)

        settingsRepository.isHighContrast()
            .onEach { updateState { copy(isHighContrast = it) } }
            .launchIn(viewModelScope)

        settingsRepository.getFontSize()
            .onEach { updateState { copy(fontSize = it) } }
            .launchIn(viewModelScope)

        settingsRepository.isTalkBackHintsEnabled()
            .onEach { updateState { copy(talkBackHintsEnabled = it) } }
            .launchIn(viewModelScope)

        settingsRepository.getLanguage()
            .onEach { updateState { copy(selectedLanguage = it) } }
            .launchIn(viewModelScope)
    }

    fun onDarkModeToggle(enabled: Boolean) {
        launch { settingsRepository.setDarkMode(enabled) }
    }

    fun onHighContrastToggle(enabled: Boolean) {
        launch { settingsRepository.setHighContrast(enabled) }
    }

    fun onFontSizeChanged(scale: Float) {
        launch { settingsRepository.setFontSize(scale) }
    }

    fun onTalkBackHintsToggle(enabled: Boolean) {
        launch { settingsRepository.setTalkBackHints(enabled) }
    }

    fun onLanguageChanged(languageCode: String) {
        launch { settingsRepository.setLanguage(languageCode) }
    }

    fun onSignOut() {
        launch {
            updateState { copy(isLoading = true) }
            settingsRepository.signOut()
                .onSuccess {
                    updateState { copy(isLoading = false) }
                    sendEvent(SettingsEvent.SignOutSuccess)
                }
                .onError { message ->
                    updateState { copy(isLoading = false) }
                    sendEvent(SettingsEvent.ShowError(message))
                }
        }
    }
}
