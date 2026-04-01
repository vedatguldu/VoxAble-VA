package com.voxable.feature_converter.data.engine

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class ImageOcrEngine @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    /**
     * Image → Text: ML Kit ile görüntüden metin çıkar.
     */
    suspend fun extractText(imageUri: Uri): Pair<String, File> {
        val text = recognizeFromUri(imageUri)
        val outputDir = File(context.cacheDir, "conversions").apply { mkdirs() }
        val outputFile = File(outputDir, "ocr_text_${System.currentTimeMillis()}.txt")
        outputFile.writeText(text)
        return text to outputFile
    }

    private suspend fun recognizeFromUri(uri: Uri): String =
        suspendCancellableCoroutine { continuation ->
            try {
                val image = InputImage.fromFilePath(context, uri)
                textRecognizer.process(image)
                    .addOnSuccessListener { result ->
                        continuation.resume(result.text)
                    }
                    .addOnFailureListener {
                        continuation.resume("")
                    }
            } catch (e: Exception) {
                continuation.resume("")
            }
        }

    fun release() {
        textRecognizer.close()
    }
}
