package com.voxable.core_accessibility.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp

/**
 * Erişilebilirlik en iyi pratiklerini gösteren örnek ekran.
 *
 * Bu dosya üretim kodu değildir — geliştiricilere
 * contentDescription, semantics, heading, liveRegion,
 * mergeDescendants kullanımını gösterir.
 *
 * Preview ve eğitim amaçlıdır.
 */

// ───────────── ÖRNEK 1: Erisilebilir Form ────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccessibleFormExample() {
    var email by remember { mutableStateOf("") }
    var hasError by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Giriş",
                        // BAŞLIK: TalkBack heading navigasyonu
                        modifier = Modifier.semantics { heading() }
                    )
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                // PANE: Ekran değişikliği duyurusu
                .semantics { liveRegion = LiveRegionMode.Polite },
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // HATA DUYURUSU: Assertive live region
            if (hasError) {
                AccessibleErrorBanner(
                    message = "E-posta adresi geçersiz format",
                    isVisible = true
                )
            }

            // BUTON: Minimum 56dp yükseklik + açıklayıcı label
            Button(
                onClick = { hasError = email.isEmpty() },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .semantics {
                        contentDescription = "Giriş yap butonu"
                        role = Role.Button
                    }
            ) {
                Text("Giriş Yap")
            }
        }
    }
}

// ───────────── ÖRNEK 2: Birleştirilmiş Semantik ────────────

@Composable
fun MergedSemanticsExample() {
    // MERGE: Çoklu öğeyi tek TalkBack duyurusunda birleştir
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clearAndSetSemantics {
                contentDescription = "Profil kartı: Ali Veli, Geliştirici, 4.5 yıldız"
            }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Ali Veli", style = MaterialTheme.typography.titleMedium)
            Text("Geliştirici", style = MaterialTheme.typography.bodyMedium)
            Text("★★★★☆ 4.5", style = MaterialTheme.typography.bodySmall)
        }
    }
}

// ───────────── ÖRNEK 3: Toggle / Switch ───────────────────

@Composable
fun AccessibleToggleExample() {
    var isDarkMode by remember { mutableStateOf(false) }

    // TOGGLE: Rol, durum açıklaması ve label birlikte
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .padding(horizontal = 16.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = "Karanlık mod"
                role = Role.Switch
                stateDescription = if (isDarkMode) "Açık" else "Kapalı"
            }
    ) {
        Text(
            "Karanlık Mod",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = isDarkMode,
            onCheckedChange = { isDarkMode = it }
        )
    }
}

// ───────────── ÖRNEK 4: İlerleme + Live Region ─────────────

@Composable
fun AccessibleProgressExample() {
    var progress by remember { mutableFloatStateOf(0.65f) }
    val percent = (progress * 100).toInt()

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp)
    ) {
        // BAŞLIK
        Text(
            text = "İndirme",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.semantics { heading() }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // İLERLEME: Live region ile durumu duyur
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .semantics {
                    contentDescription = "İndirme ilerlemesi: yüzde $percent"
                    liveRegion = LiveRegionMode.Polite
                }
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "%$percent",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

// ───────────── ÖRNEK 5: IconButton + contentDescription ───

@Composable
fun AccessibleIconButtonExample() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(16.dp)
    ) {
        // DOĞRU: Her IconButton için açıklayıcı contentDescription
        IconButton(
            onClick = { },
            modifier = Modifier.heightIn(min = 48.dp)
        ) {
            Icon(
                Icons.Filled.PlayArrow,
                contentDescription = "Sesi oynat" // ✅ Açıklayıcı
            )
        }

        IconButton(
            onClick = { },
            modifier = Modifier.heightIn(min = 48.dp)
        ) {
            Icon(
                Icons.Filled.CheckCircle,
                contentDescription = "Onayla" // ✅ Açıklayıcı
            )
        }

        IconButton(
            onClick = { },
            modifier = Modifier.heightIn(min = 48.dp)
        ) {
            Icon(
                Icons.Filled.Error,
                contentDescription = null // ✅ Dekoratif — null kabul edilir
            )
        }
    }
}

// ───────────── ÖRNEK 6: Liste Öğesi Pozisyon ────────────────

@Composable
fun AccessibleListItemExample() {
    val items = listOf("Türk Lirası", "Amerikan Doları", "Euro")

    Column {
        items.forEachIndexed { index, item ->
            // LİSTE KONUMU: "X / Y öğe" duyurusu
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .semantics(mergeDescendants = true) {
                        contentDescription = "$item, ${index + 1} / ${items.size} öğe"
                    }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(item, modifier = Modifier.weight(1f))
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = null // Merge ile kapsüllendi
                    )
                }
            }
        }
    }
}
