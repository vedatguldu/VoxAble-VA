package com.voxable.feature_reader.data.parser

import com.voxable.feature_reader.domain.model.BookDocument
import com.voxable.feature_reader.domain.model.Chapter
import com.voxable.feature_reader.domain.model.DocumentFormat
import com.voxable.feature_reader.domain.model.DocumentMetadata
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import java.io.File

class PdfParser : DocumentParser {
    override suspend fun parse(file: File, documentId: String): BookDocument {
        val document = PDDocument.load(file)
        val chapters = mutableListOf<Chapter>()
        val stripper = PDFTextStripper()
        val totalPages = document.numberOfPages

        for (page in 1..totalPages) {
            stripper.startPage = page
            stripper.endPage = page
            val text = stripper.getText(document).trim()
            if (text.isNotEmpty()) {
                chapters.add(
                    Chapter(
                        index = page - 1,
                        title = "Sayfa $page",
                        content = text,
                        wordSpans = WordSpanExtractor.extract(text)
                    )
                )
            }
        }

        val info = document.documentInformation
        val metadata = DocumentMetadata(
            author = info?.author,
            pageCount = totalPages
        )
        document.close()

        return BookDocument(
            id = documentId,
            title = info?.title ?: file.nameWithoutExtension,
            uri = android.net.Uri.fromFile(file),
            format = DocumentFormat.PDF,
            chapters = chapters,
            metadata = metadata
        )
    }
}
