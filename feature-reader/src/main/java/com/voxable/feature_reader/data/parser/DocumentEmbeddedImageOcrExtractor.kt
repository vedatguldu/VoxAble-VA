package com.voxable.feature_reader.data.parser

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.voxable.feature_reader.domain.model.BookDocument
import com.voxable.feature_reader.domain.model.DocumentFormat
import com.voxable.feature_reader.domain.model.DocumentMetadata
import com.voxable.feature_reader.domain.repository.OcrEngineRepository
import nl.siegmann.epublib.epub.EpubReader
import org.apache.pdfbox.cos.COSName
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDResources
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DocumentEmbeddedImageOcrExtractor @Inject constructor(
    private val ocrRepository: OcrEngineRepository
) {

    suspend fun appendEmbeddedImageText(
        document: BookDocument,
        sourceFile: File,
        format: DocumentFormat
    ): BookDocument {
        val ocrBlocks = extractEmbeddedImageText(sourceFile, format, document.metadata?.language ?: "tr")
        if (ocrBlocks.isEmpty()) return document

        var imageCount = 0
        var ocrSegments = 0
        val chapters = document.chapters.map { chapter ->
            val chapterBlocks = ocrBlocks[chapter.index].orEmpty().filter { it.isNotBlank() }
            imageCount += chapterBlocks.size
            ocrSegments += chapterBlocks.size
            if (chapterBlocks.isEmpty()) {
                chapter
            } else {
                val appended = buildString {
                    append(chapter.content.trim())
                    append("\n\n[Gömülü görsellerden çıkarılan metin]\n")
                    chapterBlocks.forEachIndexed { index, block ->
                        append(index + 1)
                        append(". ")
                        append(block.trim())
                        append("\n")
                    }
                }.trim()
                chapter.copy(content = appended, wordSpans = WordSpanExtractor.extract(appended))
            }
        }

        return document.copy(
            chapters = chapters,
            metadata = (document.metadata ?: DocumentMetadata()).copy(
                ocrApplied = true,
                embeddedImageCount = imageCount,
                embeddedImageOcrSegments = ocrSegments
            )
        )
    }

    private suspend fun extractEmbeddedImageText(
        sourceFile: File,
        format: DocumentFormat,
        languageHint: String
    ): Map<Int, List<String>> {
        return when (format) {
            DocumentFormat.PDF -> extractFromPdf(sourceFile, languageHint)
            DocumentFormat.DOCX -> extractFromDocx(sourceFile, languageHint)
            DocumentFormat.EPUB -> extractFromEpub(sourceFile, languageHint)
            DocumentFormat.HTML -> extractFromHtml(sourceFile, languageHint)
            DocumentFormat.DAISY -> extractFromDaisy(sourceFile, languageHint)
            DocumentFormat.TXT -> emptyMap()
        }
    }

    private suspend fun extractFromPdf(file: File, language: String): Map<Int, List<String>> {
        val result = linkedMapOf<Int, MutableList<String>>()
        PDDocument.load(file).use { document ->
            document.pages.forEachIndexed { pageIndex, page ->
                val texts = mutableListOf<String>()
                collectPdfImages(page).forEach { bitmap ->
                    ocrBitmap(bitmap, language)?.let(texts::add)
                }
                if (texts.isNotEmpty()) result[pageIndex] = texts
            }
        }
        return result
    }

    private fun collectPdfImages(page: PDPage): List<Bitmap> {
        val bitmaps = mutableListOf<Bitmap>()
        collectPdfImages(page.resources, bitmaps)
        return bitmaps
    }

    private fun collectPdfImages(resources: PDResources?, output: MutableList<Bitmap>) {
        if (resources == null) return
        for (name: COSName in resources.xObjectNames) {
            when (val xObject = resources.getXObject(name)) {
                is PDImageXObject -> xObject.image?.let(output::add)
                is PDFormXObject -> collectPdfImages(xObject.resources, output)
            }
        }
    }

    private suspend fun extractFromDocx(file: File, language: String): Map<Int, List<String>> {
        XWPFDocument(FileInputStream(file)).use { document ->
            val texts = document.allPictures.mapNotNull { picture ->
                BitmapFactory.decodeByteArray(picture.data, 0, picture.data.size)?.let { bitmap ->
                    ocrBitmap(bitmap, language)
                }
            }
            return if (texts.isEmpty()) emptyMap() else mapOf(0 to texts)
        }
    }

    private suspend fun extractFromEpub(file: File, language: String): Map<Int, List<String>> {
        val result = linkedMapOf<Int, MutableList<String>>()
        EpubReader().readEpub(FileInputStream(file)).let { book ->
            book.spine.spineReferences.forEachIndexed { chapterIndex, spineRef ->
                val resource = spineRef.resource
                val html = String(resource.data, Charsets.UTF_8)
                val section = Jsoup.parse(html)
                val texts = section.select("img[src]").mapNotNull { element ->
                    val src = element.attr("src")
                    val imageResource = book.resources.all.find { candidate ->
                        candidate.href.equals(src, ignoreCase = true) || candidate.href.endsWith(src)
                    }
                    imageResource?.data?.let { data ->
                        BitmapFactory.decodeStream(ByteArrayInputStream(data))?.let { bitmap ->
                            ocrBitmap(bitmap, language)
                        }
                    }
                }
                if (texts.isNotEmpty()) result[chapterIndex] = texts.toMutableList()
            }
        }
        return result
    }

    private suspend fun extractFromHtml(file: File, language: String): Map<Int, List<String>> {
        val document = Jsoup.parse(file, Charsets.UTF_8.name())
        return extractFromHtmlDocument(document, file.parentFile ?: file, language)
    }

    private suspend fun extractFromDaisy(file: File, language: String): Map<Int, List<String>> {
        val document = Jsoup.parse(file, Charsets.UTF_8.name(), "", org.jsoup.parser.Parser.xmlParser())
        return extractFromHtmlDocument(document, file.parentFile ?: file, language)
    }

    private suspend fun extractFromHtmlDocument(
        document: Document,
        baseDir: File,
        language: String
    ): Map<Int, List<String>> {
        val result = linkedMapOf<Int, MutableList<String>>()
        val sections = document.select("article, section, div.chapter")
        val targets = if (sections.isEmpty()) listOf(document.body()).filterNotNull() else sections
        targets.forEachIndexed { index, section ->
            val texts = section.select("img[src]").mapNotNull { image ->
                resolveHtmlImage(image.attr("src"), baseDir)?.let { bitmap ->
                    ocrBitmap(bitmap, language)
                }
            }
            if (texts.isNotEmpty()) result[index] = texts.toMutableList()
        }
        return result
    }

    private fun resolveHtmlImage(src: String, baseDir: File): Bitmap? {
        if (src.startsWith("data:image", ignoreCase = true)) {
            val encoded = src.substringAfter("base64,", missingDelimiterValue = "")
            if (encoded.isBlank()) return null
            val bytes = Base64.decode(encoded, Base64.DEFAULT)
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }
        val file = if (src.startsWith("/")) File(src) else File(baseDir, src)
        return if (file.exists()) BitmapFactory.decodeFile(file.absolutePath) else null
    }

    private suspend fun ocrBitmap(bitmap: Bitmap, language: String): String? {
        return try {
            val text = ocrRepository.recognizeHybrid(bitmap, normalizeLanguage(language)).trim()
            if (text.length < 3) null else text
        } finally {
            if (!bitmap.isRecycled) bitmap.recycle()
        }
    }

    private fun normalizeLanguage(language: String): String {
        val normalized = language.lowercase(Locale.ROOT)
        return when {
            normalized.startsWith("tr") -> "tr"
            normalized.startsWith("en") -> "en"
            normalized.startsWith("de") -> "de"
            normalized.startsWith("fr") -> "fr"
            normalized.startsWith("es") -> "es"
            normalized.startsWith("ja") -> "ja"
            normalized.startsWith("ko") -> "ko"
            normalized.startsWith("zh") -> "zh"
            normalized.startsWith("hi") -> "hi"
            else -> "tr"
        }
    }
}