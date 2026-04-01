package com.voxable.feature_converter.domain.fileconverter.usecase

import com.voxable.core.util.Resource
import com.voxable.feature_converter.domain.fileconverter.model.ConversionRequest
import com.voxable.feature_converter.domain.fileconverter.model.ConversionResult
import com.voxable.feature_converter.domain.fileconverter.repository.FileConverterRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ConvertFileUseCase @Inject constructor(
    private val repository: FileConverterRepository
) {
    operator fun invoke(request: ConversionRequest): Flow<Resource<ConversionResult>> {
        return repository.convertFile(request)
    }

    fun cancel() {
        repository.cancelConversion()
    }
}
