package com.voxable.feature_reader.data.parser

import com.voxable.feature_reader.domain.model.BookDocument
import com.voxable.feature_reader.domain.model.Chapter
import com.voxable.feature_reader.domain.model.DocumentFormat
import java.io.File

class TxtParser : DocumentParser {
    override suspend fun parse(file: File, documentId: String): BookDocument {
        val fullText = file.readText(Charsets.UTF_8)
        val sections = fullText.split("\n\n").filter { it.isNotBlank() }

        val chapters = if (sections.size <= 1) {
            // Single chapter - paginate by 3000 chars
            val pages = fullText.chunked(3000)
            pages.mapIndexed { index, text ->
                Chapter(
                    index = index,
                    title = "Sayfa ${index + 1}",
                    content = text.trim(),
                    wordSpans = WordSpanExtractor.extract(text.trim())
                )
            }
        } else {
            sections.mapIndexed { index, text ->
                Chapter(
                    index = index,
                    title = "Bölüm ${index + 1}",
                    content = text.trim(),
                    wordSpans = WordSpanExtractor.extract(text.trim())
                )
            }
        }

        return BookDocument(
            id = documentId,
            title = file.nameWithoutExtension,
            uri = android.net.Uri.fromFile(file),
            format = DocumentFormat.TXT,
            chapters = chapters
        )
    }
}
