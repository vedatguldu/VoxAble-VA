package com.voxable.feature_reader.data.parser

import com.voxable.feature_reader.domain.model.BookDocument
import com.voxable.feature_reader.domain.model.Chapter
import com.voxable.feature_reader.domain.model.DocumentFormat
import org.jsoup.Jsoup
import java.io.File

class DaisyParser : DocumentParser {
    override suspend fun parse(file: File, documentId: String): BookDocument {
        val content = file.readText(Charsets.UTF_8)
        val doc = Jsoup.parse(content, "", org.jsoup.parser.Parser.xmlParser())
        val chapters = mutableListOf<Chapter>()

        // DAISY 2.02 NCC format
        val nccHeadings = doc.select("h1, h2, h3, h4, h5, h6")
        if (nccHeadings.isNotEmpty()) {
            nccHeadings.forEachIndexed { index, heading ->
                val title = heading.text().trim()
                val anchor = heading.select("a")
                val href = anchor.attr("href")
                chapters.add(
                    Chapter(
                        index = index,
                        title = title.ifEmpty { "Bölüm ${index + 1}" },
                        content = title,
                        wordSpans = WordSpanExtractor.extract(title)
                    )
                )
            }
        }

        // DAISY 3 DTBook format
        val levels = doc.select("level1, level2, level, dtbook level1, dtbook level2")
        if (levels.isNotEmpty() && chapters.isEmpty()) {
            levels.forEachIndexed { index, level ->
                val heading = level.select("h1, h2, h3, hd").firstOrNull()
                val title = heading?.text()?.trim() ?: "Bölüm ${index + 1}"
                val text = level.text().trim()
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

        if (chapters.isEmpty()) {
            chapters.add(
                Chapter(
                    index = 0,
                    title = "İçerik",
                    content = doc.text().trim(),
                    wordSpans = WordSpanExtractor.extract(doc.text().trim())
                )
            )
        }

        return BookDocument(
            id = documentId,
            title = doc.title() ?: file.nameWithoutExtension,
            uri = android.net.Uri.fromFile(file),
            format = DocumentFormat.DAISY,
            chapters = chapters
        )
    }
}
