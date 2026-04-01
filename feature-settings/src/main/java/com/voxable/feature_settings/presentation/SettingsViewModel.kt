package com.voxable.feature_settings.presentation

import androidx.lifecycle.viewModelScope
import com.voxable.core.base.BaseViewModel
import com.voxable.core.util.onError
import com.voxable.core.util.onSuccess
import com.voxable.core_accessibility.VoiceFeedbackManager
import com.voxable.feature_settings.domain.model.AppLanguage
import com.voxable.feature_settings.domain.model.TouchTargetSize
import com.voxable.feature_settings.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val voiceFeedbackManager: VoiceFeedbackManager
) : BaseViewModel<SettingsState, SettingsEvent>(SettingsState()) {

    init {
        observeSettings()
    }

    private fun observeSettings() {
        // Görünüm
        settingsRepository.isDarkMode()
            .onEach { updateState { copy(isDarkMode = it) } }
            .launchIn(viewModelScope)

        settingsRepository.isHighContrast()
            .onEach { updateState { copy(isHighContrast = it) } }
            .launchIn(viewModelScope)

        settingsRepository.getFontSize()
            .onEach { updateState { copy(fontSize = it) } }
            .launchIn(viewModelScope)

        // Dil
        settingsRepository.getLanguage()
            .onEach { updateState { copy(selectedLanguage = AppLanguage.fromCode(it)) } }
            .launchIn(viewModelScope)

        // Ses
        settingsRepository.isVoiceEnabled()
            .onEach { updateState { copy(isVoiceEnabled = it) } }
            .launchIn(viewModelScope)

        settingsRepository.getVoiceSpeed()
            .onEach { updateState { copy(voiceSpeed = it) } }
            .launchIn(viewModelScope)

        settingsRepository.getVoicePitch()
            .onEach { updateState { copy(voicePitch = it) } }
            .launchIn(viewModelScope)

        settingsRepository.isAutoRead()
            .onEach { updateState { copy(isAutoRead = it) } }
            .launchIn(viewModelScope)

        // Erişilebilirlik
        settingsRepository.isTalkBackHintsEnabled()
            .onEach { updateState { copy(talkBackHintsEnabled = it) } }
            .launchIn(viewModelScope)

        settingsRepository.isHapticFeedback()
            .onEach { updateState { copy(hapticFeedbackEnabled = it) } }
            .launchIn(viewModelScope)

        settingsRepository.isReduceMotion()
            .onEach { updateState { copy(reduceMotionEnabled = it) } }
            .launchIn(viewModelScope)

        settingsRepository.isScreenReaderOptimization()
            .onEach { updateState { copy(screenReaderOptEnabled = it) } }
            .launchIn(viewModelScope)

        settingsRepository.getTouchTargetSize()
            .onEach { updateState { copy(touchTargetSize = TouchTargetSize.fromName(it)) } }
            .launchIn(viewModelScope)
    }

    // ─── Görünüm ──────────────────────────────────────────

    fun onDarkModeToggle(enabled: Boolean) {
        launch { settingsRepository.setDarkMode(enabled) }
    }

    fun onHighContrastToggle(enabled: Boolean) {
        launch { settingsRepository.setHighContrast(enabled) }
    }

    fun onFontSizeChanged(scale: Float) {
        launch { settingsRepository.setFontSize(scale) }
    }

    // ─── Dil ──────────────────────────────────────────────

    fun onLanguageChanged(language: AppLanguage) {
        launch {
            settingsRepository.setLanguage(language.code)
            sendEvent(SettingsEvent.LanguageChanged)
            sendEvent(SettingsEvent.ShowMessage("Dil değiştirildi: ${language.nativeName}"))
        }
    }

    // ─── Ses Ayarları ─────────────────────────────────────

    fun onVoiceEnabledToggle(enabled: Boolean) {
        launch { settingsRepository.setVoiceEnabled(enabled) }
    }

    fun onVoiceSpeedChanged(speed: Float) {
        launch {
            settingsRepository.setVoiceSpeed(speed)
            voiceFeedbackManager.setSpeechRate(speed)
        }
    }

    fun onVoicePitchChanged(pitch: Float) {
        launch { settingsRepository.setVoicePitch(pitch) }
    }

    fun onAutoReadToggle(enabled: Boolean) {
        launch { settingsRepository.setAutoRead(enabled) }
    }

    fun onTestVoice() {
        val state = uiState.value
        if (!state.isVoiceEnabled) return

        updateState { copy(isTestingSpeech = true) }

        val locale = Locale(state.selectedLanguage.code)
        voiceFeedbackManager.initTts(locale)
        voiceFeedbackManager.setSpeechRate(state.voiceSpeed)

        val testText = when (state.selectedLanguage) {
            AppLanguage.TURKISH -> "Merhaba, bu bir ses testi cümlesidir. Hız ve ton ayarlarınızı kontrol edin."
            AppLanguage.ENGLISH -> "Hello, this is a voice test sentence. Check your speed and pitch settings."
            AppLanguage.GERMAN -> "Hallo, dies ist ein Sprachtest. Überprüfen Sie Ihre Geschwindigkeit und Tonhöhe."
            AppLanguage.FRENCH -> "Bonjour, ceci est un test de voix. Vérifiez vos paramètres de vitesse et de ton."
            AppLanguage.SPANISH -> "Hola, esta es una prueba de voz. Verifique sus ajustes de velocidad y tono."
            AppLanguage.ARABIC -> "مرحبا، هذا اختبار صوتي. تحقق من إعدادات السرعة والنبرة."
        }

        voiceFeedbackManager.speak(text = testText) {
            updateState { copy(isTestingSpeech = false) }
        }
    }

    fun onStopTestVoice() {
        voiceFeedbackManager.stopSpeaking()
        updateState { copy(isTestingSpeech = false) }
    }

    // ─── Erişilebilirlik ──────────────────────────────────

    fun onTalkBackHintsToggle(enabled: Boolean) {
        launch { settingsRepository.setTalkBackHints(enabled) }
    }

    fun onHapticFeedbackToggle(enabled: Boolean) {
        launch { settingsRepository.setHapticFeedback(enabled) }
    }

    fun onReduceMotionToggle(enabled: Boolean) {
        launch { settingsRepository.setReduceMotion(enabled) }
    }

    fun onScreenReaderOptToggle(enabled: Boolean) {
        launch { settingsRepository.setScreenReaderOptimization(enabled) }
    }

    fun onTouchTargetSizeChanged(size: TouchTargetSize) {
        launch { settingsRepository.setTouchTargetSize(size.name) }
    }

    // ─── Hesap ────────────────────────────────────────────

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

    override fun onCleared() {
        super.onCleared()
        voiceFeedbackManager.stopSpeaking()
    }
}
