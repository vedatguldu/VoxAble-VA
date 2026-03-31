package com.voxable.feature_reader.data.parser

import com.voxable.feature_reader.domain.model.BookDocument
import com.voxable.feature_reader.domain.model.Chapter
import com.voxable.feature_reader.domain.model.DocumentFormat
import org.jsoup.Jsoup
import java.io.File

class HtmlParser : DocumentParser {
    override suspend fun parse(file: File, documentId: String): BookDocument {
        val html = file.readText(Charsets.UTF_8)
        val doc = Jsoup.parse(html)
        val chapters = mutableListOf<Chapter>()

        // Try article/section elements for chapters
        val sections = doc.select("article, section, div.chapter")
        if (sections.isNotEmpty()) {
            sections.forEachIndexed { index, section ->
                val title = section.select("h1, h2, h3").firstOrNull()?.text() ?: "Bölüm ${index + 1}"
                val text = section.text().trim()
                if (text.isNotEmpty()) {
                    chapters.add(
                        Chapter(
                            index = index,
                            title = title,
                            content = text,
                            wordSpans = WordSpanExtractor.extract(text)
                        )
                    )
                }
            }
        }

        if (chapters.isEmpty()) {
            val fullText = doc.body()?.text()?.trim() ?: ""
            chapters.add(
                Chapter(
                    index = 0,
                    title = doc.title() ?: file.nameWithoutExtension,
                    content = fullText,
                    wordSpans = WordSpanExtractor.extract(fullText)
                )
            )
        }

        return BookDocument(
            id = documentId,
            title = doc.title() ?: file.nameWithoutExtension,
            uri = android.net.Uri.fromFile(file),
            format = DocumentFormat.HTML,
            chapters = chapters
        )
    }
}
