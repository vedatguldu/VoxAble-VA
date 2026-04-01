package com.voxable.feature_converter.data.engine

import android.content.Context
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PdfEngine @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * PDF → Text: Sayfaları tarayıp metin çıkar.
     * Android'in PdfRenderer'ı doğrudan metin çıkaramaz,
     * bu yüzden her sayfayı bitmap'e render edip ML Kit OCR ile metin çıkarıyoruz.
     */
    suspend fun pdfToText(pdfUri: Uri): Pair<String, File> = withContext(Dispatchers.IO) {
        val tempPdf = copyToCache(pdfUri, "pdf_input")
        val pfd = ParcelFileDescriptor.open(tempPdf, ParcelFileDescriptor.MODE_READ_ONLY)
        val renderer = PdfRenderer(pfd)

        val allText = StringBuilder()

        for (i in 0 until renderer.pageCount) {
            val page = renderer.openPage(i)
            val bitmap = android.graphics.Bitmap.createBitmap(
                page.width * 2, page.height * 2,
                android.graphics.Bitmap.Config.ARGB_8888
            )
            // Beyaz arka plan
            val canvas = android.graphics.Canvas(bitmap)
            canvas.drawColor(android.graphics.Color.WHITE)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            page.close()

            // ML Kit ile OCR
            val text = recognizeTextFromBitmap(bitmap)
            if (text.isNotBlank()) {
                allText.appendLine("--- Sayfa ${i + 1} ---")
                allText.appendLine(text)
                allText.appendLine()
            }
            bitmap.recycle()
        }
        renderer.close()
        pfd.close()
        tempPdf.delete()

        val outputFile = File(context.cacheDir, "conversions").apply { mkdirs() }
            .let { File(it, "pdf_text_${System.currentTimeMillis()}.txt") }
        outputFile.writeText(allText.toString())

        allText.toString() to outputFile
    }

    /**
     * Text → PDF: Metin dosyasından PDF oluştur.
     */
    suspend fun textToPdf(textUri: Uri): File = withContext(Dispatchers.IO) {
        val textContent = context.contentResolver.openInputStream(textUri)?.use {
            it.bufferedReader().readText()
        } ?: throw IllegalStateException("Metin dosyası okunamadı")

        val document = PdfDocument()
        val paint = android.graphics.Paint().apply {
            textSize = 12f
            isAntiAlias = true
            color = android.graphics.Color.BLACK
        }

        val pageWidth = 595 // A4
        val pageHeight = 842
        val margin = 40
        val lineHeight = 18f
        val maxLinesPerPage = ((pageHeight - 2 * margin) / lineHeight).toInt()

        val lines = textContent.split("\n")
        var lineIndex = 0
        var pageNumber = 1

        while (lineIndex < lines.size) {
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            // Beyaz arka plan
            canvas.drawColor(android.graphics.Color.WHITE)

            var y = margin + lineHeight
            var linesOnPage = 0
            while (lineIndex < lines.size && linesOnPage < maxLinesPerPage) {
                val line = lines[lineIndex]
                // Satır sarma
                val maxCharsPerLine = ((pageWidth - 2 * margin) / paint.measureText("M")).toInt()
                if (line.length <= maxCharsPerLine) {
                    canvas.drawText(line, margin.toFloat(), y, paint)
                    y += lineHeight
                    linesOnPage++
                } else {
                    var pos = 0
                    while (pos < line.length && linesOnPage < maxLinesPerPage) {
                        val end = (pos + maxCharsPerLine).coerceAtMost(line.length)
                        canvas.drawText(line.substring(pos, end), margin.toFloat(), y, paint)
                        y += lineHeight
                        linesOnPage++
                        pos = end
                    }
                    if (pos < line.length) break // Satır bitmedi, yeni sayfaya devam
                }
                lineIndex++
            }
            document.finishPage(page)
            pageNumber++
        }

        val outputDir = File(context.cacheDir, "conversions").apply { mkdirs() }
        val outputFile = File(outputDir, "text_pdf_${System.currentTimeMillis()}.pdf")
        FileOutputStream(outputFile).use { fos ->
            document.writeTo(fos)
        }
        document.close()

        outputFile
    }

    private suspend fun recognizeTextFromBitmap(bitmap: android.graphics.Bitmap): String {
        return kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
            val image = com.google.mlkit.vision.common.InputImage.fromBitmap(bitmap, 0)
            val recognizer = com.google.mlkit.vision.text.TextRecognition.getClient(
                com.google.mlkit.vision.text.latin.TextRecognizerOptions.DEFAULT_OPTIONS
            )
            recognizer.process(image)
                .addOnSuccessListener { result ->
                    continuation.resume(result.text)
                }
                .addOnFailureListener {
                    continuation.resume("")
                }
        }
    }

    private fun copyToCache(uri: Uri, prefix: String): File {
        val input = context.contentResolver.openInputStream(uri)
            ?: throw IllegalStateException("Dosya okunamadı: $uri")
        val temp = File(context.cacheDir, "${prefix}_${System.currentTimeMillis()}")
        input.use { ins -> temp.outputStream().use { outs -> ins.copyTo(outs) } }
        return temp
    }
}
