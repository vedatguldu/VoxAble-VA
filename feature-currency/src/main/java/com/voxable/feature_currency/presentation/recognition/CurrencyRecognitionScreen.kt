package com.voxable.feature_currency.presentation.recognition

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.voxable.core_ui.components.LoadingIndicator
import com.voxable.core_ui.components.VoxAbleTopBar
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyRecognitionScreen(
    onBack: () -> Unit,
    viewModel: CurrencyRecognitionViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.onCameraPermissionChanged(granted)
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { viewModel.onImageSelected(it) }
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    Scaffold(
        topBar = {
            VoxAbleTopBar(
                title = "Para Birimi Tanıma",
                onBack = onBack
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Otomatik sesli okuma ayarı
            item {
                AutoSpeakToggle(
                    autoSpeak = state.autoSpeak,
                    onToggle = { viewModel.onToggleAutoSpeak() }
                )
            }

            // Kamera önizleme veya yakalanan görüntü
            item {
                if (state.isCameraActive && state.hasCameraPermission) {
                    CameraPreviewCard(
                        onImageCaptured = { uri -> viewModel.onImageCaptured(uri) }
                    )
                } else if (state.capturedImageUri != null) {
                    CapturedImageCard(
                        imageUri = state.capturedImageUri!!,
                        onRetry = { viewModel.onRetry() }
                    )
                } else {
                    NoCameraCard(
                        hasPermission = state.hasCameraPermission,
                        onRequestPermission = {
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        },
                        onOpenCamera = { viewModel.onToggleCamera() }
                    )
                }
            }

            // Aksiyon butonları
            item {
                ActionButtons(
                    onGallery = {
                        galleryLauncher.launch(
                            ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                    },
                    onCamera = { viewModel.onToggleCamera() },
                    hasCameraPermission = state.hasCameraPermission,
                    isCameraActive = state.isCameraActive
                )
            }

            // Yükleniyor
            if (state.isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            LoadingIndicator()
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Para birimi tanınıyor...",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.semantics { liveRegion() }
                            )
                        }
                    }
                }
            }

            // Tanıma sonucu
            state.recognitionResult?.let { result ->
                item {
                    RecognitionResultCard(
                        result = result,
                        isSpeaking = state.isSpeaking,
                        onSpeak = { viewModel.onSpeakResult() }
                    )
                }
            }

            // Hata mesajı
            state.error?.let { error ->
                item {
                    ErrorCard(error = error)
                }
            }

            // Geçmiş sonuçlar
            if (state.recentResults.size > 1) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.History, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Son Tanımalar",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.semantics { heading() }
                            )
                        }
                        IconButton(
                            onClick = { viewModel.onClearHistory() },
                            modifier = Modifier.semantics {
                                contentDescription = "Geçmişi temizle"
                            }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null)
                        }
                    }
                }
                items(state.recentResults.drop(1)) { result ->
                    HistoryResultItem(result = result)
                }
            }
        }
    }
}

@Composable
private fun AutoSpeakToggle(
    autoSpeak: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (autoSpeak) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Otomatik Sesli Okuma",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            Switch(
                checked = autoSpeak,
                onCheckedChange = { onToggle() },
                modifier = Modifier.semantics {
                    contentDescription = if (autoSpeak)
                        "Otomatik sesli okuma açık"
                    else
                        "Otomatik sesli okuma kapalı"
                }
            )
        }
    }
}

@Composable
private fun CameraPreviewCard(
    onImageCaptured: (Uri) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(360.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                    }
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.surfaceProvider = previewView.surfaceProvider
                        }
                        val capture = ImageCapture.Builder()
                            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                            .build()
                        imageCapture = capture

                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview,
                                capture
                            )
                        } catch (_: Exception) {}
                    }, ContextCompat.getMainExecutor(ctx))
                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )

            // Çekim butonu
            FloatingActionButton(
                onClick = {
                    val capture = imageCapture ?: return@FloatingActionButton
                    val photoFile = File(
                        context.cacheDir,
                        "currency_${System.currentTimeMillis()}.jpg"
                    )
                    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
                    capture.takePicture(
                        outputOptions,
                        ContextCompat.getMainExecutor(context),
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                onImageCaptured(Uri.fromFile(photoFile))
                            }
                            override fun onError(exception: ImageCaptureException) {}
                        }
                    )
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
                    .size(72.dp)
                    .semantics { contentDescription = "Fotoğraf çek" },
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Camera,
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
private fun CapturedImageCard(
    imageUri: String,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = imageUri,
                contentDescription = "Yakalanan para birimi görüntüsü",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
            )
            IconButton(
                onClick = onRetry,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .background(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                        CircleShape
                    )
                    .semantics { contentDescription = "Yeniden çek" }
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun NoCameraCard(
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
    onOpenCamera: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = if (hasPermission) "Kamerayı açmak için butona dokunun"
                else "Kamera izni gerekli",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            androidx.compose.material3.TextButton(
                onClick = if (hasPermission) onOpenCamera else onRequestPermission
            ) {
                Text(if (hasPermission) "Kamerayı Aç" else "İzin Ver")
            }
        }
    }
}

