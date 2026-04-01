package com.voxable.feature_reader.domain.model

import android.net.Uri

enum class DocumentFormat(val extensions: Set<String>) {
    PDF(setOf("pdf")),
    EPUB(setOf("epub")),
    TXT(setOf("txt")),
    DOCX(setOf("docx")),
    HTML(setOf("html", "htm", "xhtml")),
    DAISY(setOf("xml", "ncc", "opf"));

    companion object {
        fun fromExtension(extension: String): DocumentFormat? {
            val normalized = extension.trim().lowercase().removePrefix(".")
            return entries.firstOrNull { normalized in it.extensions }
        }
    }
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
    val description: String? = null,
    val ocrApplied: Boolean = false,
    val embeddedImageCount: Int = 0,
    val embeddedImageOcrSegments: Int = 0
)
