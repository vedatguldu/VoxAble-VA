package com.voxable.feature_reader.domain.repository

import android.graphics.Bitmap
import android.net.Uri

interface OcrEngineRepository {
    suspend fun recognizeWithMlKit(bitmap: Bitmap, language: String = "latin"): String
    suspend fun recognizeWithTesseract(bitmap: Bitmap, language: String = "tur"): String
    suspend fun recognizeHybrid(bitmap: Bitmap, language: String = "latin"): String
    suspend fun recognizeFromUri(uri: Uri): String
}
