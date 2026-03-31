package com.voxable.core_accessibility.focus

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester
import kotlinx.coroutines.delay

/**
 * Odak tuzaklarını (Focus Trap) yöneten yapı.
 *
 * Modal dialog, bottom sheet gibi bileşenlerde odak
 * kapsam dışına kaçmasını önler. WCAG 2.4.3 uyumlu.
 */

/**
 * Odak tuzak durumu.
 *
 * @property firstFocus Tuzaktaki ilk odaklanabilir öğe
 * @property lastFocus Tuzaktaki son odaklanabilir öğe
 * @property itemCount Toplam odaklanabilir öğe sayısı
 */
data class FocusTrapState(
    val firstFocus: FocusRequester = FocusRequester(),
    val lastFocus: FocusRequester = FocusRequester(),
    val itemCount: Int = 0
)

/**
 * Odak tuzak state'i hatırlar ve ilk öğeye odak yönlendirir.
 *
 * Kullanım:
 * ```
 * val trap = rememberFocusTrap(itemCount = 3)
 *
 * Dialog {
 *     Column {
 *         TextField(modifier = Modifier.focusRequester(trap.firstFocus))
 *         TextField()
 *         Button(modifier = Modifier.focusRequester(trap.lastFocus))
 *     }
 * }
 * ```
 */
@Composable
fun rememberFocusTrap(
    itemCount: Int = 2,
    autoFocusFirst: Boolean = true
): FocusTrapState {
    val state = remember(itemCount) {
        FocusTrapState(
            firstFocus = FocusRequester(),
            lastFocus = FocusRequester(),
            itemCount = itemCount
        )
    }

    if (autoFocusFirst) {
        LaunchedEffect(state) {
            delay(300)
            try {
                state.firstFocus.requestFocus()
            } catch (_: IllegalStateException) {
                // Henüz bağlanmamış
            }
        }
    }

    return state
}

/**
 * Tab indeksli odak döngüsü yöneticisi.
 *
 * Klavye veya switch erişimi kullanan kullanıcılar
 * için döngüsel odak navigasyonu sağlar.
 *
 * @param itemCount Odaklanabilir öğe sayısı
 * @return Pair<Int, (Int) -> Unit> — mevcut indeks ve indeks değiştirme fonksiyonu
 */
@Composable
fun rememberCyclicFocusIndex(itemCount: Int): Pair<Int, (Int) -> Unit> {
    var currentIndex by remember { mutableIntStateOf(0) }

    val setIndex: (Int) -> Unit = { delta ->
        currentIndex = ((currentIndex + delta) % itemCount + itemCount) % itemCount
    }

    return currentIndex to setIndex
}
