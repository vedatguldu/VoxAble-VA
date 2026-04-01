package com.voxable.feature_converter.domain.fileconverter.model

import android.net.Uri

data class ConversionResult(
    val outputUri: Uri,
    val outputFileName: String,
    val conversionType: ConversionType,
    val extractedText: String? = null,
    val durationMs: Long = 0
) {
    val isTextResult: Boolean
        get() = conversionType in listOf(
            ConversionType.PDF_TO_TEXT,
            ConversionType.IMAGE_TO_TEXT
        )
}
