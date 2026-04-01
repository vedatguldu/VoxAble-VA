package com.voxable.feature_reader.domain.usecase

import com.voxable.core.util.Resource
import com.voxable.feature_reader.domain.model.Bookmark
import com.voxable.feature_reader.domain.repository.BookReaderRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ManageBookmarksUseCase @Inject constructor(
    private val repository: BookReaderRepository
) {
    suspend fun addBookmark(bookmark: Bookmark): Resource<Long> = repository.addBookmark(bookmark)
    suspend fun removeBookmark(bookmarkId: Long): Resource<Unit> = repository.removeBookmark(bookmarkId)
    suspend fun getBookmarks(documentId: String): Resource<List<Bookmark>> = repository.getBookmarks(documentId)
    fun observeBookmarks(documentId: String): Flow<List<Bookmark>> = repository.observeBookmarks(documentId)

    suspend fun add(bookmark: Bookmark): Resource<Long> = addBookmark(bookmark)
    suspend fun remove(bookmarkId: Long): Resource<Unit> = removeBookmark(bookmarkId)
    suspend fun getAll(documentId: String): Resource<List<Bookmark>> = getBookmarks(documentId)
}
