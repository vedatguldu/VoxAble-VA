package com.voxable.feature_reader.domain.model

import android.net.Uri

enum class DocumentFormat {
    PDF, EPUB, TXT, DOCX, HTML, DAISY
}

data class BookDocument(
    val id: String,
    val title: String,
    val uri: Uri,
    val format: DocumentFormat,
    val chapters: List<Chapter>,
    val metadata: DocumentMetadata? = null
)

data class Chapter(
    val index: Int,
    val title: String,
    val content: String,
    val wordSpans: List<WordSpan> = emptyList()
)

data class WordSpan(
    val start: Int,
    val end: Int,
    val word: String
)

data class DocumentMetadata(
    val author: String? = null,
    val publisher: String? = null,
    val language: String? = null,
    val pageCount: Int? = null,
    val description: String? = null
)
