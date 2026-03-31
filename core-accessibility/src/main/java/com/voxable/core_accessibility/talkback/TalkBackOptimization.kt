package com.voxable.core_accessibility.talkback

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.voxable.core_accessibility.AccessibilityStateManager
import com.voxable.core_accessibility.TalkBackUtils

// ────────────────────────────────────────────────────────────
// TalkBack Optimizasyon Katmanı
//
// Compose tarafında TalkBack ile uyumlu çalışmak için
// yüksek seviyeli Composable'lar ve Modifier’lar.
// ────────────────────────────────────────────────────────────

/**
 * Ekran geçişlerinde TalkBack duyurusu yapan Composable.
 *
 * Her ekranın kökünde kullanılmalıdır:
 * ```
 * @Composable
 * fun HomeScreen() {
 *     ScreenAnnouncement("Ana Sayfa", talkBackUtils)
 *     // ... ekran içeriği
 * }
 * ```
 */
@Composable
fun ScreenAnnouncement(
    screenName: String,
    talkBackUtils: TalkBackUtils
) {
    LaunchedEffect(screenName) {
        talkBackUtils.announceScreenChange(screenName)
    }
}

/**
 * Koşula bağlı TalkBack duyurusu.
 *
 * Belirli bir durum değiştiğinde (hata, başarı vb.)
 * otomatik duyuru yapar.
 *
 * ```
 * DynamicAnnouncement(
 *     message = errorMessage,
 *     condition = errorMessage != null,
 *     talkBackUtils = talkBackUtils
 * )
 * ```
 */
@Composable
fun DynamicAnnouncement(
    message: String?,
    condition: Boolean = message != null,
    talkBackUtils: TalkBackUtils
) {
    LaunchedEffect(message, condition) {
        if (condition && !message.isNullOrBlank()) {
            talkBackUtils.announce(message)
        }
    }
}

/**
 * TalkBack aktifken koşullu içerik gösterme.
 *
 * TalkBack kullanıcıları için ek açıklama metin veya
 * yardım ipuçları göstermek için:
 *
 * ```
 * TalkBackConditional(stateManager) {
 *     Text("IPUÇU: Çift tıklayın")
 * }
 * ```
 */
@Composable
fun TalkBackConditional(
    stateManager: AccessibilityStateManager,
    content: @Composable () -> Unit
) {
    val isTalkBack by stateManager.touchExplorationStateFlow
        .collectAsStateWithLifecycle(initialValue = false)

    if (isTalkBack) {
        content()
    }
}

/**
 * Ekran değişikliği için pane semantik modifier.
 * Navigation geçişlerinde TalkBack'in yeni sayfayı duyurmasını sağlar.
 */
fun Modifier.talkBackScreen(screenTitle: String): Modifier =
    this.semantics {
        paneTitle = screenTitle
    }

/**
 * Dinamik içerik bölgesi modifier.
 * İçerik değiştiğinde TalkBack otomatik duyuru yapar.
 *
 * @param currentValue Duyurulacak mevcut değer
 * @param assertive true: hemen duyur, false: sırayı bekle
 */
fun Modifier.talkBackDynamicContent(
    currentValue: String,
    assertive: Boolean = false
): Modifier = this.semantics {
    contentDescription = currentValue
    liveRegion = if (assertive) LiveRegionMode.Assertive else LiveRegionMode.Polite
}
