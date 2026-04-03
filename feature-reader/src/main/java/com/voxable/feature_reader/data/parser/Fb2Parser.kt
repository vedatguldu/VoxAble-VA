package com.voxable.feature_reader.data.parser

import com.voxable.feature_reader.domain.model.BookDocument
import com.voxable.feature_reader.domain.model.Chapter
import com.voxable.feature_reader.domain.model.DocumentFormat
import com.voxable.feature_reader.domain.model.DocumentMetadata
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.parser.Parser
import java.io.File

class Fb2Parser : DocumentParser {

    override suspend fun parse(file: File, documentId: String): BookDocument {
        val doc = Jsoup.parse(file.readText(), "", Parser.xmlParser())

        val metadata = extractMetadata(doc)
        val chapters = extractChapters(doc)

        return BookDocument(
            id = documentId,
            title = metadata.author?.let { "$it \u2014 " }.orEmpty() +
                    (doc.selectFirst("book-title")?.text() ?: file.nameWithoutExtension),
            uri = android.net.Uri.fromFile(file),
            format = DocumentFormat.FB2,
            chapters = chapters.ifEmpty {
                listOf(
                    Chapter(
                        index = 0,
                        title = "\u0130\u00e7erik",
                        content = doc.selectFirst("body")?.text().orEmpty(),
                        wordSpans = WordSpanExtractor.extract(
                            doc.selectFirst("body")?.text().orEmpty()
                        )
                    )
                )
            },
            metadata = metadata
        )
    }

    private fun extractMetadata(doc: Document): DocumentMetadata {
        val titleInfo = doc.selectFirst("title-info")
        val author = titleInfo?.selectFirst("author")?.let { authorEl ->
            listOfNotNull(
                authorEl.selectFirst("first-name")?.text(),
                authorEl.selectFirst("middle-name")?.text(),
                authorEl.selectFirst("last-name")?.text()
            ).joinToString(" ").ifBlank { null }
        }
        val lang = titleInfo?.selectFirst("lang")?.text()
        val annotation = titleInfo?.selectFirst("annotation")?.text()
        val publishInfo = doc.selectFirst("publish-info")
        val publisher = publishInfo?.selectFirst("publisher")?.text()

        return DocumentMetadata(
            author = author,
            publisher = publisher,
            language = lang,
            description = annotation
        )
    }

    private fun extractChapters(doc: Document): List<Chapter> {
        val chapters = mutableListOf<Chapter>()
        val body = doc.selectFirst("body") ?: return chapters
        val sections = body.select("> section")

        if (sections.isEmpty()) {
            val text = extractSectionText(body)
            if (text.isNotBlank()) {
                chapters.add(
                    Chapter(
                        index = 0,
                        title = "\u0130\u00e7erik",
                        content = text,
                        wordSpans = WordSpanExtractor.extract(text)
                    )
                )
            }
            return chapters
        }

        sections.forEachIndexed { index, section ->
            val title = section.selectFirst("title")?.text()?.trim() ?: "B\u00f6l\u00fcm ${index + 1}"
            val text = extractSectionText(section)
            if (text.isNotBlank()) {
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
        return chapters
    }

    private fun extractSectionText(element: Element): String {
        val sb = StringBuilder()
        for (child in element.children()) {
            when (child.tagName()) {
                "p" -> {
                    sb.appendLine(child.text())
                    sb.appendLine()
                }
                "empty-line" -> sb.appendLine()
                "subtitle" -> {
                    sb.appendLine(child.text())
                    sb.appendLine()
                }
                "epigraph", "cite" -> {
                    sb.appendLine(child.text())
                    sb.appendLine()
                }
                "poem" -> {
                    child.select("stanza > v").forEach { verse ->
                        sb.appendLine(verse.text())
                    }
                    sb.appendLine()
                }
                "title" -> { /* ba\u015fl\u0131k zaten chapter title olarak kullan\u0131l\u0131yor */ }
                "section" -> { /* i\u00e7 i\u00e7e section'lar ana ak\u0131\u015fa dahil */ }
                else -> {
                    val text = child.text().trim()
                    if (text.isNotEmpty()) {
                        sb.appendLine(text)
                        sb.appendLine()
                    }
                }
            }
        }
        return sb.toString().trim()
    }
}
