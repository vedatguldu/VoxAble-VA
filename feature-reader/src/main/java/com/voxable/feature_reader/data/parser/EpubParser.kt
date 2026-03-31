package com.voxable.feature_reader.data.parser

import com.voxable.feature_reader.domain.model.BookDocument
import com.voxable.feature_reader.domain.model.Chapter
import com.voxable.feature_reader.domain.model.DocumentFormat
import com.voxable.feature_reader.domain.model.DocumentMetadata
import nl.siegmann.epublib.epub.EpubReader
import org.jsoup.Jsoup
import java.io.File
import java.io.FileInputStream

class EpubParser : DocumentParser {
    override suspend fun parse(file: File, documentId: String): BookDocument {
        val reader = EpubReader()
        val book = reader.readEpub(FileInputStream(file))
        val chapters = mutableListOf<Chapter>()

        book.spine.spineReferences.forEachIndexed { index, spineRef ->
            val resource = spineRef.resource
            val html = String(resource.data, Charsets.UTF_8)
            val text = Jsoup.parse(html).text().trim()
            if (text.isNotEmpty()) {
                chapters.add(
                    Chapter(
                        index = index,
                        title = resource.title ?: "Bölüm ${index + 1}",
                        content = text,
                        wordSpans = WordSpanExtractor.extract(text)
                    )
                )
            }
        }

        val meta = book.metadata
        return BookDocument(
            id = documentId,
            title = meta.firstTitle ?: file.nameWithoutExtension,
            uri = android.net.Uri.fromFile(file),
            format = DocumentFormat.EPUB,
            chapters = chapters,
            metadata = DocumentMetadata(
                author = meta.authors.firstOrNull()?.let { "${it.firstname} ${it.lastname}" },
                publisher = meta.publishers.firstOrNull(),
                language = meta.language,
                description = meta.descriptions.firstOrNull()
            )
        )
    }
}
