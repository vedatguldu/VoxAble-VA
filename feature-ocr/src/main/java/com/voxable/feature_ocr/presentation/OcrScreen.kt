package com.voxable.feature_ocr.presentation

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.voxable.core_ui.components.LoadingIndicator
import com.voxable.core_ui.components.VoxAbleButton
import com.voxable.core_ui.components.VoxAbleTopBar
import com.voxable.feature_ocr.domain.model.DetectedBarcode
import com.voxable.feature_ocr.domain.model.DetectedColor
import java.io.File
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OcrScreen(
    onBack: () -> Unit,
    viewModel: OcrViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    var previewUri by remember { mutableStateOf<Uri?>(null) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        previewUri = uri
        uri?.let { viewModel.onImageSelected(it) }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.onCameraPermissionChanged(granted)
        if (granted) viewModel.onToggleCamera()
    }

    LaunchedEffect(Unit) {
        viewModel.onCameraPermissionChanged(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
        viewModel.events.collect { }
    }

    Scaffold(
        topBar = {
            VoxAbleTopBar(
                title = "Metin Tanıma (OCR)",
                onBack = onBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LanguageSelector(
                selectedLanguage = state.selectedLanguage,
                onSelected = viewModel::onLanguageSelected
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    modifier = Modifier.weight(1f).heightIn(min = 56.dp).semantics {
                        contentDescription = "Galeriden görüntü seç"
                    }
                ) {
                    Icon(Icons.Default.Image, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Galeri")
                }

                OutlinedButton(
                    onClick = {
                        if (state.hasCameraPermission) viewModel.onToggleCamera()
                        else cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    },
                    modifier = Modifier.weight(1f).heightIn(min = 56.dp).semantics {
                        contentDescription = "Kamera aç"
                    }
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Kamera")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (state.isCameraActive) {
                LiveScanToggle(
                    liveScanEnabled = state.liveScanEnabled,
                    onToggle = { viewModel.onToggleLiveScan() }
                )
                Spacer(modifier = Modifier.height(12.dp))

                CameraCaptureCard(
                    context = context,
                    liveScanEnabled = state.liveScanEnabled,
                    onImageCaptured = { uri ->
                        previewUri = uri
                        viewModel.onImageCaptured(uri)
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            previewUri?.let { uri ->
                SelectedImagePreview(uri = uri, context = context)
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (state.isProcessing) {
                LoadingIndicator()
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Metin tanınıyor...",
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            state.error?.let { error ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Hata: $error" }
                ) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (state.analysisSummary.isNotBlank()) {
                SummaryCard(summary = state.analysisSummary)
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (state.recognizedText.isNotEmpty()) {
                Text(
                    text = "Tanınan Metin",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.fillMaxWidth().semantics { heading() }
                )
                Spacer(modifier = Modifier.height(8.dp))

                Card(modifier = Modifier.fillMaxWidth()) {
                    SelectionContainer {
                        Text(
                            text = state.recognizedText,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    VoxAbleButton(
                        text = "Kopyala",
                        onClick = { clipboardManager.setText(AnnotatedString(state.recognizedText)) },
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedButton(
                        onClick = { viewModel.onClearText() },
                        modifier = Modifier.weight(1f).heightIn(min = 56.dp)
                    ) {
                        Icon(Icons.Default.Clear, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Temizle")
                    }
                }
            }

            if (state.detectedBarcodes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "QR / Barkod Sonuçları",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.fillMaxWidth().semantics { heading() }
                )
                Spacer(modifier = Modifier.height(8.dp))
                BarcodeSection(barcodes = state.detectedBarcodes)
            }

            if (state.detectedColors.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Baskın Renkler",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.fillMaxWidth().semantics { heading() }
                )
                Spacer(modifier = Modifier.height(8.dp))
                ColorSection(colors = state.detectedColors)
            }
        }
    }
}

@Composable
private fun LiveScanToggle(
    liveScanEnabled: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Canlı Tarama",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Switch(
                checked = liveScanEnabled,
                onCheckedChange = { onToggle() }
            )
        }
    }
}

@Composable
private fun SummaryCard(summary: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Text(
            text = summary,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
private fun LanguageSelector(selectedLanguage: String, onSelected: (String) -> Unit) {
    val languages = listOf(
        "tr" to "Türkçe",
        "en" to "English",
        "de" to "Deutsch",
        "fr" to "Français",
        "es" to "Español",
        "ja" to "日本語",
        "ko" to "한국어",
        "zh" to "中文",
        "hi" to "हिन्दी"
    )
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("OCR Dili", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        languages.chunked(4).forEach { rowLanguages ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowLanguages.forEach { (code, label) ->
                    AssistChip(
                        onClick = { onSelected(code) },
                        label = { Text(label) },
                        leadingIcon = if (selectedLanguage == code) {
                            { Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        } else null
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SelectedImagePreview(uri: Uri, context: Context) {
    val bitmap = remember(uri.toString()) {
        context.contentResolver.openInputStream(uri)?.use(BitmapFactory::decodeStream)
    }
    bitmap?.let {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Collections, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Seçilen görsel", style = MaterialTheme.typography.titleSmall)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = "Seçilen OCR görseli",
                    modifier = Modifier.fillMaxWidth().height(220.dp)
                )
            }
        }
    }
}

@Composable
private fun BarcodeSection(barcodes: List<DetectedBarcode>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        barcodes.forEach { barcode ->
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.QrCode2, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${barcode.formatLabel} • ${barcode.valueTypeLabel}",
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    SelectionContainer {
                        Text(
                            text = barcode.displayValue,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorSection(colors: List<DetectedColor>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        colors.chunked(2).forEach { rowColors ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowColors.forEach { color ->
                    OutlinedCard(modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Palette, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = color.name,
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${(color.coverage * 100).toInt()} kapsama • ${color.hex}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                if (rowColors.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun CameraCaptureCard(
    context: Context,
    liveScanEnabled: Boolean,
    onImageCaptured: (Uri) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }
    val imageCapture = remember {
        ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).build()
    }

    DisposableEffect(lifecycleOwner) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val executor = ContextCompat.getMainExecutor(context)
        val listener = Runnable {
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also { it.surfaceProvider = previewView.surfaceProvider }
            runCatching {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture)
            }
        }
        cameraProviderFuture.addListener(listener, executor)
        onDispose {
            runCatching { cameraProviderFuture.get().unbindAll() }
        }
    }

    LaunchedEffect(liveScanEnabled) {
        while (liveScanEnabled) {
            delay(2500)
            val outputFile = File(context.cacheDir, "ocr_live_${System.currentTimeMillis()}.jpg")
            imageCapture.takePicture(
                ImageCapture.OutputFileOptions.Builder(outputFile).build(),
                ContextCompat.getMainExecutor(context),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        onImageCaptured(Uri.fromFile(outputFile))
                    }

                    override fun onError(exception: ImageCaptureException) {
                    }
                }
            )
        }
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Kamera OCR", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))
            Box(modifier = Modifier.fillMaxWidth().height(280.dp)) {
                AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
            }
            Spacer(modifier = Modifier.height(12.dp))
            VoxAbleButton(
                text = "Fotoğraf Çek ve Tara",
                onClick = {
                    val outputFile = File(context.cacheDir, "ocr_${System.currentTimeMillis()}.jpg")
                    imageCapture.takePicture(
                        ImageCapture.OutputFileOptions.Builder(outputFile).build(),
                        ContextCompat.getMainExecutor(context),
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                onImageCaptured(Uri.fromFile(outputFile))
                            }

                            override fun onError(exception: ImageCaptureException) {
                            }
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
