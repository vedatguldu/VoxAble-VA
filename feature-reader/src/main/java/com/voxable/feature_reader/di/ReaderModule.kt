package com.voxable.feature_reader.di

import android.content.Context
import androidx.room.Room
import com.voxable.feature_reader.data.local.BookmarkDao
import com.voxable.feature_reader.data.local.ReaderDatabase
import com.voxable.feature_reader.data.local.ReadingPositionDao
import com.voxable.feature_reader.data.ocr.HybridOcrEngine
import com.voxable.feature_reader.data.parser.DocumentParserFactory
import com.voxable.feature_reader.data.repository.BookReaderRepositoryImpl
import com.voxable.feature_reader.data.repository.ReaderRepositoryImpl
import com.voxable.feature_reader.data.tts.WordTrackingTtsEngine
import com.voxable.feature_reader.domain.repository.BookReaderRepository
import com.voxable.feature_reader.domain.repository.OcrEngineRepository
import com.voxable.feature_reader.domain.repository.ReaderRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ReaderModule {

    // ─── Database ───────────────────────────────────────────────────

    @Provides
    @Singleton
    fun provideReaderDatabase(
        @ApplicationContext context: Context
    ): ReaderDatabase {
        return Room.databaseBuilder(
            context,
            ReaderDatabase::class.java,
            "voxable_reader_database"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun provideBookmarkDao(db: ReaderDatabase): BookmarkDao = db.bookmarkDao()

    @Provides
    @Singleton
    fun provideReadingPositionDao(db: ReaderDatabase): ReadingPositionDao = db.readingPositionDao()

    // ─── Engines ────────────────────────────────────────────────────

    @Provides
    @Singleton
    fun provideWordTrackingTtsEngine(
        @ApplicationContext context: Context
    ): WordTrackingTtsEngine = WordTrackingTtsEngine(context)

    @Provides
    @Singleton
    fun provideDocumentParserFactory(): DocumentParserFactory = DocumentParserFactory()

    // ─── Repositories ───────────────────────────────────────────────

    @Provides
    @Singleton
    fun provideReaderRepository(
        @ApplicationContext context: Context
    ): ReaderRepository {
        return ReaderRepositoryImpl(context)
    }

    @Provides
    @Singleton
    fun provideBookReaderRepository(
        @ApplicationContext context: Context,
        parserFactory: DocumentParserFactory,
        embeddedImageOcrExtractor: com.voxable.feature_reader.data.parser.DocumentEmbeddedImageOcrExtractor,
        ttsEngine: WordTrackingTtsEngine,
        bookmarkDao: BookmarkDao,
        readingPositionDao: ReadingPositionDao
    ): BookReaderRepository {
        return BookReaderRepositoryImpl(
            context,
            parserFactory,
            embeddedImageOcrExtractor,
            ttsEngine,
            bookmarkDao,
            readingPositionDao
        )
    }

    @Provides
    @Singleton
    fun provideOcrEngineRepository(
        @ApplicationContext context: Context
    ): OcrEngineRepository {
        return HybridOcrEngine(context)
    }
}
