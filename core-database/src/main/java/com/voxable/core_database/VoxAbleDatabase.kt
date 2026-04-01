package com.voxable.core_database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.voxable.core_database.converter.Converters
import com.voxable.core_database.dao.DownloadDao
import com.voxable.core_database.dao.SyncQueueDao
import com.voxable.core_database.dao.SyncStatusDao
import com.voxable.core_database.dao.UserDao
import com.voxable.core_database.entity.DownloadEntity
import com.voxable.core_database.entity.SyncQueueEntity
import com.voxable.core_database.entity.SyncStatusEntity
import com.voxable.core_database.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        DownloadEntity::class,
        SyncQueueEntity::class,
        SyncStatusEntity::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class VoxAbleDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun downloadDao(): DownloadDao
    abstract fun syncQueueDao(): SyncQueueDao
    abstract fun syncStatusDao(): SyncStatusDao

    companion object {

        /**
         * Version 1 → 2: sync_queue ve sync_status tablolarını ekler.
         *
         * Kolon tiplerinde Room'un beklediği SQLite karşılıkları:
         *   Long  → INTEGER, String → TEXT, Boolean → INTEGER (0/1)
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // ── sync_queue ─────────────────────────────────
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `sync_queue` (
                        `id`                 INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `entityType`         TEXT    NOT NULL,
                        `entityId`           TEXT    NOT NULL,
                        `operation`          TEXT    NOT NULL,
                        `payload`            TEXT    NOT NULL,
                        `syncStatus`         TEXT    NOT NULL DEFAULT 'PENDING',
                        `retryCount`         INTEGER NOT NULL DEFAULT 0,
                        `maxRetries`         INTEGER NOT NULL DEFAULT 3,
                        `lastAttemptAt`      INTEGER,
                        `nextRetryAt`        INTEGER,
                        `errorMessage`       TEXT,
                        `conflictResolution` TEXT    NOT NULL DEFAULT 'LATEST_WINS',
                        `version`            INTEGER NOT NULL DEFAULT 0,
                        `createdAt`          INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )

                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_sync_queue_entityType_entityId` " +
                        "ON `sync_queue` (`entityType`, `entityId`)"
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_sync_queue_syncStatus` " +
                        "ON `sync_queue` (`syncStatus`)"
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_sync_queue_nextRetryAt` " +
                        "ON `sync_queue` (`nextRetryAt`)"
                )

                // ── sync_status ────────────────────────────────
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `sync_status` (
                        `entityType`    TEXT    PRIMARY KEY NOT NULL,
                        `lastSyncAt`    INTEGER NOT NULL DEFAULT 0,
                        `lastSuccessAt` INTEGER NOT NULL DEFAULT 0,
                        `lastError`     TEXT,
                        `pendingCount`  INTEGER NOT NULL DEFAULT 0,
                        `isSyncing`     INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )
            }
        }
    }
}
