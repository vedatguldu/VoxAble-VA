package com.voxable.feature_reader.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        BookmarkEntity::class,
        ReadingPositionEntity::class,
        HighlightEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class ReaderDatabase : RoomDatabase() {
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun readingPositionDao(): ReadingPositionDao
    abstract fun highlightDao(): HighlightDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS highlights (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        documentId TEXT NOT NULL,
                        chapterIndex INTEGER NOT NULL,
                        startOffset INTEGER NOT NULL,
                        endOffset INTEGER NOT NULL,
                        highlightedText TEXT NOT NULL,
                        color INTEGER NOT NULL,
                        note TEXT,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_highlights_documentId ON highlights(documentId)")
            }
        }
    }
}
