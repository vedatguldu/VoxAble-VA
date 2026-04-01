package com.voxable.feature_converter.presentation.fileconverter

import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.voxable.core.base.BaseViewModel
import com.voxable.core.util.Resource
import com.voxable.feature_converter.domain.fileconverter.model.ConversionRequest
import com.voxable.feature_converter.domain.fileconverter.model.ConversionType
import com.voxable.feature_converter.domain.fileconverter.model.SupportedFormats
import com.voxable.feature_converter.domain.fileconverter.usecase.ConvertFileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FileConverterViewModel @Inject constructor(
    private val convertFileUseCase: ConvertFileUseCase
) : BaseViewModel<FileConverterState, FileConverterEvent>(FileConverterState()) {

    private var conversionJob: Job? = null

    fun onFileSelected(uri: Uri, fileName: String) {
        val extension = fileName.substringAfterLast('.', "").lowercase()
        val types = SupportedFormats.detectConversionTypes(extension)

        if (types.isEmpty()) {
            updateState { copy(error = "Bu dosya formatı desteklenmiyor: .$extension") }
            sendEvent(FileConverterEvent.ShowError("Bu dosya formatı desteklenmiyor"))
            return
        }

        val firstType = types.first()
        val outputFormats = SupportedFormats.getOutputFormatsFor(firstType)

        updateState {
            copy(
                sourceFileName = fileName,
                sourceFileUri = uri.toString(),
                sourceExtension = extension,
                availableConversionTypes = types,
                selectedConversionType = firstType,
                availableOutputFormats = outputFormats,
                selectedOutputFormat = outputFormats.firstOrNull { it != extension } ?: outputFormats.first(),
                conversionResult = null,
                error = null
            )
        }
    }

    fun onConversionTypeSelected(type: ConversionType) {
        val outputFormats = SupportedFormats.getOutputFormatsFor(type)
        updateState {
            copy(
                selectedConversionType = type,
                availableOutputFormats = outputFormats,
                selectedOutputFormat = outputFormats.firstOrNull { it != sourceExtension } ?: outputFormats.first(),
                conversionResult = null,
                error = null
            )
        }
    }

    fun onOutputFormatSelected(format: String) {
        updateState { copy(selectedOutputFormat = format) }
    }

    fun onStartConversion() {
        val state = currentState
        val uriString = state.sourceFileUri ?: return
        val type = state.selectedConversionType ?: return
        val fileName = state.sourceFileName ?: return

        conversionJob = viewModelScope.launch {
            val request = ConversionRequest(
                sourceUri = Uri.parse(uriString),
                sourceFileName = fileName,
                conversionType = type,
                outputFormat = state.selectedOutputFormat
            )
            convertFileUseCase(request).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        updateState { copy(isConverting = true, error = null) }
                    }
                    is Resource.Success -> {
                        updateState {
                            copy(
                                isConverting = false,
                                conversionResult = resource.data
                            )
                        }
                        val duration = resource.data.durationMs / 1000.0
                        sendEvent(FileConverterEvent.ShowSuccess(
                            "Dönüşüm tamamlandı (%.1f saniye)".format(duration)
                        ))
                    }
                    is Resource.Error -> {
                        updateState { copy(isConverting = false, error = resource.message) }
                        sendEvent(FileConverterEvent.ShowError(resource.message))
                    }
                }
            }
        }
    }

    fun onCancelConversion() {
        conversionJob?.cancel()
        convertFileUseCase.cancel()
        updateState { copy(isConverting = false) }
    }

    fun onShareResult() {
        val result = currentState.conversionResult ?: return
        val mimeType = when {
            result.outputFileName.endsWith(".pdf") -> "application/pdf"
            result.outputFileName.endsWith(".txt") -> "text/plain"
            result.outputFileName.endsWith(".mp4") -> "video/mp4"
            result.outputFileName.endsWith(".mp3") -> "audio/mpeg"
            result.outputFileName.endsWith(".aac") -> "audio/aac"
            result.outputFileName.endsWith(".wav") -> "audio/wav"
            else -> "*/*"
        }
        sendEvent(FileConverterEvent.ShareFile(result.outputUri.toString(), mimeType))
    }

    fun onReset() {
        updateState { FileConverterState() }
    }
}
