package com.voxable.core_accessibility.talkback

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.voxable.core_accessibility.AccessibilityProfile
import com.voxable.core_accessibility.AccessibilityStateManager
import com.voxable.core_accessibility.VoiceFeedbackManager

// ────────────────────────────────────────────────────────────
// Erisilebilirlik Bilincine Sahip Konteynırlar
//
// Erişilebilirlik profiline göre davranış değiştiren
// wrapper Composable'lar.
// ────────────────────────────────────────────────────────────

/**
 * Erişilebilirlik profiline göre alt bileşenlere
 * profil bilgisi ileten Composable.
 *
 * Kullanım:
 * ```
 * AccessibilityAware(stateManager) { profile ->
 *     if (profile.isTalkBackActive) {
 *         Text("TalkBack aktif — ekranı keşfetmek için parmak gezdirin")
 *     }
 *     if (!profile.shouldShowAnimations) {
 *         // Animasyonsuz içerik göster
 *     }
 * }
 * ```
 */
@Composable
fun AccessibilityAware(
    stateManager: AccessibilityStateManager,
    content: @Composable (profile: AccessibilityProfile) -> Unit
) {
    val profile by stateManager.profileFlow.collectAsStateWithLifecycle(
        initialValue = AccessibilityProfile(
            isAccessibilityServiceEnabled = stateManager.isAccessibilityEnabled,
            isTalkBackActive = stateManager.isTalkBackEnabled,
            isReduceMotionEnabled = stateManager.isReduceMotionEnabled,
            fontScale = stateManager.fontScale,
            isHighContrastRequested = false
        )
    )

    content(profile)
}

/**
 * TalkBack kullanıcıları için birden fazla öğeyi tek duyuruda
 * birleştiren konteynır.
 *
 * TalkBack'in her metin/ikon'ü ayrı ayrı duyurması yerine,
 * tüm alt öğeleri tek konuşma ile okur.
 *
 * ```
 * MergedAccessibilityContainer(
 *     mergedDescription = "Ali Veli, 5 yıldız, 12 yorum"
 * ) {
 *     Text("Ali Veli")
 *     RatingBar(5)
 *     Text("12 yorum")
 * }
 * ```
 */
@Composable
fun MergedAccessibilityContainer(
    mergedDescription: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier.clearAndSetSemantics {
            contentDescription = mergedDescription
        }
    ) {
        content()
    }
}

/**
 * Dekoratif içeriği erişilebilirlik ağacından gizler.
 * TalkBack bu konteynırı ve alt öğelerini görmezden gelir.
 *
 * ```
 * AccessibilityHidden {
 *     Image(decorativeBg)
 *     Spacer(Modifier.height(4.dp))
 * }
 * ```
 */
@Composable
fun AccessibilityHidden(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier.clearAndSetSemantics { }) {
        content()
    }
}

/**
 * TTS yaşam döngüsü yönetimi yapan Composable.
 * Bir ekranın kökünde kullanıldığında, ekran
 * composition'dan çıktığında konuşmayı durdurur.
 *
 * ```
 * ManageTtsLifecycle(voiceFeedbackManager)
 * ```
 */
@Composable
fun ManageTtsLifecycle(
    voiceFeedbackManager: VoiceFeedbackManager
) {
    DisposableEffect(Unit) {
        onDispose {
            voiceFeedbackManager.stopSpeaking()
        }
    }
}
