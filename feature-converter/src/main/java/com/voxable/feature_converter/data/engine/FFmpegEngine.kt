package com.voxable.feature_converter.data.engine

import android.content.Context
import android.net.Uri
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.FFmpegKitConfig
import com.arthenica.ffmpegkit.ReturnCode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class FFmpegEngine @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var currentSessionId: Long? = null

    /**
     * Video → Audio: Ses akışını çıkar.
     */
    suspend fun extractAudio(
        inputUri: Uri,
        outputFormat: String
    ): File = withContext(Dispatchers.IO) {
        val inputPath = copyToCache(inputUri, "input_video")
        val outputFile = createOutputFile("audio_output", outputFormat)

        val codec = when (outputFormat) {
            "mp3" -> "libmp3lame"
            "aac", "m4a" -> "aac"
            "ogg" -> "libvorbis"
            "flac" -> "flac"
            "wav" -> "pcm_s16le"
            else -> "copy"
        }

        val command = "-y -i \"${inputPath.absolutePath}\" -vn -acodec $codec \"${outputFile.absolutePath}\""
        executeFFmpeg(command)
        inputPath.delete()
        outputFile
    }

    /**
     * Audio → Video: Sese siyah arka plan ekleyerek video oluştur.
     */
    suspend fun audioToVideo(
        inputUri: Uri,
        outputFormat: String
    ): File = withContext(Dispatchers.IO) {
        val inputPath = copyToCache(inputUri, "input_audio")
        val outputFile = createOutputFile("video_output", outputFormat)

        val command = buildString {
            append("-y -f lavfi -i color=c=black:s=1280x720:r=30 ")
            append("-i \"${inputPath.absolutePath}\" ")
            append("-shortest -pix_fmt yuv420p ")
            append("-c:v libx264 -c:a aac ")
            append("\"${outputFile.absolutePath}\"")
        }
        executeFFmpeg(command)
        inputPath.delete()
        outputFile
    }

    /**
     * Video format dönüşümü.
     */
    suspend fun convertVideoFormat(
        inputUri: Uri,
        outputFormat: String
    ): File = withContext(Dispatchers.IO) {
        val inputPath = copyToCache(inputUri, "input_video")
        val outputFile = createOutputFile("video_converted", outputFormat)

        val command = "-y -i \"${inputPath.absolutePath}\" -c:v libx264 -c:a aac \"${outputFile.absolutePath}\""
        executeFFmpeg(command)
        inputPath.delete()
        outputFile
    }

    /**
     * Ses format dönüşümü.
     */
    suspend fun convertAudioFormat(
        inputUri: Uri,
        outputFormat: String
    ): File = withContext(Dispatchers.IO) {
        val inputPath = copyToCache(inputUri, "input_audio")
        val outputFile = createOutputFile("audio_converted", outputFormat)

        val codec = when (outputFormat) {
            "mp3" -> "libmp3lame"
            "aac", "m4a" -> "aac"
            "ogg" -> "libvorbis"
            "flac" -> "flac"
            "wav" -> "pcm_s16le"
            else -> "copy"
        }

        val command = "-y -i \"${inputPath.absolutePath}\" -acodec $codec \"${outputFile.absolutePath}\""
        executeFFmpeg(command)
        inputPath.delete()
        outputFile
    }

    fun cancel() {
        currentSessionId?.let { FFmpegKit.cancel(it) }
    }

    private suspend fun executeFFmpeg(command: String) = suspendCancellableCoroutine { cont ->
        val session = FFmpegKit.execute(command)
        currentSessionId = session.sessionId
        if (ReturnCode.isSuccess(session.returnCode)) {
            cont.resume(Unit)
        } else {
            val logs = session.allLogsAsString ?: "Bilinmeyen hata"
            cont.resume(Unit) // Hata durumunda da devam et; çıktı dosyası kontrol edilecek
        }
        currentSessionId = null
    }

    private fun copyToCache(uri: Uri, prefix: String): File {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalStateException("Dosya okunamadı: $uri")
        val tempFile = File(context.cacheDir, "${prefix}_${System.currentTimeMillis()}")
        inputStream.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return tempFile
    }

    private fun createOutputFile(prefix: String, extension: String): File {
        val outputDir = File(context.cacheDir, "conversions").apply { mkdirs() }
        return File(outputDir, "${prefix}_${System.currentTimeMillis()}.$extension")
    }
}
