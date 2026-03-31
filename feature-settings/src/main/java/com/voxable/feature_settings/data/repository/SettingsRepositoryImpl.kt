package com.voxable.feature_settings.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.google.firebase.auth.FirebaseAuth
import com.voxable.core.util.Constants
import com.voxable.core.util.Resource
import com.voxable.feature_settings.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = Constants.DATASTORE_NAME
)

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firebaseAuth: FirebaseAuth
) : SettingsRepository {

    private object Keys {
        val DARK_MODE = booleanPreferencesKey(Constants.Preferences.KEY_DARK_MODE)
        val HIGH_CONTRAST = booleanPreferencesKey(Constants.Preferences.KEY_HIGH_CONTRAST)
        val FONT_SIZE = floatPreferencesKey(Constants.Preferences.KEY_FONT_SIZE)
        val TALKBACK_HINTS = booleanPreferencesKey(Constants.Preferences.KEY_TALKBACK_HINTS)
        val LANGUAGE = stringPreferencesKey(Constants.Preferences.KEY_LANGUAGE)
    }

    override suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { it[Keys.DARK_MODE] = enabled }
    }

    override fun isDarkMode(): Flow<Boolean> =
        context.dataStore.data.catch { emit(emptyPreferences()) }
            .map { it[Keys.DARK_MODE] ?: false }

    override suspend fun setHighContrast(enabled: Boolean) {
        context.dataStore.edit { it[Keys.HIGH_CONTRAST] = enabled }
    }

    override fun isHighContrast(): Flow<Boolean> =
        context.dataStore.data.catch { emit(emptyPreferences()) }
            .map { it[Keys.HIGH_CONTRAST] ?: false }

    override suspend fun setFontSize(scale: Float) {
        context.dataStore.edit { it[Keys.FONT_SIZE] = scale }
    }

    override fun getFontSize(): Flow<Float> =
        context.dataStore.data.catch { emit(emptyPreferences()) }
            .map { it[Keys.FONT_SIZE] ?: 1.0f }

    override suspend fun setTalkBackHints(enabled: Boolean) {
        context.dataStore.edit { it[Keys.TALKBACK_HINTS] = enabled }
    }

    override fun isTalkBackHintsEnabled(): Flow<Boolean> =
        context.dataStore.data.catch { emit(emptyPreferences()) }
            .map { it[Keys.TALKBACK_HINTS] ?: true }

    override suspend fun setLanguage(languageCode: String) {
        context.dataStore.edit { it[Keys.LANGUAGE] = languageCode }
    }

    override fun getLanguage(): Flow<String> =
        context.dataStore.data.catch { emit(emptyPreferences()) }
            .map { it[Keys.LANGUAGE] ?: "tr" }

    override suspend fun signOut(): Resource<Unit> {
        return try {
            firebaseAuth.signOut()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Çıkış yapılamadı")
        }
    }
}
