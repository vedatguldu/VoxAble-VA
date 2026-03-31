package com.voxable.feature_reader.domain.usecase

import com.voxable.core.util.Resource
import com.voxable.feature_reader.domain.repository.ReaderRepository
import javax.inject.Inject

class ReadTextUseCase @Inject constructor(
    private val readerRepository: ReaderRepository
) {
    suspend operator fun invoke(text: String): Resource<Unit> {
        if (text.isBlank()) return Resource.Error("Okunacak metin boş olamaz")
        return readerRepository.textToSpeech(text)
    }
}
