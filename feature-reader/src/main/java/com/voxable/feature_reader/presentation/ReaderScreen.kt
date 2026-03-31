package com.voxable.feature_reader.presentation

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Toc
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.voxable.core_ui.components.VoxAbleTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    onBack: () -> Unit,
    viewModel: ReaderViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val documentPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { viewModel.openDocument(it) }
    }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.recognizeFromImage(it) }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ReaderEvent.ShowError -> snackbarHostState.showSnackbar(event.message)
                is ReaderEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
                is ReaderEvent.DocumentLoaded -> snackbarHostState.showSnackbar("Belge y\u00fcklendi")
                is ReaderEvent.ReadingCompleted -> snackbarHostState.showSnackbar("Okuma tamamland\u0131")
            }
        }
    }

    Scaffold(
        topBar = {
            VoxAbleTopBar(
                title = if (state.isDocumentMode) (state.document?.title ?: "Belge Okuyucu") else "Metin Okuyucu",
                showBackButton = true,
                onBackClick = onBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (state.isDocumentMode) {
                ReaderBottomBar(
                    state = state,
                    onPlayPause = viewModel::onReadClick,
                    onStop = { viewModel.stopReading() },
                    onPrevChapter = viewModel::previousChapter,
                    onNextChapter = viewModel::nextChapter,
                    onToggleChapterList = viewModel::toggleChapterList,
                    onToggleBookmark = viewModel::toggleBookmarkDialog
                )
            }
        },
        floatingActionButton = {
            if (!state.isDocumentMode) {
                FloatingActionButton(
                    onClick = viewModel::onReadClick,
                    modifier = Modifier.semantics {
                        contentDescription = if (state.isSpeaking) "Okumay\u0131 durdur" else "Metni oku"
                    }
                ) {
                    Icon(
                        imageVector = if (state.isSpeaking) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (state.isDocumentMode) {
                DocumentReaderContent(
                    state = state,
                    onSpeedChange = viewModel::setTtsSpeed,
                    onPitchChange = viewModel::setTtsPitch
                )
            } else {
                TextInputContent(
                    state = state,
                    onTextChange = viewModel::onTextChange,
                    onOpenDocument = {
                        documentPicker.launch(
                            arrayOf(
                                "application/pdf",
                                "application/epub+zip",
                                "text/plain",
                                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                                "text/html",
                                "text/xml",
                                "application/oebps-package+xml"
                            )
                        )
                    },
                    onOpenOcr = { imagePicker.launch("image/*") }
                )
            }

            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.semantics {
                            contentDescription = "Y\u00fckleniyor"
                            liveRegion = LiveRegionMode.Polite
                        }
                    )
                }
            }
        }

        if (state.showChapterList) {
            ChapterListSheet(
                state = state,
                onChapterSelected = viewModel::goToChapter,
                onDismiss = viewModel::toggleChapterList
            )
        }

        if (state.showBookmarkDialog) {
            BookmarkDialog(
                bookmarks = state.bookmarks,
                onAdd = viewModel::addBookmark,
                onRemove = viewModel::removeBookmark,
                onGoTo = viewModel::goToBookmark,
                onDismiss = viewModel::toggleBookmarkDialog
            )
        }
    }
}

