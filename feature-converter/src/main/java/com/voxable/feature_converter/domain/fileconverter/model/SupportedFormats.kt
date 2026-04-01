package com.voxable.feature_converter.domain.fileconverter.model

object SupportedFormats {
    val videoFormats = listOf("mp4", "mkv", "avi", "webm", "mov", "flv", "3gp")
    val audioFormats = listOf("mp3", "aac", "wav", "ogg", "flac", "m4a", "wma")
    val imageFormats = listOf("jpg", "jpeg", "png", "bmp", "webp", "tiff")
    val documentFormats = listOf("pdf", "txt")

    fun isVideo(extension: String): Boolean =
        extension.lowercase() in videoFormats

    fun isAudio(extension: String): Boolean =
        extension.lowercase() in audioFormats

    fun isImage(extension: String): Boolean =
        extension.lowercase() in imageFormats

    fun isPdf(extension: String): Boolean =
        extension.lowercase() == "pdf"

    fun isText(extension: String): Boolean =
        extension.lowercase() == "txt"

    fun getOutputFormatsFor(type: ConversionType): List<String> = when (type) {
        ConversionType.VIDEO_TO_AUDIO -> audioFormats
        ConversionType.AUDIO_TO_VIDEO -> videoFormats
        ConversionType.VIDEO_FORMAT -> videoFormats
        ConversionType.AUDIO_FORMAT -> audioFormats
        ConversionType.PDF_TO_TEXT -> listOf("txt")
        ConversionType.TEXT_TO_PDF -> listOf("pdf")
        ConversionType.IMAGE_TO_TEXT -> listOf("txt")
    }

    fun detectConversionTypes(extension: String): List<ConversionType> {
        val ext = extension.lowercase()
        val types = mutableListOf<ConversionType>()
        if (isVideo(ext)) {
            types += ConversionType.VIDEO_TO_AUDIO
            types += ConversionType.VIDEO_FORMAT
        }
        if (isAudio(ext)) {
            types += ConversionType.AUDIO_FORMAT
            types += ConversionType.AUDIO_TO_VIDEO
        }
        if (isPdf(ext)) {
            types += ConversionType.PDF_TO_TEXT
        }
        if (isText(ext)) {
            types += ConversionType.TEXT_TO_PDF
        }
        if (isImage(ext)) {
            types += ConversionType.IMAGE_TO_TEXT
        }
        return types
    }
}
