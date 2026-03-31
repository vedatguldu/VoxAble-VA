package com.voxable.feature_settings.domain.repository

import com.voxable.core.util.Resource
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    suspend fun setDarkMode(enabled: Boolean)
    fun isDarkMode(): Flow<Boolean>

    suspend fun setHighContrast(enabled: Boolean)
    fun isHighContrast(): Flow<Boolean>

    suspend fun setFontSize(scale: Float)
    fun getFontSize(): Flow<Float>

    suspend fun setTalkBackHints(enabled: Boolean)
    fun isTalkBackHintsEnabled(): Flow<Boolean>

    suspend fun setLanguage(languageCode: String)
    fun getLanguage(): Flow<String>

    suspend fun signOut(): Resource<Unit>
}
