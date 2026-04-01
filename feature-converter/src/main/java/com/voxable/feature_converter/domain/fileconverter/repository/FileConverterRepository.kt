package com.voxable.feature_converter.domain.fileconverter.repository

import com.voxable.core.util.Resource
import com.voxable.feature_converter.domain.fileconverter.model.ConversionRequest
import com.voxable.feature_converter.domain.fileconverter.model.ConversionResult
import kotlinx.coroutines.flow.Flow

interface FileConverterRepository {
    fun convertFile(request: ConversionRequest): Flow<Resource<ConversionResult>>
    fun cancelConversion()
}
