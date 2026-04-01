package com.voxable.feature_settings.domain.repository

import com.voxable.core.util.Resource
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {

    // ─── Görünüm ──────────────────────────────────────────
    suspend fun setDarkMode(enabled: Boolean)
    fun isDarkMode(): Flow<Boolean>

    suspend fun setHighContrast(enabled: Boolean)
    fun isHighContrast(): Flow<Boolean>

    suspend fun setFontSize(scale: Float)
    fun getFontSize(): Flow<Float>

    // ─── Dil ──────────────────────────────────────────────
    suspend fun setLanguage(languageCode: String)
    fun getLanguage(): Flow<String>

    // ─── Ses Ayarları ─────────────────────────────────────
    suspend fun setVoiceEnabled(enabled: Boolean)
    fun isVoiceEnabled(): Flow<Boolean>

    suspend fun setVoiceSpeed(speed: Float)
    fun getVoiceSpeed(): Flow<Float>

    suspend fun setVoicePitch(pitch: Float)
    fun getVoicePitch(): Flow<Float>

    suspend fun setAutoRead(enabled: Boolean)
    fun isAutoRead(): Flow<Boolean>

    // ─── Erişilebilirlik ──────────────────────────────────
    suspend fun setTalkBackHints(enabled: Boolean)
    fun isTalkBackHintsEnabled(): Flow<Boolean>

    suspend fun setHapticFeedback(enabled: Boolean)
    fun isHapticFeedback(): Flow<Boolean>

    suspend fun setReduceMotion(enabled: Boolean)
    fun isReduceMotion(): Flow<Boolean>

    suspend fun setScreenReaderOptimization(enabled: Boolean)
    fun isScreenReaderOptimization(): Flow<Boolean>

    suspend fun setTouchTargetSize(size: String)
    fun getTouchTargetSize(): Flow<String>

    // ─── Hesap ────────────────────────────────────────────
    suspend fun signOut(): Resource<Unit>
}
