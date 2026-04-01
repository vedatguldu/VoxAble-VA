package com.voxable.feature_converter.presentation.fileconverter

import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.voxable.core_ui.components.VoxAbleTopBar
import com.voxable.feature_converter.domain.fileconverter.model.ConversionType
import com.voxable.feature_converter.domain.fileconverter.model.SupportedFormats

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileConverterScreen(
    onBack: () -> Unit,
    viewModel: FileConverterViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            val fileName = getFileName(context, it) ?: "dosya"
            viewModel.onFileSelected(it, fileName)
        }
    }

    Scaffold(
        topBar = {
            VoxAbleTopBar(
                title = "Dosya Dönüştürücü",
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
            // Dosya seçme bölümü
            item {
                FileSelectionCard(
                    fileName = state.sourceFileName,
                    extension = state.sourceExtension,
                    onSelectFile = {
                        filePicker.launch(arrayOf("*/*"))
                    },
                    onReset = { viewModel.onReset() }
                )
            }

            // Dönüşüm tipi seçimi
            if (state.availableConversionTypes.isNotEmpty()) {
                item {
                    ConversionTypeSelector(
                        types = state.availableConversionTypes,
                        selectedType = state.selectedConversionType,
                        onTypeSelected = { viewModel.onConversionTypeSelected(it) }
                    )
                }
            }

            // Çıktı formatı seçimi
            if (state.availableOutputFormats.isNotEmpty()) {
                item {
                    OutputFormatSelector(
                        formats = state.availableOutputFormats,
                        selectedFormat = state.selectedOutputFormat,
                        onFormatSelected = { viewModel.onOutputFormatSelected(it) }
                    )
                }
            }

            // Dönüştür butonu
            if (state.selectedConversionType != null && !state.isConverting) {
                item {
                    Button(
                        onClick = { viewModel.onStartConversion() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .semantics {
                                contentDescription = "Dönüşümü başlat"
                            },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Dönüştür",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }

            // Dönüşüm ilerlemesi
            if (state.isConverting) {
                item {
                    ConvertingIndicator(
                        onCancel = { viewModel.onCancelConversion() }
                    )
                }
            }

            // Sonuç
            state.conversionResult?.let { result ->
                item {
                    ConversionResultCard(
                        result = result,
                        onShare = { viewModel.onShareResult() }
                    )
                }

                // Metin sonucu (PDF→Text veya Image→Text)
                if (result.isTextResult && !result.extractedText.isNullOrBlank()) {
                    item {
                        ExtractedTextCard(text = result.extractedText)
                    }
                }
            }

            // Hata mesajı
            state.error?.let { error ->
                item {
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
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }

            // Desteklenen formatlar bilgisi
            item {
                SupportedFormatsInfo()
            }
        }
    }
}

@Composable
private fun FileSelectionCard(
    fileName: String?,
    extension: String,
    onSelectFile: () -> Unit,
    onReset: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (fileName == null) {
                Icon(
                    imageVector = Icons.Default.FileOpen,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Dönüştürmek istediğiniz dosyayı seçin",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onSelectFile,
                    modifier = Modifier.semantics {
                        contentDescription = "Dosya seç"
                    }
                ) {
                    Icon(Icons.Default.FileOpen, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Dosya Seç")
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = getFileIcon(extension),
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = fileName,
                                style = MaterialTheme.typography.bodyLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = ".${extension.uppercase()}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Row {
                        IconButton(
                            onClick = onSelectFile,
                            modifier = Modifier.semantics {
                                contentDescription = "Farklı dosya seç"
                            }
                        ) {
                            Icon(Icons.Default.FileOpen, contentDescription = null)
                        }
                        IconButton(
                            onClick = onReset,
                            modifier = Modifier.semantics {
                                contentDescription = "Sıfırla"
                            }
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ConversionTypeSelector(
    types: List<ConversionType>,
    selectedType: ConversionType?,
    onTypeSelected: (ConversionType) -> Unit
) {
    Column {
        Text(
            text = "Dönüşüm Türü",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.semantics { heading() }
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            types.forEach { type ->
                FilterChip(
                    selected = selectedType == type,
                    onClick = { onTypeSelected(type) },
                    label = { Text(type.label) },
                    modifier = Modifier.semantics {
                        contentDescription = "${type.label}: ${type.description}" +
                                if (selectedType == type) ", seçili" else ""
                    }
                )
            }
        }
    }
}

@Composable
private fun OutputFormatSelector(
    formats: List<String>,
    selectedFormat: String,
    onFormatSelected: (String) -> Unit
) {
    Column {
        Text(
            text = "Çıktı Formatı",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.semantics { heading() }
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            formats.forEach { format ->
                FilterChip(
                    selected = selectedFormat == format,
                    onClick = { onFormatSelected(format) },
                    label = { Text(".${format.uppercase()}") },
                    modifier = Modifier.semantics {
                        contentDescription = "${format.uppercase()} formatı" +
                                if (selectedFormat == format) ", seçili" else ""
                    }
                )
            }
        }
    }
}

@Composable
private fun ConvertingIndicator(onCancel: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { liveRegion() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Dönüştürülüyor...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.semantics {
                    contentDescription = "Dönüşümü iptal et"
                }
            ) {
                Icon(Icons.Default.Cancel, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("İptal")
            }
        }
    }
}

@Composable
private fun ConversionResultCard(
    result: com.voxable.feature_converter.domain.fileconverter.model.ConversionResult,
    onShare: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                liveRegion()
                contentDescription = "Dönüşüm tamamlandı: ${result.outputFileName}"
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
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
                    text = "Dönüşüm Tamamlandı",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.semantics { heading() }
                )
                IconButton(
                    onClick = onShare,
                    modifier = Modifier.semantics {
                        contentDescription = "Dosyayı paylaş"
                    }
                ) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = result.outputFileName,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            val duration = result.durationMs / 1000.0
            Text(
                text = "Süre: %.1f saniye".format(duration),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun ExtractedTextCard(text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Çıkarılan Metin",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.semantics { heading() }
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 50
            )
        }
    }
}

@Composable
private fun SupportedFormatsInfo() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Desteklenen Formatlar",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.semantics { heading() }
            )
            Spacer(modifier = Modifier.height(8.dp))
            FormatRow("Video", SupportedFormats.videoFormats)
            FormatRow("Ses", SupportedFormats.audioFormats)
            FormatRow("Görüntü", SupportedFormats.imageFormats)
            FormatRow("Belge", SupportedFormats.documentFormats)
        }
    }
}

@Composable
private fun FormatRow(label: String, formats: List<String>) {
    Row(
        modifier = Modifier.padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = formats.joinToString(", ") { it.uppercase() },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

private fun getFileIcon(extension: String) = when {
    SupportedFormats.isVideo(extension) -> Icons.Default.VideoFile
    SupportedFormats.isAudio(extension) -> Icons.Default.AudioFile
    SupportedFormats.isImage(extension) -> Icons.Default.Image
    else -> Icons.Default.Description
}

private fun getFileName(context: android.content.Context, uri: Uri): String? {
    var name: String? = null
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (nameIndex >= 0 && cursor.moveToFirst()) {
            name = cursor.getString(nameIndex)
        }
    }
    return name
}
