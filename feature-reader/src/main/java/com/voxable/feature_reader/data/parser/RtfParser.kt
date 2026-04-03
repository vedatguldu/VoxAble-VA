package com.voxable.feature_reader.data.parser

import com.voxable.feature_reader.domain.model.BookDocument
import com.voxable.feature_reader.domain.model.Chapter
import com.voxable.feature_reader.domain.model.DocumentFormat
import com.voxable.feature_reader.domain.model.DocumentMetadata
import java.io.File

class RtfParser : DocumentParser {

    override suspend fun parse(file: File, documentId: String): BookDocument {
        val rawContent = file.readText(Charsets.ISO_8859_1)
        val plainText = stripRtf(rawContent)
        val metadata = extractMetadata(rawContent)

        val chapters = paginateText(plainText)

        return BookDocument(
            id = documentId,
            title = metadata.author?.let { "$it \u2014 " }.orEmpty() + file.nameWithoutExtension,
            uri = android.net.Uri.fromFile(file),
            format = DocumentFormat.RTF,
            chapters = chapters,
            metadata = metadata
        )
    }

    private fun extractMetadata(raw: String): DocumentMetadata {
        val author = extractInfoField(raw, "author")
        val title = extractInfoField(raw, "title")
        return DocumentMetadata(
            author = author,
            description = title
        )
    }

    private fun extractInfoField(raw: String, field: String): String? {
        val regex = Regex("""\{\\$field\s+([^}]*)""")
        return regex.find(raw)?.groupValues?.get(1)?.trim()?.ifBlank { null }
    }

    /**
     * RTF kontrol kodlar\u0131n\u0131 kald\u0131rarak d\u00fcz metin \u00e7\u0131kar\u0131r.
     * Android'de javax.swing.text.rtf mevcut olmad\u0131\u011f\u0131 i\u00e7in regex tabanl\u0131 stripping.
     */
    private fun stripRtf(rtf: String): String {
        var text = rtf

        // \u00d6ncelikle header/font/color/style tablolar\u0131n\u0131 kald\u0131r
        text = removeRtfGroups(text, setOf("fonttbl", "colortbl", "stylesheet", "info", "header", "footer", "headerl", "headerr", "footerl", "footerr"))

        // \\par \u2192 yeni sat\u0131r
        text = text.replace(Regex("""\\par\b\s?"""  ), "\n")
        // \\line \u2192 yeni sat\u0131r
        text = text.replace(Regex("""\\line\b\s?"""), "\n")
        // \\tab \u2192 tab
        text = text.replace(Regex("""\\tab\b\s?"""), "\t")

        // Unicode karakter escape: \\'XX \u2192 karakter
        text = text.replace(Regex("""\\\\'([0-9a-fA-F]{2})""")) { match ->
            val code = match.groupValues[1].toIntOrNull(16) ?: 0
            if (code in 32..255) code.toChar().toString() else ""
        }

        // Unicode \\uN escape: \\uXXXXX? \u2192 karakter (ard\u0131ndan replacement char skip)
        text = text.replace(Regex("""\\u(\d+)\s?\??""")) { match ->
            val code = match.groupValues[1].toIntOrNull() ?: 0
            if (code in 0..0xFFFF) code.toChar().toString() else ""
        }

        // Kalan RTF kontrol kelimelerini kald\u0131r: \\word veya \\wordN
        text = text.replace(Regex("""\\[a-zA-Z]+\d*\s?"""), "")
        // \\ ile ka\u00e7\u0131r\u0131lm\u0131\u015f \u00f6zel karakterler
        text = text.replace(Regex("""\\[{}\\]""")) { match ->
            match.value.last().toString()
        }

        // Kalan s\u00fcsl\u00fc parantezleri kald\u0131r
        text = text.replace("{", "").replace("}", "")

        // \u00c7oklu bo\u015f sat\u0131rlar\u0131 temizle
        text = text.replace(Regex("""\n{3,}"""), "\n\n")

        return text.trim()
    }

    /**
     * Belirli RTF grup bloklar\u0131n\u0131 ({\\fonttbl ...}, {\\colortbl ...} vb.) kald\u0131r\u0131r.
     */
    private fun removeRtfGroups(text: String, groupNames: Set<String>): String {
        var result = text
        for (name in groupNames) {
            val startPattern = "{\\$name"
            var startIdx = result.indexOf(startPattern)
            while (startIdx >= 0) {
                var depth = 0
                var endIdx = startIdx
                while (endIdx < result.length) {
                    when (result[endIdx]) {
                        '{' -> depth++
                        '}' -> {
                            depth--
                            if (depth == 0) {
                                result = result.removeRange(startIdx, endIdx + 1)
                                break
                            }
                        }
                    }
                    endIdx++
                }
                startIdx = result.indexOf(startPattern)
            }
        }
        return result
    }

    private fun paginateText(text: String, charsPerChapter: Int = 3000): List<Chapter> {
        if (text.isBlank()) {
            return listOf(
                Chapter(
                    index = 0,
                    title = "Bo\u015f Belge",
                    content = "",
                    wordSpans = emptyList()
                )
            )
        }

        val chapters = mutableListOf<Chapter>()
        val paragraphs = text.split(Regex("""\n\n+"""))
        val buffer = StringBuilder()
        var chapterIndex = 0

        for (paragraph in paragraphs) {
            if (buffer.length + paragraph.length > charsPerChapter && buffer.isNotEmpty()) {
                val content = buffer.toString().trim()
                chapters.add(
                    Chapter(
                        index = chapterIndex,
                        title = "B\u00f6l\u00fcm ${chapterIndex + 1}",
                        content = content,
                        wordSpans = WordSpanExtractor.extract(content)
                    )
                )
                chapterIndex++
                buffer.clear()
            }
            buffer.appendLine(paragraph)
            buffer.appendLine()
        }

        if (buffer.isNotBlank()) {
            val content = buffer.toString().trim()
            chapters.add(
                Chapter(
                    index = chapterIndex,
                    title = if (chapters.isEmpty()) "\u0130\u00e7erik" else "B\u00f6l\u00fcm ${chapterIndex + 1}",
                    content = content,
                    wordSpans = WordSpanExtractor.extract(content)
                )
            )
        }

        return chapters
    }
}
