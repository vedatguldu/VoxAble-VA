package com.voxable.feature_auth.presentation.login

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.voxable.core_ui.components.LoadingIndicator
import com.voxable.core_ui.components.VoxAbleButton
import com.voxable.core_ui.components.VoxAbleTextField
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Google Sign-In Credential Manager
    val credentialManager = remember { CredentialManager.create(context) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is LoginEvent.LoginSuccess -> onLoginSuccess()
                is LoginEvent.ShowError -> snackbarHostState.showSnackbar(event.message)
                is LoginEvent.ShowSuccess -> snackbarHostState.showSnackbar(event.message)
                is LoginEvent.LaunchGoogleSignIn -> {
                    scope.launch {
                        try {
                            val googleIdOption = GetGoogleIdOption.Builder()
                                .setFilterByAuthorizedAccounts(false)
                                .setServerClientId(
                                    context.getString(
                                        context.resources.getIdentifier(
                                            "default_web_client_id",
                                            "string",
                                            context.packageName
                                        )
                                    )
                                )
                                .build()

                            val request = GetCredentialRequest.Builder()
                                .addCredentialOption(googleIdOption)
                                .build()

                            val result = credentialManager.getCredential(
                                request = request,
                                context = context
                            )

                            val googleIdTokenCredential = GoogleIdTokenCredential
                                .createFrom(result.credential.data)

                            viewModel.onGoogleSignInResult(googleIdTokenCredential.idToken)
                        } catch (e: GetCredentialCancellationException) {
                            // Kullanıcı iptal etti
                        } catch (e: Exception) {
                            viewModel.onGoogleSignInError(
                                e.message ?: "Google ile giriş başarısız"
                            )
                        }
                    }
                }
            }
        }
    }

    // Şifre sıfırlama dialogu
    if (state.showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = viewModel::onForgotPasswordDismiss,
            title = { Text("Şifre Sıfırlama") },
            text = {
                Column {
                    if (state.forgotPasswordSent) {
                        Text(
                            "Şifre sıfırlama bağlantısı e-postanıza gönderildi.",
                            modifier = Modifier.semantics {
                                contentDescription =
                                    "Şifre sıfırlama bağlantısı e-postanıza gönderildi"
                            }
                        )
                    } else {
                        Text("E-posta adresinize bir şifre sıfırlama bağlantısı göndereceğiz.")
                        Spacer(modifier = Modifier.height(12.dp))
                        VoxAbleTextField(
                            value = state.forgotPasswordEmail,
                            onValueChange = viewModel::onForgotPasswordEmailChange,
                            label = "E-posta",
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Done
                            ),
                            accessibilityLabel = "Şifre sıfırlama e-posta adresi"
                        )
                    }
                }
            },
            confirmButton = {
                if (state.forgotPasswordSent) {
                    TextButton(onClick = viewModel::onForgotPasswordDismiss) {
                        Text("Tamam")
                    }
                } else {
                    TextButton(onClick = viewModel::onSendPasswordReset) {
                        Text("Gönder")
                    }
                }
            },
            dismissButton = {
                if (!state.forgotPasswordSent) {
                    TextButton(onClick = viewModel::onForgotPasswordDismiss) {
                        Text("İptal")
                    }
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (state.isLoading && !state.showForgotPasswordDialog) {
            LoadingIndicator(accessibilityLabel = "Giriş yapılıyor, lütfen bekleyin")
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "VoxAble",
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.semantics { heading() }
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Hesabınıza giriş yapın",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(32.dp))

                VoxAbleTextField(
                    value = state.email,
                    onValueChange = viewModel::onEmailChange,
                    label = "E-posta",
                    errorMessage = state.emailError,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    accessibilityLabel = "E-posta adresi giriş alanı"
                )

                Spacer(modifier = Modifier.height(16.dp))

                VoxAbleTextField(
                    value = state.password,
                    onValueChange = viewModel::onPasswordChange,
                    label = "Şifre",
                    errorMessage = state.passwordError,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    accessibilityLabel = "Şifre giriş alanı"
                )

                // Şifremi unuttum
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = viewModel::onForgotPasswordClick,
                        modifier = Modifier.semantics {
                            contentDescription = "Şifremi unuttum, şifre sıfırlama"
                        }
                    ) {
                        Text(
                            text = "Şifremi Unuttum",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                VoxAbleButton(
                    text = "Giriş Yap",
                    onClick = viewModel::onLoginClick,
                    enabled = !state.isLoading && !state.isGoogleLoading,
                    accessibilityLabel = "E-posta ile giriş yap butonu"
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Ayırıcı
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f))
                    Text(
                        text = "  veya  ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Google ile giriş butonu
                OutlinedButton(
                    onClick = viewModel::onGoogleSignInClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .semantics {
                            contentDescription = "Google hesabı ile giriş yap"
                        },
                    enabled = !state.isLoading && !state.isGoogleLoading,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    shape = MaterialTheme.shapes.medium
                ) {
                    if (state.isGoogleLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Email,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Google ile Giriş Yap",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                TextButton(
                    onClick = onNavigateToRegister,
                    modifier = Modifier.semantics {
                        contentDescription = "Hesabınız yok mu? Kayıt olun sayfasına git"
                    }
                ) {
                    Text(
                        text = "Hesabınız yok mu? Kayıt olun",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
