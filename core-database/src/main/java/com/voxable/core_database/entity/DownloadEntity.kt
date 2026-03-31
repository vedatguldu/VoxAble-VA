package com.voxable.core_database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val url: String,
    val fileName: String,
    val filePath: String,
    val fileSize: Long = 0,
    val downloadedSize: Long = 0,
    val status: String = "pending", // pending, downloading, completed, failed
    val mimeType: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
