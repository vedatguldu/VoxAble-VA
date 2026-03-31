package com.voxable.core_accessibility

import android.content.Context
import android.os.Build
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Cihazın erişilebilirlik durumunu merkezi olarak yöneten sınıf.
 *
 * Tüm erişilebilirlik servisleri (TalkBack, Switch Access, vb.) için
 * reaktif durum akışları sağlar. ViewModel'ler ve Composable'lar bu
 * sınıftan gelen Flow'ları toplayarak UI'ı otomatik günceller.
 *
 * Kullanım:
 * ```
 * @Inject lateinit var stateManager: AccessibilityStateManager
 *
 * // Compose içinde
 * val isTalkBack by stateManager.touchExplorationStateFlow
 *     .collectAsStateWithLifecycle(initialValue = false)
 * ```
 */
@Singleton
class AccessibilityStateManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val accessibilityManager =
        context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager

    // ─── Anlık Durum Sorguları ─────────────────────────────

    /** TalkBack (dokunarak keşfetme) aktif mi? */
    val isTalkBackEnabled: Boolean
        get() = accessibilityManager.isTouchExplorationEnabled

    /** Herhangi bir erişilebilirlik servisi aktif mi? */
    val isAccessibilityEnabled: Boolean
        get() = accessibilityManager.isEnabled

    /** Cihazda animasyonlar kapatılmış mı? (Reduce Motion) */
    val isReduceMotionEnabled: Boolean
        get() {
            val scale = Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1.0f
            )
            return scale == 0f
        }

    /** Font ölçeği (1.0 = normal, >1.0 = büyütülmüş) */
    val fontScale: Float
        get() = context.resources.configuration.fontScale

    /** Büyük font kullanılıyor mu? (1.3x ve üzeri) */
    val isLargeFontEnabled: Boolean
        get() = fontScale >= 1.3f

    /** Yüksek kontrast metin modu aktif mi? (API 31+) */
    val isHighTextContrastEnabled: Boolean
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            accessibilityManager.isHighTextContrastEnabled()
        } else {
            false
        }

    // ─── Reactive Durum Akışları ──────────────────────────

    /** Erişilebilirlik durumu değişiklik akışı */
    val accessibilityStateFlow: Flow<Boolean> = callbackFlow {
        val listener = AccessibilityManager.AccessibilityStateChangeListener { enabled ->
            trySend(enabled)
        }
        accessibilityManager.addAccessibilityStateChangeListener(listener)
        trySend(accessibilityManager.isEnabled)

        awaitClose {
            accessibilityManager.removeAccessibilityStateChangeListener(listener)
        }
    }.distinctUntilChanged()

    /** TalkBack (dokunarak keşfetme) durum akışı */
    val touchExplorationStateFlow: Flow<Boolean> = callbackFlow {
        val listener = AccessibilityManager.TouchExplorationStateChangeListener { enabled ->
            trySend(enabled)
        }
        accessibilityManager.addTouchExplorationStateChangeListener(listener)
        trySend(accessibilityManager.isTouchExplorationEnabled)

        awaitClose {
            accessibilityManager.removeTouchExplorationStateChangeListener(listener)
        }
    }.distinctUntilChanged()

    // ─── Kullanıcı Tercih State'leri ─────────────────────

    private val _userPreferVoiceGuidance = MutableStateFlow(true)
    val userPreferVoiceGuidance: StateFlow<Boolean> = _userPreferVoiceGuidance.asStateFlow()

    private val _userPreferHapticFeedback = MutableStateFlow(true)
    val userPreferHapticFeedback: StateFlow<Boolean> = _userPreferHapticFeedback.asStateFlow()

    private val _userPreferHighContrast = MutableStateFlow(false)
    val userPreferHighContrast: StateFlow<Boolean> = _userPreferHighContrast.asStateFlow()

    fun setVoiceGuidancePreference(enabled: Boolean) {
        _userPreferVoiceGuidance.value = enabled
    }

    fun setHapticFeedbackPreference(enabled: Boolean) {
        _userPreferHapticFeedback.value = enabled
    }

    fun setHighContrastPreference(enabled: Boolean) {
        _userPreferHighContrast.value = enabled
    }

    // ─── Birleşik Profil ─────────────────────────────────

    /**
     * Tüm erişilebilirlik durumlarını tek bir data class altında
     * birleştiren akış. Herhangi bir durum değiştiğinde yeni
     * [AccessibilityProfile] yayınlar.
     */
    val profileFlow: Flow<AccessibilityProfile> = combine(
        accessibilityStateFlow,
        touchExplorationStateFlow,
        _userPreferHighContrast
    ) { accessibilityEnabled, talkBackEnabled, highContrast ->
        AccessibilityProfile(
            isAccessibilityServiceEnabled = accessibilityEnabled,
            isTalkBackActive = talkBackEnabled,
            isReduceMotionEnabled = isReduceMotionEnabled,
            fontScale = fontScale,
            isHighContrastRequested = highContrast || isHighTextContrastEnabled
        )
    }.distinctUntilChanged()
}

/**
 * Tüm erişilebilirlik profilini temsil eden veri sınıfı.
 */
data class AccessibilityProfile(
    val isAccessibilityServiceEnabled: Boolean,
    val isTalkBackActive: Boolean,
    val isReduceMotionEnabled: Boolean,
    val fontScale: Float,
    val isHighContrastRequested: Boolean
) {
    /** Kullanıcı büyük yazı tipi mi kullanıyor? */
    val isLargeFont: Boolean get() = fontScale >= 1.3f

    /** Animasyonlar gösterilmeli mi? */
    val shouldShowAnimations: Boolean get() = !isReduceMotionEnabled

    /** TalkBack ipuçları gösterilmeli mi? */
    val shouldProvideHints: Boolean get() = isTalkBackActive
}
