package com.voxable.feature_reader.data.parser

import com.voxable.feature_reader.domain.model.BookDocument
import com.voxable.feature_reader.domain.model.Chapter
import com.voxable.feature_reader.domain.model.DocumentFormat
import com.voxable.feature_reader.domain.model.DocumentMetadata
import java.io.File
import java.util.zip.ZipFile

class CbzParser : DocumentParser {

    companion object {
        private val IMAGE_EXTENSIONS = setOf("jpg", "jpeg", "png", "gif", "bmp", "webp")
    }

    override suspend fun parse(file: File, documentId: String): BookDocument {
        val zipFile = ZipFile(file)
        val imageEntries = zipFile.entries().toList()
            .filter { entry ->
                !entry.isDirectory &&
                        entry.name.substringAfterLast('.').lowercase() in IMAGE_EXTENSIONS
            }
            .sortedBy { it.name.lowercase() }

        val chapters = imageEntries.mapIndexed { index, entry ->
            val pageName = entry.name
                .substringAfterLast('/')
                .substringBeforeLast('.')

            Chapter(
                index = index,
                title = "Sayfa ${index + 1}",
                content = "[CBZ_IMAGE:${entry.name}]",
                wordSpans = emptyList()
            )
        }

        zipFile.close()

        return BookDocument(
            id = documentId,
            title = file.nameWithoutExtension,
            uri = android.net.Uri.fromFile(file),
            format = DocumentFormat.CBZ,
            chapters = chapters,
            metadata = DocumentMetadata(
                pageCount = chapters.size
            )
        )
    }

    /**
     * CBZ ar\u015fivinden belirli bir sayfan\u0131n resim byte'lar\u0131n\u0131 \u00e7\u0131kar\u0131r.
     * UI katman\u0131nda resim g\u00f6stermek i\u00e7in kullan\u0131l\u0131r.
     */
    fun extractImageBytes(cbzFile: File, imagePath: String): ByteArray? {
        return try {
            val zipFile = ZipFile(cbzFile)
            val entry = zipFile.getEntry(imagePath)
            val bytes = entry?.let { zipFile.getInputStream(it).use { stream -> stream.readBytes() } }
            zipFile.close()
            bytes
        } catch (e: Exception) {
            null
        }
    }

    /**
     * CBZ ar\u015fivindeki t\u00fcm resim yollar\u0131n\u0131 s\u0131ral\u0131 d\u00f6nd\u00fcr\u00fcr.
     */
    fun listImagePaths(cbzFile: File): List<String> {
        return try {
            val zipFile = ZipFile(cbzFile)
            val paths = zipFile.entries().toList()
                .filter { entry ->
                    !entry.isDirectory &&
                            entry.name.substringAfterLast('.').lowercase() in IMAGE_EXTENSIONS
                }
                .sortedBy { it.name.lowercase() }
                .map { it.name }
            zipFile.close()
            paths
        } catch (e: Exception) {
            emptyList()
        }
    }
}