@Composable
private fun TextInputContent(
    state: ReaderState,
    onTextChange: (String) -> Unit,
    onOpenDocument: () -> Unit,
    onOpenOcr: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Okunmas\u0131n\u0131 istedi\u011finiz metni yaz\u0131n veya belge a\u00e7\u0131n:",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.semantics { heading() }
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onOpenDocument)
                    .semantics {
                        contentDescription = "Belge a\u00e7 \u2014 PDF, EPUB, TXT, DOCX, HTML veya DAISY dosyas\u0131 se\u00e7in"
                        role = Role.Button
                    },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Belge A\u00e7", style = MaterialTheme.typography.labelLarge)
                }
            }

            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onOpenOcr)
                    .semantics {
                        contentDescription = "OCR ile metin tan\u0131 \u2014 g\u00f6rselden metin \u00e7\u0131kar"
                        role = Role.Button
                    },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("OCR Tan\u0131ma", style = MaterialTheme.typography.labelLarge)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = state.inputText,
            onValueChange = onTextChange,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .semantics {
                    contentDescription = "Okunacak metin giri\u015f alan\u0131"
                },
            placeholder = { Text("Metni buraya yaz\u0131n...") },
            textStyle = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun DocumentReaderContent(
    state: ReaderState,
    onSpeedChange: (Float) -> Unit,
    onPitchChange: (Float) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        state.currentChapter?.let { chapter ->
            Text(
                text = chapter.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .semantics { heading() }
            )
        }

        state.document?.let { doc ->
            Text(
                text = "B\u00f6l\u00fcm ${state.currentChapterIndex + 1} / ${doc.chapters.size}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.semantics {
                    liveRegion = LiveRegionMode.Polite
                }
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        AnimatedVisibility(visible = state.isSpeaking || state.ttsState.isPaused) {
            TtsControlsRow(
                speed = state.ttsSpeed,
                pitch = state.ttsPitch,
                onSpeedChange = onSpeedChange,
                onPitchChange = onPitchChange
            )
        }

        Divider(modifier = Modifier.padding(vertical = 4.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .semantics { contentDescription = "Belge metni" }
        ) {
            Text(
                text = state.chapterText,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun TtsControlsRow(
    speed: Float,
    pitch: Float,
    onSpeedChange: (Float) -> Unit,
    onPitchChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text("H\u0131z: ${"%.1f".format(speed)}x", style = MaterialTheme.typography.bodySmall)
            Slider(
                value = speed,
                onValueChange = onSpeedChange,
                valueRange = 0.5f..3.0f,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
                    .semantics {
                        contentDescription = "Okuma h\u0131z\u0131: ${"%.1f".format(speed)} kat"
                        stateDescription = "${"%.1f".format(speed)}x"
                    }
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("\uD83C\uDFB5", style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.width(4.dp))
            Text("Perde: ${"%.1f".format(pitch)}", style = MaterialTheme.typography.bodySmall)
            Slider(
                value = pitch,
                onValueChange = onPitchChange,
                valueRange = 0.5f..2.0f,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
                    .semantics {
                        contentDescription = "Ses perdesi: ${"%.1f".format(pitch)}"
                        stateDescription = "${"%.1f".format(pitch)}"
                    }
            )
        }
    }
}

@Composable
private fun ReaderBottomBar(
    state: ReaderState,
    onPlayPause: () -> Unit,
    onStop: () -> Unit,
    onPrevChapter: () -> Unit,
    onNextChapter: () -> Unit,
    onToggleChapterList: () -> Unit,
    onToggleBookmark: () -> Unit
) {
    val doc = state.document
    val hasPrev = state.currentChapterIndex > 0
    val hasNext = doc != null && state.currentChapterIndex < (doc.chapters.size - 1)

    BottomAppBar(
        modifier = Modifier.semantics { contentDescription = "Okuyucu kontrolleri" }
    ) {
        IconButton(
            onClick = onToggleChapterList,
            modifier = Modifier.semantics { contentDescription = "\u0130\u00e7indekiler" }
        ) {
            Icon(Icons.Default.Toc, contentDescription = null)
        }

        IconButton(
            onClick = onPrevChapter,
            enabled = hasPrev,
            modifier = Modifier.semantics { contentDescription = "\u00d6nceki b\u00f6l\u00fcm" }
        ) {
            Icon(Icons.Default.ChevronLeft, contentDescription = null)
        }

        IconButton(
            onClick = onPlayPause,
            modifier = Modifier.semantics {
                contentDescription = if (state.isSpeaking) "Okumay\u0131 duraklat" else "Sesli oku"
            }
        ) {
            Icon(
                imageVector = if (state.isSpeaking) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = null
            )
        }

        if (state.isSpeaking || state.ttsState.isPaused) {
            IconButton(
                onClick = onStop,
                modifier = Modifier.semantics { contentDescription = "Okumay\u0131 durdur" }
            ) {
                Icon(Icons.Default.Stop, contentDescription = null)
            }
        }

        IconButton(
            onClick = onNextChapter,
            enabled = hasNext,
            modifier = Modifier.semantics { contentDescription = "Sonraki b\u00f6l\u00fcm" }
        ) {
            Icon(Icons.Default.ChevronRight, contentDescription = null)
        }

        Spacer(Modifier.weight(1f))

        IconButton(
            onClick = onToggleBookmark,
            modifier = Modifier.semantics { contentDescription = "Yer imleri" }
        ) {
            Icon(
                imageVector = if (state.bookmarks.isNotEmpty()) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                contentDescription = null
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChapterListSheet(
    state: ReaderState,
    onChapterSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    val chapters = state.document?.chapters ?: emptyList()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "\u0130\u00e7indekiler",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(bottom = 12.dp)
                    .semantics { heading() }
            )

            LazyColumn {
                items(chapters) { chapter ->
                    val isCurrent = chapter.index == state.currentChapterIndex
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isCurrent) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surface
                            )
                            .clickable { onChapterSelected(chapter.index) }
                            .padding(12.dp)
                            .semantics {
                                contentDescription = "B\u00f6l\u00fcm ${chapter.index + 1}: ${chapter.title}" +
                                        if (isCurrent) ", mevcut b\u00f6l\u00fcm" else ""
                                role = Role.Button
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = if (isCurrent)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = chapter.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
private fun BookmarkDialog(
    bookmarks: List<com.voxable.feature_reader.domain.model.Bookmark>,
    onAdd: (String, String?) -> Unit,
    onRemove: (Long) -> Unit,
    onGoTo: (com.voxable.feature_reader.domain.model.Bookmark) -> Unit,
    onDismiss: () -> Unit
) {
    var bookmarkTitle by rememberSaveable { mutableStateOf("") }
    var bookmarkNote by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Yer \u0130mleri",
                modifier = Modifier.semantics { heading() }
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = bookmarkTitle,
                    onValueChange = { bookmarkTitle = it },
                    label = { Text("Yer imi ad\u0131") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = "Yeni yer imi ad\u0131" },
                    singleLine = true
                )
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(
                    value = bookmarkNote,
                    onValueChange = { bookmarkNote = it },
                    label = { Text("Not (iste\u011fe ba\u011fl\u0131)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = "Yer imi notu" },
                    singleLine = true
                )
                Spacer(Modifier.height(4.dp))
                TextButton(
                    onClick = {
                        if (bookmarkTitle.isNotBlank()) {
                            onAdd(bookmarkTitle, bookmarkNote.ifBlank { null })
                            bookmarkTitle = ""
                            bookmarkNote = ""
                        }
                    },
                    modifier = Modifier.semantics { contentDescription = "Yer imi ekle" }
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Ekle")
                }

                if (bookmarks.isNotEmpty()) {
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    Text(
                        "Kay\u0131tl\u0131 Yer \u0130mleri",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                }

                bookmarks.forEach { bm ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { onGoTo(bm) }
                            .padding(vertical = 6.dp)
                            .semantics {
                                contentDescription = "${bm.title}, B\u00f6l\u00fcm ${bm.chapterIndex + 1}"
                                role = Role.Button
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Bookmark, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(bm.title, style = MaterialTheme.typography.bodyMedium)
                            bm.note?.let {
                                Text(
                                    it,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        IconButton(
                            onClick = { onRemove(bm.id) },
                            modifier = Modifier
                                .size(24.dp)
                                .semantics { contentDescription = "${bm.title} yer imini sil" }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Kapat")
            }
        }
    )
}
