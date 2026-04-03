package com.voxable.feature_reader.domain.model

data class Bookmark(
    val id: Long = 0,
    val documentId: String,
    val chapterIndex: Int,
    val pageIndex: Int = 0,
    val characterOffset: Int = 0,
    val title: String,
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

data class ReadingPosition(
    val documentId: String,
    val chapterIndex: Int,
    val pageIndex: Int = 0,
    val characterOffset: Int = 0,
    val progressPercent: Float = 0f,
    val updatedAt: Long = System.currentTimeMillis()
)

data class Highlight(
    val id: Long = 0,
    val documentId: String,
    val chapterIndex: Int,
    val startOffset: Int,
    val endOffset: Int,
    val highlightedText: String,
    val color: HighlightColor = HighlightColor.YELLOW,
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class HighlightColor(val argb: Int) {
    YELLOW(0x80FFEB3B.toInt()),
    GREEN(0x804CAF50.toInt()),
    BLUE(0x802196F3.toInt()),
    PINK(0x80E91E63.toInt()),
    PURPLE(0x809C27B0.toInt());

    companion object {
        fun fromArgb(argb: Int): HighlightColor =
            entries.firstOrNull { it.argb == argb } ?: YELLOW
    }
}
