package com.voxable.feature_reader.presentation

import com.voxable.core.base.UiEvent
import com.voxable.core.base.UiState
import com.voxable.feature_reader.domain.model.BookDocument
import com.voxable.feature_reader.domain.model.Bookmark
import com.voxable.feature_reader.domain.model.Chapter
import com.voxable.feature_reader.domain.model.ReadingPosition
import com.voxable.feature_reader.domain.model.TtsState

data class ReaderState(
    // Eski basit TTS modu
    val inputText: String = "",

    // Belge
    val document: BookDocument? = null,
    val currentChapterIndex: Int = 0,
    val currentPageIndex: Int = 0,
    val currentChapter: Chapter? = null,
    val chapterText: String = "",

    // TTS
    val ttsState: TtsState = TtsState(),
    val isSpeaking: Boolean = false,

    // Yer imleri
    val bookmarks: List<Bookmark> = emptyList(),
    val showBookmarkDialog: Boolean = false,

    // Okuma konumu
    val readingPosition: ReadingPosition? = null,

    // Genel durum
    val isLoading: Boolean = false,
    val isDocumentMode: Boolean = false,
    val ttsSpeed: Float = 1.0f,
    val ttsPitch: Float = 1.0f,
    val ttsLanguage: String = "tr",

    // Bölüm listesi görünümü
    val showChapterList: Boolean = false
) : UiState

sealed class ReaderEvent : UiEvent {
    data class ShowError(val message: String) : ReaderEvent()
    data class ShowMessage(val message: String) : ReaderEvent()
    data object DocumentLoaded : ReaderEvent()
    data object ReadingCompleted : ReaderEvent()
}
