package com.voxable.feature_converter.domain.fileconverter.model

import android.net.Uri

data class ConversionRequest(
    val sourceUri: Uri,
    val sourceFileName: String,
    val conversionType: ConversionType,
    val outputFormat: String
)