@Composable
private fun ActionButtons(
    onGallery: () -> Unit,
    onCamera: () -> Unit,
    hasCameraPermission: Boolean,
    isCameraActive: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Galeri butonu
        androidx.compose.material3.OutlinedButton(
            onClick = onGallery,
            modifier = Modifier
                .weight(1f)
                .height(52.dp)
                .semantics { contentDescription = "Galeriden fotoğraf seç" }
        ) {
            Icon(Icons.Default.PhotoLibrary, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Galeri")
        }

        // Kamera butonu
        androidx.compose.material3.Button(
            onClick = onCamera,
            enabled = hasCameraPermission,
            modifier = Modifier
                .weight(1f)
                .height(52.dp)
                .semantics {
                    contentDescription = if (isCameraActive) "Kamerayı kapat" else "Kamerayı aç"
                }
        ) {
            Icon(Icons.Default.CameraAlt, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (isCameraActive) "Kamerayı Kapat" else "Kamera")
        }
    }
}

@Composable
private fun RecognitionResultCard(
    result: com.voxable.feature_currency.domain.model.CurrencyRecognitionResult,
    isSpeaking: Boolean,
    onSpeak: () -> Unit
) {
    val containerColor = if (result.isSuccessful)
        MaterialTheme.colorScheme.primaryContainer
    else
        MaterialTheme.colorScheme.errorContainer

    val contentColor = if (result.isSuccessful)
        MaterialTheme.colorScheme.onPrimaryContainer
    else
        MaterialTheme.colorScheme.onErrorContainer

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                liveRegion()
                contentDescription = result.toSpokenText()
            },
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tanıma Sonucu",
                    style = MaterialTheme.typography.titleMedium,
                    color = contentColor,
                    modifier = Modifier.semantics { heading() }
                )
                IconButton(
                    onClick = onSpeak,
                    modifier = Modifier.semantics {
                        contentDescription = if (isSpeaking) "Sesi durdur" else "Sonucu sesli oku"
                    }
                ) {
                    Icon(
                        imageVector = if (isSpeaking) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                        contentDescription = null,
                        tint = contentColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (result.isSuccessful && result.currency != null) {
                // Para birimi adı ve sembolü
                Text(
                    text = "${result.currency.symbol} ${result.currency.name}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = contentColor
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Kupür
                result.detectedDenomination?.let { denomination ->
                    Text(
                        text = "Kupür: $denomination ${result.currency.code}",
                        style = MaterialTheme.typography.titleLarge,
                        color = contentColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // Ülke
                Text(
                    text = "Ülke: ${result.currency.country}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = contentColor
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Güven oranı
                val confPercent = "%.0f".format(result.confidence * 100)
                Text(
                    text = "Güven: %$confPercent",
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor.copy(alpha = 0.8f)
                )
            } else {
                Text(
                    text = result.summary,
                    style = MaterialTheme.typography.bodyLarge,
                    color = contentColor
                )
            }
        }
    }
}

@Composable
private fun ErrorCard(error: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = "Hata: $error" },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Text(
            text = error,
            color = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun HistoryResultItem(
    result: com.voxable.feature_currency.domain.model.CurrencyRecognitionResult
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (result.isSuccessful && result.currency != null) {
                Text(
                    text = result.currency.symbol,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.width(40.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = result.currency.name,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    result.detectedDenomination?.let { denom ->
                        Text(
                            text = "$denom ${result.currency.code}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
                val confPercent = "%.0f".format(result.confidence * 100)
                Text(
                    text = "%$confPercent",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            } else {
                Text(
                    text = result.summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
