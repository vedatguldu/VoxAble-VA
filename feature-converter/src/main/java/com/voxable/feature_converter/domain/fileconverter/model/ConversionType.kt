package com.voxable.feature_converter.domain.fileconverter.model

enum class ConversionType(val label: String, val description: String) {
    VIDEO_TO_AUDIO("Video → Ses", "Video dosyasından ses çıkar"),
    AUDIO_TO_VIDEO("Ses → Video", "Sese siyah arka plan ekleyerek video oluştur"),
    VIDEO_FORMAT("Video Formatı", "Video formatını dönüştür (MP4, MKV, AVI, WebM)"),
    AUDIO_FORMAT("Ses Formatı", "Ses formatını dönüştür (MP3, AAC, WAV, OGG, FLAC)"),
    PDF_TO_TEXT("PDF → Metin", "PDF dosyasından metin çıkar"),
    TEXT_TO_PDF("Metin → PDF", "Metin dosyasından PDF oluştur"),
    IMAGE_TO_TEXT("Görüntü → Metin", "Görüntüdeki metni OCR ile çıkar")
}
