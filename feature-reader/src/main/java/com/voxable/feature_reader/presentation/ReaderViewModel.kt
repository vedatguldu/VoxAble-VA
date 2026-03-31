package com.voxable.feature_reader.presentation

import android.net.Uri
import com.voxable.core.base.BaseViewModel
import com.voxable.core.util.Resource
import com.voxable.feature_reader.domain.model.Bookmark
import com.voxable.feature_reader.domain.model.ReadingPosition
import com.voxable.feature_reader.domain.model.TtsState
import com.voxable.feature_reader.domain.repository.ReaderRepository
import com.voxable.feature_reader.domain.usecase.ManageBookmarksUseCase
import com.voxable.feature_reader.domain.usecase.OcrDocumentUseCase
import com.voxable.feature_reader.domain.usecase.OpenDocumentUseCase
import com.voxable.feature_reader.domain.usecase.ReadAloudUseCase
import com.voxable.feature_reader.domain.usecase.ReadTextUseCase
import com.voxable.feature_reader.domain.usecase.ResumePositionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ReaderViewModel @Inject constructor(
    private val readTextUseCase: ReadTextUseCase,
    private val readerRepository: ReaderRepository,
    private val openDocumentUseCase: OpenDocumentUseCase,
    private val readAloudUseCase: ReadAloudUseCase,
    private val manageBookmarksUseCase: ManageBookmarksUseCase,
    private val resumePositionUseCase: ResumePositionUseCase,
    private val ocrDocumentUseCase: OcrDocumentUseCase
) : BaseViewModel<ReaderState, ReaderEvent>(ReaderState()) {

    fun onTextChange(text: String) {
        updateState { copy(inputText = text) }
    }

    fun onReadClick() {
        if (currentState.isSpeaking) {
            stopReading()
        } else if (currentState.isDocumentMode && currentState.chapterText.isNotBlank()) {
            startDocumentReading()
        } else {
            startTextReading()
        }
    }

    private fun startTextReading() {
        launch {
            updateState { copy(isSpeaking = true) }
            when (val result = readTextUseCase(currentState.inputText)) {
                is Resource.Success -> { }
                is Resource.Error -> {
                    updateState { copy(isSpeaking = false) }
                    sendEvent(ReaderEvent.ShowError(result.message))
                }
                is Resource.Loading -> Unit
            }
        }
    }

    private fun stopReading() {
        launch {
            readAloudUseCase.stop()
            readerRepository.stopSpeech()
            updateState {
                copy(
                    isSpeaking = false,
                    ttsState = ttsState.copy(isSpeaking = false, isPaused = false, currentWordIndex = -1)
                )
            }
        }
    }

    // ─── Belge yükleme ──────────────────────────────────────────────

    fun openDocument(uri: Uri, fileName: String? = null) {
        launch {
            updateState { copy(isLoading = true) }
            when (val result = openDocumentUseCase(uri, fileName)) {
                is Resource.Success -> {
                    val doc = result.data
                    val firstChapter = doc.chapters.firstOrNull()
                    updateState {
                        copy(
                            document = doc,
                            isDocumentMode = true,
                            currentChapterIndex = 0,
                            currentPageIndex = 0,
                            currentChapter = firstChapter,
                            chapterText = firstChapter?.content ?: "",
                            isLoading = false
                        )
                    }
                    loadReadingPosition(doc.id)
                    loadBookmarks(doc.id)
                    sendEvent(ReaderEvent.DocumentLoaded)
                }
                is Resource.Error -> {
                    updateState { copy(isLoading = false) }
                    sendEvent(ReaderEvent.ShowError(result.message))
                }
                is Resource.Loading -> Unit
            }
        }
    }

    // ─── Bölüm navigasyonu ──────────────────────────────────────────

    fun goToChapter(index: Int) {
        val doc = currentState.document ?: return
        val chapter = doc.chapters.getOrNull(index) ?: return
        launch {
            if (currentState.isSpeaking) {
                readAloudUseCase.stop()
                updateState { copy(isSpeaking = false, ttsState = TtsState()) }
            }
            updateState {
                copy(
                    currentChapterIndex = index,
                    currentChapter = chapter,
                    chapterText = chapter.content,
                    showChapterList = false
                )
            }
            saveCurrentPosition()
        }
    }

    fun nextChapter() {
        val doc = currentState.document ?: return
        val nextIndex = currentState.currentChapterIndex + 1
        if (nextIndex < doc.chapters.size) goToChapter(nextIndex)
    }

    fun previousChapter() {
        val prevIndex = currentState.currentChapterIndex - 1
        if (prevIndex >= 0) goToChapter(prevIndex)
    }

    fun toggleChapterList() {
        updateState { copy(showChapterList = !showChapterList) }
    }

    // ─── Sesli okuma ─────────────────────────────────────────────────

    private fun startDocumentReading() {
        launch {
            val text = currentState.chapterText
            if (text.isBlank()) {
                sendEvent(ReaderEvent.ShowError("Okunacak metin bulunamadı"))
                return@launch
            }
            updateState { copy(isSpeaking = true, ttsState = ttsState.copy(isSpeaking = true)) }
            when (val result = readAloudUseCase.start(
                text = text,
                language = currentState.ttsLanguage,
                speed = currentState.ttsSpeed,
                pitch = currentState.ttsPitch
            )) {
                is Resource.Success -> { }
                is Resource.Error -> {
                    updateState { copy(isSpeaking = false, ttsState = TtsState()) }
                    sendEvent(ReaderEvent.ShowError(result.message))
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun pauseReading() {
        launch {
            readAloudUseCase.pause()
            updateState {
                copy(
                    isSpeaking = false,
                    ttsState = ttsState.copy(isSpeaking = false, isPaused = true)
                )
            }
        }
    }

    fun resumeReading() {
        startDocumentReading()
    }

    // ─── TTS ayarları ───────────────────────────────────────────────

    fun setTtsSpeed(speed: Float) { updateState { copy(ttsSpeed = speed) } }
    fun setTtsPitch(pitch: Float) { updateState { copy(ttsPitch = pitch) } }
    fun setTtsLanguage(language: String) { updateState { copy(ttsLanguage = language) } }

    // ─── Yer imleri ─────────────────────────────────────────────────

    fun addBookmark(title: String, note: String? = null) {
        val doc = currentState.document ?: return
        launch {
            val bookmark = Bookmark(
                documentId = doc.id,
                title = title,
                chapterIndex = currentState.currentChapterIndex,
                pageIndex = currentState.currentPageIndex,
                note = note
            )
            when (val result = manageBookmarksUseCase.add(bookmark)) {
                is Resource.Success -> {
                    sendEvent(ReaderEvent.ShowMessage("Yer imi eklendi"))
                    loadBookmarks(doc.id)
                }
                is Resource.Error -> sendEvent(ReaderEvent.ShowError(result.message))
                is Resource.Loading -> Unit
            }
            updateState { copy(showBookmarkDialog = false) }
        }
    }

    fun removeBookmark(bookmarkId: Long) {
        val doc = currentState.document ?: return
        launch {
            when (val result = manageBookmarksUseCase.remove(bookmarkId)) {
                is Resource.Success -> {
                    sendEvent(ReaderEvent.ShowMessage("Yer imi silindi"))
                    loadBookmarks(doc.id)
                }
                is Resource.Error -> sendEvent(ReaderEvent.ShowError(result.message))
                is Resource.Loading -> Unit
            }
        }
    }

    fun goToBookmark(bookmark: Bookmark) { goToChapter(bookmark.chapterIndex) }

    fun toggleBookmarkDialog() { updateState { copy(showBookmarkDialog = !showBookmarkDialog) } }

    private fun loadBookmarks(documentId: String) {
        launch {
            when (val result = manageBookmarksUseCase.getAll(documentId)) {
                is Resource.Success -> updateState { copy(bookmarks = result.data) }
                is Resource.Error -> { }
                is Resource.Loading -> Unit
            }
        }
    }

    // ─── Okuma konumu ───────────────────────────────────────────────

    private fun loadReadingPosition(documentId: String) {
        launch {
            when (val result = resumePositionUseCase.get(documentId)) {
                is Resource.Success -> {
                    val position = result.data
                    if (position != null) {
                        updateState { copy(readingPosition = position) }
                        goToChapter(position.chapterIndex)
                    }
                }
                is Resource.Error -> { }
                is Resource.Loading -> Unit
            }
        }
    }

    fun saveCurrentPosition() {
        val doc = currentState.document ?: return
        launch {
            val totalChapters = doc.chapters.size.coerceAtLeast(1)
            val progress = (currentState.currentChapterIndex.toFloat() / totalChapters) * 100f
            val position = ReadingPosition(
                documentId = doc.id,
                chapterIndex = currentState.currentChapterIndex,
                pageIndex = currentState.currentPageIndex,
                progressPercent = progress
            )
            resumePositionUseCase.save(position)
        }
    }

    // ─── OCR ────────────────────────────────────────────────────────

    fun recognizeFromImage(imageUri: Uri) {
        launch {
            updateState { copy(isLoading = true) }
            when (val result = ocrDocumentUseCase.recognizeHybrid(imageUri)) {
                is Resource.Success -> {
                    updateState {
                        copy(
                            inputText = result.data,
                            chapterText = result.data,
                            isLoading = false
                        )
                    }
                    sendEvent(ReaderEvent.ShowMessage("OCR başarılı — metin tanındı"))
                }
                is Resource.Error -> {
                    updateState { copy(isLoading = false) }
                    sendEvent(ReaderEvent.ShowError(result.message))
                }
                is Resource.Loading -> Unit
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        saveCurrentPosition()
    }
}
