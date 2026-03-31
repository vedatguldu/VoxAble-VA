package com.voxable.feature_reader.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        BookmarkEntity::class,
        ReadingPositionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ReaderDatabase : RoomDatabase() {
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun readingPositionDao(): ReadingPositionDao
}
