package com.voxable.feature_reader.data.parser

import com.voxable.feature_reader.domain.model.BookDocument
import com.voxable.feature_reader.domain.model.Chapter
import com.voxable.feature_reader.domain.model.DocumentFormat
import org.apache.poi.xwpf.usermodel.XWPFDocument
import java.io.File
import java.io.FileInputStream

class DocxParser : DocumentParser {
    override suspend fun parse(file: File, documentId: String): BookDocument {
        val doc = XWPFDocument(FileInputStream(file))
        val sb = StringBuilder()

        doc.paragraphs.forEach { paragraph ->
            sb.appendLine(paragraph.text)
        }

        doc.tables.forEach { table ->
            table.rows.forEach { row ->
                row.tableCells.forEach { cell ->
                    sb.append(cell.text).append("\t")
                }
                sb.appendLine()
            }
        }

        val fullText = sb.toString().trim()
        val chapters = listOf(
            Chapter(
                index = 0,
                title = file.nameWithoutExtension,
                content = fullText,
                wordSpans = WordSpanExtractor.extract(fullText)
            )
        )
        doc.close()

        return BookDocument(
            id = documentId,
            title = file.nameWithoutExtension,
            uri = android.net.Uri.fromFile(file),
            format = DocumentFormat.DOCX,
            chapters = chapters
        )
    }
}
