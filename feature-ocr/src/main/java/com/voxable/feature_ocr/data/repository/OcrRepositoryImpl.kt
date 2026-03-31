package com.voxable.feature_ocr.data.repository

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.voxable.core.util.Resource
import com.voxable.feature_ocr.domain.repository.OcrRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class OcrRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : OcrRepository {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    override suspend fun recognizeTextFromImage(imageUri: Uri): Resource<String> {
        return try {
            val image = InputImage.fromFilePath(context, imageUri)
            val result = suspendCancellableCoroutine { continuation ->
                recognizer.process(image)
                    .addOnSuccessListener { visionText ->
                        continuation.resume(Resource.Success(visionText.text))
                    }
                    .addOnFailureListener { e ->
                        continuation.resume(
                            Resource.Error(e.message ?: "Metin tanıma başarısız oldu")
                        )
                    }
            }
            result
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Görüntü işlenirken hata oluştu")
        }
    }
}
