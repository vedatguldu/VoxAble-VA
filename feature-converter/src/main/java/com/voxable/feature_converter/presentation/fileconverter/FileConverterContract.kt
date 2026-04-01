package com.voxable.feature_converter.presentation.fileconverter

import com.voxable.core.base.UiEvent
import com.voxable.core.base.UiState
import com.voxable.feature_converter.domain.fileconverter.model.ConversionResult
import com.voxable.feature_converter.domain.fileconverter.model.ConversionType

data class FileConverterState(
    val sourceFileName: String? = null,
    val sourceFileUri: String? = null,
    val sourceExtension: String = "",
    val availableConversionTypes: List<ConversionType> = emptyList(),
    val selectedConversionType: ConversionType? = null,
    val availableOutputFormats: List<String> = emptyList(),
    val selectedOutputFormat: String = "",
    val isConverting: Boolean = false,
    val conversionResult: ConversionResult? = null,
    val error: String? = null
) : UiState

sealed interface FileConverterEvent : UiEvent {
    data class ShowError(val message: String) : FileConverterEvent
    data class ShowSuccess(val message: String) : FileConverterEvent
    data class ShareFile(val uri: String, val mimeType: String) : FileConverterEvent
}
