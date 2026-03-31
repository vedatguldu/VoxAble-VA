package com.voxable.feature_auth.presentation.register

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.voxable.core_ui.components.LoadingIndicator
import com.voxable.core_ui.components.VoxAbleButton
import com.voxable.core_ui.components.VoxAbleTextField

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is RegisterEvent.RegisterSuccess -> onRegisterSuccess()
                is RegisterEvent.ShowError -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (state.isLoading) {
            LoadingIndicator(accessibilityLabel = "Kayıt yapılıyor, lütfen bekleyin")
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
                    text = "Kayıt Ol",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.semantics { heading() }
                )

                Spacer(modifier = Modifier.height(24.dp))

                VoxAbleTextField(
                    value = state.displayName,
                    onValueChange = viewModel::onNameChange,
                    label = "Ad Soyad",
                    errorMessage = state.nameError,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    accessibilityLabel = "Ad Soyad giriş alanı"
                )

                Spacer(modifier = Modifier.height(12.dp))

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

                Spacer(modifier = Modifier.height(12.dp))

                VoxAbleTextField(
                    value = state.password,
                    onValueChange = viewModel::onPasswordChange,
                    label = "Şifre",
                    errorMessage = state.passwordError,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    accessibilityLabel = "Şifre giriş alanı"
                )

                Spacer(modifier = Modifier.height(12.dp))

                VoxAbleTextField(
                    value = state.confirmPassword,
                    onValueChange = viewModel::onConfirmPasswordChange,
                    label = "Şifre Tekrar",
                    errorMessage = state.confirmPasswordError,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    accessibilityLabel = "Şifre tekrar giriş alanı"
                )

                Spacer(modifier = Modifier.height(24.dp))

                VoxAbleButton(
                    text = "Kayıt Ol",
                    onClick = viewModel::onRegisterClick,
                    accessibilityLabel = "Kayıt ol butonu"
                )

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = onNavigateToLogin) {
                    Text(
                        text = "Zaten hesabınız var mı? Giriş yapın",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
