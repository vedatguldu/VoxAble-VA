package com.voxable.core_accessibility.semantics

import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.semantics.traversalIndex

// ────────────────────────────────────────────────────────────
// Semantik Modifier Uzantıları
//
// TalkBack, Switch Access ve diğer erişilebilirlik
// servisleri ile uyumlu role/state/content tanımlayıcıları.
// ────────────────────────────────────────────────────────────

/**
 * Buton rolü ile erisilebilir semantik tanımlar.
 *
 * ```
 * Box(modifier = Modifier.accessibleButton("Fotoğraf çek"))
 * ```
 */
fun Modifier.accessibleButton(
    label: String,
    stateDesc: String? = null,
    isEnabled: Boolean = true
): Modifier = this.semantics(mergeDescendants = true) {
    contentDescription = label
    role = Role.Button
    if (stateDesc != null) {
        stateDescription = stateDesc
    }
    if (!isEnabled) {
        disabled()
    }
}

/**
 * Başlık (heading) semantik modifier.
 * TalkBack kullanıcıları başlıklar arasında hızlıca
 * gezinmek için heading navigasyonunu kullanabilir.
 *
 * ```
 * Text("Ayarlar", modifier = Modifier.accessibleHeading("Ayarlar bölümü"))
 * ```
 */
fun Modifier.accessibleHeading(
    label: String
): Modifier = this.semantics {
    contentDescription = label
    heading()
}

/**
 * Canlı bölge (live region) modifier.
 * İçerik değiştiğinde TalkBack otomatik duyuru yapar.
 *
 * @param polite true: mevcut konuşmayı bölmez, false: hemen duyurur
 *
 * ```
 * Text(statusText, modifier = Modifier.accessibleLiveRegion(statusText))
 * ```
 */
fun Modifier.accessibleLiveRegion(
    label: String,
    polite: Boolean = true
): Modifier = this.semantics {
    contentDescription = label
    liveRegion = if (polite) LiveRegionMode.Polite else LiveRegionMode.Assertive
}

/**
 * Sayfa/pane tanımlayıcı. Ekran geçişlerinde TalkBack
 * "[pane title] görüntüleniyor" duyurusu yapar.
 *
 * ```
 * Column(modifier = Modifier.accessiblePane("Ana Sayfa"))
 * ```
 */
fun Modifier.accessiblePane(
    title: String
): Modifier = this.semantics {
    paneTitle = title
}

/**
 * Görsel içerik (image) tanımlayıcı.
 *
 * ```
 * Image(painter, modifier = Modifier.accessibleImage("Profil fotoğrafı"))
 * ```
 */
fun Modifier.accessibleImage(
    description: String
): Modifier = this.semantics {
    contentDescription = description
    role = Role.Image
}

/**
 * Dekoratif öğeyi erişilebilirlik ağacından gizler.
 * TalkBack bu öğeyi görmezden gelir.
 *
 * ```
 * Divider(modifier = Modifier.accessibilityHidden())
 * ```
 */
fun Modifier.accessibilityHidden(): Modifier = this.clearAndSetSemantics { }

/**
 * Seçilebilir öğe semantikleri (checkbox, switch vb.).
 *
 * @param label Öğe açıklaması
 * @param selected Seçili durumu
 * @param role Seçim türü (Checkbox, Switch, RadioButton)
 *
 * ```
 * Row(modifier = Modifier.accessibleToggle("Karanlık mod", isDark, Role.Switch))
 * ```
 */
fun Modifier.accessibleToggle(
    label: String,
    selected: Boolean,
    role: Role = Role.Switch
): Modifier = this.semantics(mergeDescendants = true) {
    this.contentDescription = label
    this.role = role
    this.stateDescription = if (selected) "Açık" else "Kapalı"
}

/**
 * Liste öğesi pozisyon semantikler.i
 * TalkBack "X / Y öğe" duyurusu yapar.
 *
 * ```
 * Card(modifier = Modifier.accessibleListItem("Para birimi: USD", index = 2, total = 10))
 * ```
 */
fun Modifier.accessibleListItem(
    label: String,
    index: Int,
    total: Int
): Modifier = this.semantics(mergeDescendants = true) {
    contentDescription = "$label, ${index + 1} / $total öğe"
}

/**
 * Tab/BottomNav öğesi semantikleri.
 *
 * ```
 * Tab(modifier = Modifier.accessibleTab("Ana Sayfa", isSelected = true, index = 0, total = 4))
 * ```
 */
fun Modifier.accessibleTab(
    label: String,
    isSelected: Boolean,
    index: Int,
    total: Int
): Modifier = this.semantics(mergeDescendants = true) {
    contentDescription = "$label sekmesi, ${index + 1} / $total"
    role = Role.Tab
    stateDescription = if (isSelected) "Seçili" else "Seçili değil"
}

/**
 * Traversal (gezinme) sırasını belirler.
 * Düşük değer = önce okunur.
 *
 * ```
 * Text("Başlık", modifier = Modifier.accessibleTraversalOrder(0f))
 * Text("Alt başlık", modifier = Modifier.accessibleTraversalOrder(1f))
 * ```
 */
fun Modifier.accessibleTraversalOrder(
    index: Float
): Modifier = this.semantics {
    traversalIndex = index
}

/**
 * Test etiketi ile birleşik semantik.
 * UI testlerinde hem test tag hem accessibility label ayarlar.
 *
 * ```
 * Button(modifier = Modifier.accessibleWithTestTag("Giriş Yap", "login_button"))
 * ```
 */
fun Modifier.accessibleWithTestTag(
    label: String,
    tag: String
): Modifier = this.semantics {
    contentDescription = label
    testTag = tag
}
