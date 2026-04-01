package com.voxable.feature_reader.domain.usecase

import android.net.Uri
import com.voxable.core.util.Resource
import com.voxable.feature_reader.domain.model.BookDocument
import com.voxable.feature_reader.domain.repository.BookReaderRepository
import javax.inject.Inject

class OpenDocumentUseCase @Inject constructor(
    private val repository: BookReaderRepository
) {
    suspend operator fun invoke(uri: Uri, fileName: String? = null): Resource<BookDocument> {
        return repository.openDocument(uri)
    }
}
