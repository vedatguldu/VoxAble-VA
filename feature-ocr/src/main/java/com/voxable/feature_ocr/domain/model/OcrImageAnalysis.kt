package com.voxable.feature_ocr.domain.model

data class OcrImageAnalysis(
    val recognizedText: String,
    val detectedColors: List<DetectedColor>,
    val detectedBarcodes: List<DetectedBarcode>,
    val summary: String
)

data class DetectedColor(
    val name: String,
    val hex: String,
    val coverage: Float
)

data class DetectedBarcode(
    val rawValue: String,
    val displayValue: String,
    val formatLabel: String,
    val valueTypeLabel: String
)
