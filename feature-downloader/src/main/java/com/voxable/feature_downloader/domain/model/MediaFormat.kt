package com.voxable.feature_downloader.domain.model

/**
 * İndirilebilir medya formatı.
 */
data class MediaFormat(
    val formatId: String,
    val url: String,
    val mimeType: String,
    val extension: String,
    val quality: String,
    val resolution: String? = null,
    val bitrate: Long? = null,
    val fileSize: Long? = null,
    val codec: String? = null,
    val isAudioOnly: Boolean = false
) {
    val displayLabel: String
        get() = buildString {
            append(quality)
            resolution?.let { append(" ($it)") }
            bitrate?.let { append(" • ${it / 1000}kbps") }
            fileSize?.let { append(" • ${formatFileSize(it)}") }
        }

    private fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            bytes < 1024 * 1024 * 1024 -> "${"%.1f".format(bytes / (1024.0 * 1024.0))} MB"
            else -> "${"%.2f".format(bytes / (1024.0 * 1024.0 * 1024.0))} GB"
        }
    }
}
