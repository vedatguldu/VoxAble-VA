package com.voxable.core_accessibility.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import kotlinx.coroutines.delay

// ────────────────────────────────────────────────────────────
// Dinamik Erişilebilirlik Duyuruları
//
// Compose state değişikliklerine tepki veren,
// TalkBack uyumlu duyuru mekanizmaları.
// ────────────────────────────────────────────────────────────

/**
 * State değişikliğine tepkisel duyuru modifier.
 *
 * @param currentAnnouncement Duyurulacak mevcut metin.
 *        Her değiştiğinde TalkBack'e bildirilir.
 * @param debounceMs Art arda gelen değişiklikleri
 *        birleştirmek için bekleme süresi.
 *
 * ```
 * Text(
 *     text = statusMessage,
 *     modifier = Modifier.announceOnChange(statusMessage)
 * )
 * ```
 */
fun Modifier.announceOnChange(
    currentAnnouncement: String,
    assertive: Boolean = false
): Modifier = this.semantics {
    contentDescription = currentAnnouncement
    liveRegion = if (assertive) LiveRegionMode.Assertive else LiveRegionMode.Polite
}

/**
 * Geciktirilmiş duyuru Composable.
 *
 * Belirli bir süre sonra otomatik duyuru yapar.
 * Form doğrulama sonuçları, async işlem sonuçları vb. için.
 *
 * @param message Duyurulacak mesaj
 * @param delayMs Duyuru öncesi bekleme
 * @param onAnnounced Duyuru yapıldıktan sonra callback
 *
 * ```
 * if (showValidation) {
 *     DelayedAnnouncement("Form hatalı, 2 alan düzeltilmeli")
 * }
 * ```
 */
@Composable
fun DelayedAnnouncement(
    message: String,
    delayMs: Long = 500L,
    onAnnounced: (() -> Unit)? = null
) {
    var announced by remember(message) { mutableStateOf(false) }

    LaunchedEffect(message) {
        delay(delayMs)
        announced = true
        onAnnounced?.invoke()
    }

    if (announced) {
        // Görünmez ama TalkBack tarafından okunur
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.semantics {
                contentDescription = message
                liveRegion = LiveRegionMode.Assertive
            }
        )
    }
}

/**
 * Sayaç tabanlı duyuru.
 *
 * Sayısal değerlerin değişimini periyodik olarak duyurur.
 * İndirme yüzdesi, sayaç vb. için.
 *
 * @param value Mevcut sayısal değer
 * @param formatLabel Değeri okunabilir stringe çeviren fonksiyon
 * @param announceInterval Kaç değer değişikliğinde bir duyuru yapılacağı
 *
 * ```
 * CounterAnnouncement(
 *     value = downloadPercent,
 *     formatLabel = { "Yüzde $it tamamlandı" },
 *     announceInterval = 10
 * )
 * ```
 */
@Composable
fun CounterAnnouncement(
    value: Int,
    formatLabel: (Int) -> String,
    announceInterval: Int = 10
) {
    val shouldAnnounce = value % announceInterval == 0 && value > 0

    if (shouldAnnounce) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.semantics {
                contentDescription = formatLabel(value)
                liveRegion = LiveRegionMode.Polite
            }
        )
    }
}
