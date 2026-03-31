package com.voxable.feature_reader.domain.model

data class Bookmark(
    val id: Long = 0,
    val documentId: String,
    val chapterIndex: Int,
    val position: Int = 0,
    val title: String,
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

data class ReadingPosition(
    val documentId: String,
    val chapterIndex: Int,
    val scrollPosition: Int = 0,
    val lastReadAt: Long = System.currentTimeMillis()
)
