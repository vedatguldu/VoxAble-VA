package com.voxable.feature_settings.domain.model

enum class TouchTargetSize(
    val label: String,
    val minSizeDp: Int
) {
    NORMAL("Normal", 48),
    LARGE("Büyük", 56),
    EXTRA_LARGE("Çok Büyük", 64);

    companion object {
        fun fromName(name: String): TouchTargetSize =
            entries.find { it.name == name } ?: NORMAL
    }
}
