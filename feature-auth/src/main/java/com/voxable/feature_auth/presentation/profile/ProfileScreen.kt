package com.voxable.feature_auth.presentation.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.voxable.core_ui.components.LoadingIndicator
import com.voxable.core_ui.components.VoxAbleButton
import com.voxable.core_ui.components.VoxAbleTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onSignedOut: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showSignOutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ProfileEvent.SignedOut,
                is ProfileEvent.AccountDeleted -> onSignedOut()
                is ProfileEvent.ShowError -> snackbarHostState.showSnackbar(event.message)
                is ProfileEvent.ShowSuccess -> snackbarHostState.showSnackbar(event.message)
                is ProfileEvent.ProfileUpdated,
                is ProfileEvent.PreferencesSaved -> { /* Snackbar ShowSuccess ile zaten gösteriliyor */ }
            }
        }
    }

    // Hesap silme onay dialogu
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Hesabı Sil") },
            text = {
                Text("Hesabınız ve tüm verileriniz kalıcı olarak silinecektir. Bu işlem geri alınamaz.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.onDeleteAccount()
                    }
                ) {
                    Text(
                        "Sil",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }

    // Çıkış onay dialogu
    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text("Çıkış Yap") },
            text = { Text("Hesabınızdan çıkış yapmak istediğinize emin misiniz?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSignOutDialog = false
                        viewModel.onSignOut()
                    }
                ) {
                    Text("Çıkış Yap")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Profil",
                        modifier = Modifier.semantics { heading() }
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.semantics {
                            contentDescription = "Geri dön"
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    if (state.isEditing) {
                        IconButton(
                            onClick = viewModel::onSaveProfile,
                            modifier = Modifier.semantics {
                                contentDescription = "Profili kaydet"
                            }
                        ) {
                            Icon(Icons.Filled.Save, contentDescription = null)
                        }
                    } else {
                        IconButton(
                            onClick = viewModel::toggleEditing,
                            modifier = Modifier.semantics {
                                contentDescription = "Profili düzenle"
                            }
                        ) {
                            Icon(Icons.Filled.Edit, contentDescription = null)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        if (state.isLoading) {
            LoadingIndicator(accessibilityLabel = "Profil yükleniyor, lütfen bekleyin")
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Profil avatarı
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .semantics {
                            contentDescription = "Profil fotoğrafı"
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = state.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.semantics {
                        contentDescription = "E-posta: ${state.email}"
                    }
                )

                Text(
                    text = when (state.authProvider) {
                        com.voxable.feature_auth.domain.model.AuthProvider.GOOGLE -> "Google hesabı ile giriş yapıldı"
                        com.voxable.feature_auth.domain.model.AuthProvider.EMAIL -> "E-posta ile giriş yapıldı"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                // ─── Profil bilgileri ───────────────────────────────────
                ProfileSectionHeader("Kişisel Bilgiler")

                Spacer(modifier = Modifier.height(8.dp))

                VoxAbleTextField(
                    value = state.displayName,
                    onValueChange = viewModel::onDisplayNameChange,
                    label = "Ad Soyad",
                    errorMessage = state.nameError,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    accessibilityLabel = "Ad Soyad giriş alanı",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                VoxAbleTextField(
                    value = state.phoneNumber,
                    onValueChange = viewModel::onPhoneNumberChange,
                    label = "Telefon Numarası",
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Done
                    ),
                    accessibilityLabel = "Telefon numarası giriş alanı",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (state.isEditing && state.isSaving) {
                    LoadingIndicator(accessibilityLabel = "Profil kaydediliyor")
                }

                if (state.isEditing) {
                    VoxAbleButton(
                        text = "Kaydet",
                        onClick = viewModel::onSaveProfile,
                        enabled = !state.isSaving,
                        accessibilityLabel = "Profil değişikliklerini kaydet"
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // ─── Erişilebilirlik Tercihleri ─────────────────────────
                ProfileSectionHeader("Erişilebilirlik Ayarları")

                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        AccessibilitySwitch(
                            label = "Karanlık Mod",
                            checked = state.preferences.darkMode,
                            onCheckedChange = viewModel::onDarkModeChange,
                            description = "Koyu tema etkinleştir"
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                        AccessibilitySwitch(
                            label = "Yüksek Kontrast",
                            checked = state.preferences.highContrast,
                            onCheckedChange = viewModel::onHighContrastChange,
                            description = "Metin ve arka plan kontrastını artır"
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                        AccessibilitySwitch(
                            label = "TalkBack İpuçları",
                            checked = state.preferences.talkBackHints,
                            onCheckedChange = viewModel::onTalkBackHintsChange,
                            description = "Ekran okuyucu için ek ipuçlarını göster"
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                        AccessibilitySwitch(
                            label = "Hareketi Azalt",
                            checked = state.preferences.reduceMotion,
                            onCheckedChange = viewModel::onReduceMotionChange,
                            description = "Animasyonları azalt veya kaldır"
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                        AccessibilitySwitch(
                            label = "Dokunsal Geri Bildirim",
                            checked = state.preferences.hapticFeedback,
                            onCheckedChange = viewModel::onHapticFeedbackChange,
                            description = "Titreşimli geri bildirim"
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                        // Yazı tipi boyutu slider
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics(mergeDescendants = true) {
                                    contentDescription =
                                        "Yazı tipi boyutu: yüzde ${(state.preferences.fontSize * 100).toInt()}"
                                }
                        ) {
                            Text(
                                text = "Yazı Tipi Boyutu: %${(state.preferences.fontSize * 100).toInt()}",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Slider(
                                value = state.preferences.fontSize,
                                onValueChange = viewModel::onFontSizeChange,
                                valueRange = 0.8f..2.0f,
                                steps = 5,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ─── Hesap İşlemleri ────────────────────────────────────
                ProfileSectionHeader("Hesap")

                Spacer(modifier = Modifier.height(8.dp))

                VoxAbleButton(
                    text = "Çıkış Yap",
                    onClick = { showSignOutDialog = true },
                    accessibilityLabel = "Hesaptan çıkış yap"
                )

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics {
                            contentDescription = "Hesabı sil. Bu işlem geri alınamaz."
                        }
                ) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Hesabı Sil",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun ProfileSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .semantics { heading() }
    )
}

@Composable
private fun AccessibilitySwitch(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                contentDescription = "$label: ${if (checked) "açık" else "kapalı"}. $description"
                stateDescription = if (checked) "açık" else "kapalı"
            },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
