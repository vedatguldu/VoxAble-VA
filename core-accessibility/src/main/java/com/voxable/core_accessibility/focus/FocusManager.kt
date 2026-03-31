package com.voxable.core_accessibility.focus

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import kotlinx.coroutines.delay

/**
 * Odak (Focus) yönetim sistemi için Compose yardımcıları.
 *
 * TalkBack kullanıcıları için ekran geçişlerinde, içerik
 * değişikliklerinde ve hata durumlarında doğru bileşene
 * otomatik odak yönlendirmesi sağlar.
 */

/**
 * Hatırlanan ve otomatik isteğe bağlı FocusRequester oluşturur.
 *
 * @param requestOnLaunch true ise bileşen ilk compose edildiğinde odağı alır
 * @param delayMs Odak isteği öncesi bekleme (ms). Compose layout'un
 *               tamamlanması için kısa gecikme önerilir.
 * @return FocusRequester — bileşene [Modifier.focusRequester] ile bağlanmalıdır
 *
 * Kullanım:
 * ```
 * val focusRequester = rememberAccessibilityFocusRequester(requestOnLaunch = true)
 * Text(
 *     text = "Başlık",
 *     modifier = Modifier.focusRequester(focusRequester)
 * )
 * ```
 */
@Composable
fun rememberAccessibilityFocusRequester(
    requestOnLaunch: Boolean = false,
    delayMs: Long = 300L
): FocusRequester {
    val focusRequester = remember { FocusRequester() }
    if (requestOnLaunch) {
        LaunchedEffect(Unit) {
            delay(delayMs)
            try {
                focusRequester.requestFocus()
            } catch (_: IllegalStateException) {
                // Bileşen henüz compose edilmemişse sessizce atla
            }
        }
    }
    return focusRequester
}

/**
 * Koşula bağlı odak yönlendirmesi.
 * [condition] true olduğunda belirtilen FocusRequester'a odak yönlendirir.
 *
 * Kullanım:
 * ```
 * val errorFocus = remember { FocusRequester() }
 * ConditionalFocusEffect(condition = hasError, focusRequester = errorFocus)
 * ```
 */
@Composable
fun ConditionalFocusEffect(
    condition: Boolean,
    focusRequester: FocusRequester,
    delayMs: Long = 200L
) {
    LaunchedEffect(condition) {
        if (condition) {
            delay(delayMs)
            try {
                focusRequester.requestFocus()
            } catch (_: IllegalStateException) {
                // Atla
            }
        }
    }
}

/**
 * Sıralı odak zinciri oluşturur.
 * Form alanlarında TAB/swipe sırasını kontrol etmek için.
 *
 * @param count Alan sayısı
 * @return FocusRequester listesi
 *
 * Kullanım:
 * ```
 * val focusChain = rememberFocusChain(count = 3)
 * VoxAbleTextField(modifier = Modifier.focusRequester(focusChain[0]))
 * VoxAbleTextField(modifier = Modifier.focusRequester(focusChain[1]))
 * VoxAbleButton(modifier = Modifier.focusRequester(focusChain[2]))
 * ```
 */
@Composable
fun rememberFocusChain(count: Int): List<FocusRequester> {
    return remember(count) {
        List(count) { FocusRequester() }
    }
}

/**
 * Bu Modifier, bileşene FocusRequester bağlar ve isteğe bağlı
 * olarak ilk composition'da odağı alır.
 */
fun Modifier.accessibilityFocus(
    focusRequester: FocusRequester
): Modifier = this.focusRequester(focusRequester)
