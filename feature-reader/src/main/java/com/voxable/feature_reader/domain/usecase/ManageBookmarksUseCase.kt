package com.voxable.feature_reader.domain.usecase

import com.voxable.feature_reader.domain.model.Bookmark
import com.voxable.feature_reader.domain.repository.BookReaderRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ManageBookmarksUseCase @Inject constructor(
    private val repository: BookReaderRepository
) {
    suspend fun addBookmark(bookmark: Bookmark) = repository.addBookmark(bookmark)
    suspend fun removeBookmark(bookmarkId: Long) = repository.removeBookmark(bookmarkId)
    fun getBookmarks(documentId: String): Flow<List<Bookmark>> = repository.getBookmarks(documentId)
}
