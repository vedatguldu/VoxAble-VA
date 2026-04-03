package com.voxable.feature_reader.data.parser

import com.voxable.feature_reader.domain.model.DocumentFormat
import java.io.File
import javax.inject.Inject

class DocumentParserFactory @Inject constructor() {
    fun getParser(format: DocumentFormat): DocumentParser {
        return when (format) {
            DocumentFormat.PDF -> PdfParser()
            DocumentFormat.EPUB -> EpubParser()
            DocumentFormat.TXT -> TxtParser()
            DocumentFormat.DOCX -> DocxParser()
            DocumentFormat.HTML -> HtmlParser()
            DocumentFormat.DAISY -> DaisyParser()
            DocumentFormat.FB2 -> Fb2Parser()
            DocumentFormat.CBZ -> CbzParser()
            DocumentFormat.RTF -> RtfParser()
        }
    }

    fun detectFormat(file: File): DocumentFormat {
        return when (file.extension.lowercase()) {
            "pdf" -> DocumentFormat.PDF
            "epub" -> DocumentFormat.EPUB
            "txt" -> DocumentFormat.TXT
            "docx" -> DocumentFormat.DOCX
            "html", "htm", "xhtml" -> DocumentFormat.HTML
            "xml", "ncc", "opf" -> DocumentFormat.DAISY
            "fb2" -> DocumentFormat.FB2
            "cbz" -> DocumentFormat.CBZ
            "rtf" -> DocumentFormat.RTF
            else -> DocumentFormat.TXT
        }
    }
}
