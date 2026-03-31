package com.voxable.core_accessibility.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

// ────────────────────────────────────────────────────────────
// Compose Erişilebilirlik Bileşen Örnekleri
//
// Üretim kalitesinde, TalkBack-uyumlu Compose bileşenleri.
// Uygulamaların bu örüntüleri kendi ekranlarında takip etmesi
// önerilir.
// ────────────────────────────────────────────────────────────

/**
 * Erişilebilir hata banner'ı.
 *
 * - Hata görünür olduğunda TalkBack assertive duyuru yapar
 * - Odağı otomatik olarak hataya yönlendirir
 * - Animasyonlu giriş/çıkış
 *
 * ```
 * AccessibleErrorBanner(
 *     message = state.errorMessage,
 *     isVisible = state.hasError
 * )
 * ```
 */
@Composable
fun AccessibleErrorBanner(
    message: String?,
    isVisible: Boolean,
    modifier: Modifier = Modifier,
    onDismiss: (() -> Unit)? = null,
    autoDismissMs: Long = 0L
) {
    val focusRequester = remember { FocusRequester() }

    if (autoDismissMs > 0 && isVisible && onDismiss != null) {
        LaunchedEffect(message) {
            delay(autoDismissMs)
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = isVisible && message != null,
        enter = fadeIn(tween(200)),
        exit = fadeOut(tween(200))
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            ),
            modifier = modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .semantics {
                    contentDescription = "Hata: ${message.orEmpty()}"
                    liveRegion = LiveRegionMode.Assertive
                    if (message != null) {
                        error(message)
                    }
                }
        ) {
            Text(
                text = message.orEmpty(),
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(16.dp)
            )
        }

        LaunchedEffect(message) {
            delay(300)
            try {
                focusRequester.requestFocus()
            } catch (_: IllegalStateException) { }
        }
    }
}

/**
 * Erişilebilir başarı mesajı bileşeni.
 *
 * - Live region ile otomatik duyuru
 * - Geçici gösterim (auto-dismiss)
 *
 * ```
 * AccessibleSuccessMessage(
 *     message = "Dosya başarıyla kaydedildi",
 *     isVisible = showSuccess
 * )
 * ```
 */
@Composable
fun AccessibleSuccessMessage(
    message: String,
    isVisible: Boolean,
    modifier: Modifier = Modifier,
    autoDismissMs: Long = 3000L,
    onDismiss: (() -> Unit)? = null
) {
    var show by remember(message) { mutableStateOf(isVisible) }

    if (autoDismissMs > 0 && show) {
        LaunchedEffect(message) {
            delay(autoDismissMs)
            show = false
            onDismiss?.invoke()
        }
    }

    AnimatedVisibility(
        visible = show,
        enter = fadeIn(tween(200)),
        exit = fadeOut(tween(200))
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            modifier = modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription = "Başarılı: $message"
                    liveRegion = LiveRegionMode.Polite
                }
        ) {
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

/**
 * Erişilebilir bölüm başlığı.
 *
 * TalkBack heading navigasyonunu destekler.
 * Kullanıcılar başlık jestleri ile bölümler arasında hızlıca gezinebilir.
 *
 * ```
 * AccessibleSectionHeader("Ayarlar")
 * ```
 */
@Composable
fun AccessibleSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clearAndSetSemantics {
                contentDescription = if (subtitle != null) "$title, $subtitle" else title
                heading()
            }
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (subtitle != null) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Erişilebilir ilerleme göstergesi.
 *
 * - Yüzde değeri TalkBack’e duyurulur
 * - Live region ile dinamik güncelleme
 *
 * ```
 * AccessibleProgress(percent = 65, label = "İndirme")
 * ```
 */
@Composable
fun AccessibleProgress(
    percent: Int,
    modifier: Modifier = Modifier,
    label: String = "İşlem"
) {
    Box(
        modifier = modifier.semantics {
            contentDescription = "$label: yüzde $percent tamamlandı"
            liveRegion = LiveRegionMode.Polite
        }
    ) {
        // Alt bileşenler tarafından doldurulur
    }
}
