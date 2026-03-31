package com.voxable.core_database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.voxable.core_database.converter.Converters
import com.voxable.core_database.dao.UserDao
import com.voxable.core_database.dao.DownloadDao
import com.voxable.core_database.entity.UserEntity
import com.voxable.core_database.entity.DownloadEntity

@Database(
    entities = [
        UserEntity::class,
        DownloadEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class VoxAbleDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun downloadDao(): DownloadDao
}
