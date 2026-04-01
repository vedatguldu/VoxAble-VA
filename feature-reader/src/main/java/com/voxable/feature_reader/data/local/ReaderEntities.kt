package com.voxable.feature_reader.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "bookmarks",
    indices = [Index(value = ["documentId"])]
)
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val documentId: String,
    val chapterIndex: Int,
    val pageIndex: Int = 0,
    val characterOffset: Int = 0,
    val title: String,
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "reading_positions",
    indices = [Index(value = ["documentId"], unique = true)]
)
data class ReadingPositionEntity(
    @PrimaryKey val documentId: String,
    val chapterIndex: Int,
    val pageIndex: Int = 0,
    val characterOffset: Int = 0,
    val progressPercent: Float = 0f,
    val updatedAt: Long = System.currentTimeMillis()
)
