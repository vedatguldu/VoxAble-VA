package com.voxable.feature_converter.data.repository

import android.content.Context
import android.net.Uri
import com.voxable.core.util.Resource
import com.voxable.feature_converter.data.engine.FFmpegEngine
import com.voxable.feature_converter.data.engine.ImageOcrEngine
import com.voxable.feature_converter.data.engine.PdfEngine
import com.voxable.feature_converter.domain.fileconverter.model.ConversionRequest
import com.voxable.feature_converter.domain.fileconverter.model.ConversionResult
import com.voxable.feature_converter.domain.fileconverter.model.ConversionType
import com.voxable.feature_converter.domain.fileconverter.repository.FileConverterRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileConverterRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val ffmpegEngine: FFmpegEngine,
    private val pdfEngine: PdfEngine,
    private val imageOcrEngine: ImageOcrEngine
) : FileConverterRepository {

    override fun convertFile(request: ConversionRequest): Flow<Resource<ConversionResult>> = flow {
        emit(Resource.Loading)
        try {
            val startTime = System.currentTimeMillis()
            val result = when (request.conversionType) {
                ConversionType.VIDEO_TO_AUDIO -> {
                    val outputFile = ffmpegEngine.extractAudio(request.sourceUri, request.outputFormat)
                    ConversionResult(
                        outputUri = Uri.fromFile(outputFile),
                        outputFileName = outputFile.name,
                        conversionType = request.conversionType,
                        durationMs = System.currentTimeMillis() - startTime
                    )
                }

                ConversionType.AUDIO_TO_VIDEO -> {
                    val outputFile = ffmpegEngine.audioToVideo(request.sourceUri, request.outputFormat)
                    ConversionResult(
                        outputUri = Uri.fromFile(outputFile),
                        outputFileName = outputFile.name,
                        conversionType = request.conversionType,
                        durationMs = System.currentTimeMillis() - startTime
                    )
                }

                ConversionType.VIDEO_FORMAT -> {
                    val outputFile = ffmpegEngine.convertVideoFormat(request.sourceUri, request.outputFormat)
                    ConversionResult(
                        outputUri = Uri.fromFile(outputFile),
                        outputFileName = outputFile.name,
                        conversionType = request.conversionType,
                        durationMs = System.currentTimeMillis() - startTime
                    )
                }

                ConversionType.AUDIO_FORMAT -> {
                    val outputFile = ffmpegEngine.convertAudioFormat(request.sourceUri, request.outputFormat)
                    ConversionResult(
                        outputUri = Uri.fromFile(outputFile),
                        outputFileName = outputFile.name,
                        conversionType = request.conversionType,
                        durationMs = System.currentTimeMillis() - startTime
                    )
                }

                ConversionType.PDF_TO_TEXT -> {
                    val (text, outputFile) = pdfEngine.pdfToText(request.sourceUri)
                    ConversionResult(
                        outputUri = Uri.fromFile(outputFile),
                        outputFileName = outputFile.name,
                        conversionType = request.conversionType,
                        extractedText = text,
                        durationMs = System.currentTimeMillis() - startTime
                    )
                }

                ConversionType.TEXT_TO_PDF -> {
                    val outputFile = pdfEngine.textToPdf(request.sourceUri)
                    ConversionResult(
                        outputUri = Uri.fromFile(outputFile),
                        outputFileName = outputFile.name,
                        conversionType = request.conversionType,
                        durationMs = System.currentTimeMillis() - startTime
                    )
                }

                ConversionType.IMAGE_TO_TEXT -> {
                    val (text, outputFile) = imageOcrEngine.extractText(request.sourceUri)
                    ConversionResult(
                        outputUri = Uri.fromFile(outputFile),
                        outputFileName = outputFile.name,
                        conversionType = request.conversionType,
                        extractedText = text,
                        durationMs = System.currentTimeMillis() - startTime
                    )
                }
            }
            emit(Resource.Success(result))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Dönüşüm sırasında hata oluştu"))
        }
    }

    override fun cancelConversion() {
        ffmpegEngine.cancel()
    }
}
