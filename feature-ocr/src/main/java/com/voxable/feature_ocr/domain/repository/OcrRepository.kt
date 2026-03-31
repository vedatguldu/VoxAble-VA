package com.voxable.feature_ocr.domain.repository

import android.net.Uri
import com.voxable.core.util.Resource

interface OcrRepository {
    suspend fun recognizeTextFromImage(imageUri: Uri): Resource<String>
}
