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
        // Görünüm
        val DARK_MODE = booleanPreferencesKey(Constants.Preferences.KEY_DARK_MODE)
        val HIGH_CONTRAST = booleanPreferencesKey(Constants.Preferences.KEY_HIGH_CONTRAST)
        val FONT_SIZE = floatPreferencesKey(Constants.Preferences.KEY_FONT_SIZE)

        // Dil
        val LANGUAGE = stringPreferencesKey(Constants.Preferences.KEY_LANGUAGE)

        // Ses
        val VOICE_ENABLED = booleanPreferencesKey(Constants.Preferences.KEY_VOICE_ENABLED)
        val VOICE_SPEED = floatPreferencesKey(Constants.Preferences.KEY_VOICE_SPEED)
        val VOICE_PITCH = floatPreferencesKey(Constants.Preferences.KEY_VOICE_PITCH)
        val AUTO_READ = booleanPreferencesKey(Constants.Preferences.KEY_AUTO_READ)

        // Erişilebilirlik
        val TALKBACK_HINTS = booleanPreferencesKey(Constants.Preferences.KEY_TALKBACK_HINTS)
        val HAPTIC_FEEDBACK = booleanPreferencesKey(Constants.Preferences.KEY_HAPTIC_FEEDBACK)
        val REDUCE_MOTION = booleanPreferencesKey(Constants.Preferences.KEY_REDUCE_MOTION)
        val SCREEN_READER_OPT = booleanPreferencesKey(Constants.Preferences.KEY_SCREEN_READER_OPT)
        val TOUCH_TARGET_SIZE = stringPreferencesKey(Constants.Preferences.KEY_TOUCH_TARGET_SIZE)
    }

    private fun <T> readPreference(key: Preferences.Key<T>, defaultValue: T): Flow<T> =
        context.dataStore.data.catch { emit(emptyPreferences()) }
            .map { it[key] ?: defaultValue }

    private suspend fun <T> writePreference(key: Preferences.Key<T>, value: T) {
        context.dataStore.edit { it[key] = value }
    }

    // ─── Görünüm ──────────────────────────────────────────

    override suspend fun setDarkMode(enabled: Boolean) = writePreference(Keys.DARK_MODE, enabled)
    override fun isDarkMode(): Flow<Boolean> = readPreference(Keys.DARK_MODE, false)

    override suspend fun setHighContrast(enabled: Boolean) = writePreference(Keys.HIGH_CONTRAST, enabled)
    override fun isHighContrast(): Flow<Boolean> = readPreference(Keys.HIGH_CONTRAST, false)

    override suspend fun setFontSize(scale: Float) = writePreference(Keys.FONT_SIZE, scale)
    override fun getFontSize(): Flow<Float> = readPreference(Keys.FONT_SIZE, 1.0f)

    // ─── Dil ──────────────────────────────────────────────

    override suspend fun setLanguage(languageCode: String) = writePreference(Keys.LANGUAGE, languageCode)
    override fun getLanguage(): Flow<String> = readPreference(Keys.LANGUAGE, "tr")

    // ─── Ses Ayarları ─────────────────────────────────────

    override suspend fun setVoiceEnabled(enabled: Boolean) = writePreference(Keys.VOICE_ENABLED, enabled)
    override fun isVoiceEnabled(): Flow<Boolean> = readPreference(Keys.VOICE_ENABLED, true)

    override suspend fun setVoiceSpeed(speed: Float) = writePreference(Keys.VOICE_SPEED, speed)
    override fun getVoiceSpeed(): Flow<Float> = readPreference(Keys.VOICE_SPEED, 1.0f)

    override suspend fun setVoicePitch(pitch: Float) = writePreference(Keys.VOICE_PITCH, pitch)
    override fun getVoicePitch(): Flow<Float> = readPreference(Keys.VOICE_PITCH, 1.0f)

    override suspend fun setAutoRead(enabled: Boolean) = writePreference(Keys.AUTO_READ, enabled)
    override fun isAutoRead(): Flow<Boolean> = readPreference(Keys.AUTO_READ, false)

    // ─── Erişilebilirlik ──────────────────────────────────

    override suspend fun setTalkBackHints(enabled: Boolean) = writePreference(Keys.TALKBACK_HINTS, enabled)
    override fun isTalkBackHintsEnabled(): Flow<Boolean> = readPreference(Keys.TALKBACK_HINTS, true)

    override suspend fun setHapticFeedback(enabled: Boolean) = writePreference(Keys.HAPTIC_FEEDBACK, enabled)
    override fun isHapticFeedback(): Flow<Boolean> = readPreference(Keys.HAPTIC_FEEDBACK, true)

    override suspend fun setReduceMotion(enabled: Boolean) = writePreference(Keys.REDUCE_MOTION, enabled)
    override fun isReduceMotion(): Flow<Boolean> = readPreference(Keys.REDUCE_MOTION, false)

    override suspend fun setScreenReaderOptimization(enabled: Boolean) = writePreference(Keys.SCREEN_READER_OPT, enabled)
    override fun isScreenReaderOptimization(): Flow<Boolean> = readPreference(Keys.SCREEN_READER_OPT, true)

    override suspend fun setTouchTargetSize(size: String) = writePreference(Keys.TOUCH_TARGET_SIZE, size)
    override fun getTouchTargetSize(): Flow<String> = readPreference(Keys.TOUCH_TARGET_SIZE, "NORMAL")

    // ─── Hesap ────────────────────────────────────────────

    override suspend fun signOut(): Resource<Unit> {
        return try {
            firebaseAuth.signOut()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Çıkış yapılamadı")
        }
    }
}
