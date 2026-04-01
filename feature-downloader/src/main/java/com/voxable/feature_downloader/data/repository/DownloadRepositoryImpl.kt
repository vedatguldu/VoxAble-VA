package com.voxable.feature_downloader.data.repository

import android.content.Context
import android.os.Environment
import com.voxable.core.util.Resource
import com.voxable.core_database.dao.DownloadDao
import com.voxable.core_database.entity.DownloadEntity
import com.voxable.feature_downloader.data.detector.MediaDetector
import com.voxable.feature_downloader.domain.model.MediaInfo
import com.voxable.feature_downloader.domain.repository.DownloadRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val okHttpClient: OkHttpClient,
    private val downloadDao: DownloadDao,
    private val mediaDetector: MediaDetector
) : DownloadRepository {

    override suspend fun detectMedia(url: String): Resource<MediaInfo> {
        return withContext(Dispatchers.IO) {
            try {
                val info = mediaDetector.detect(url)
                Resource.Success(info)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Medya algılanamadı")
            }
        }
    }

    override suspend fun startDownload(url: String, fileName: String): Resource<Long> {
        return withContext(Dispatchers.IO) {
            try {
                val downloadsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                    ?: return@withContext Resource.Error("İndirme dizini bulunamadı")

                val file = File(downloadsDir, fileName)

                // Veritabanına kaydet
                val entity = DownloadEntity(
                    url = url,
                    fileName = fileName,
                    filePath = file.absolutePath,
                    mimeType = "application/octet-stream",
                    fileSize = 0L,
                    downloadedSize = 0L,
                    status = "downloading"
                )
                val id = downloadDao.insert(entity)

                // HTTP isteği yap
                val request = Request.Builder().url(url).build()
                val response = okHttpClient.newCall(request).execute()

                if (!response.isSuccessful) {
                    downloadDao.updateStatus(id, "failed")
                    return@withContext Resource.Error("İndirme başarısız: HTTP ${response.code}")
                }

                val body = response.body ?: run {
                    downloadDao.updateStatus(id, "failed")
                    return@withContext Resource.Error("Boş yanıt alındı")
                }

                val totalBytes = body.contentLength()
                downloadDao.updateProgress(id, 0L, totalBytes)

                FileOutputStream(file).use { output ->
                    body.byteStream().use { input ->
                        val buffer = ByteArray(8192)
                        var bytesRead: Long = 0

                        var len: Int
                        while (input.read(buffer).also { len = it } != -1) {
                            output.write(buffer, 0, len)
                            bytesRead += len
                            downloadDao.updateProgress(id, bytesRead, totalBytes)
                        }
                    }
                }

                downloadDao.updateStatus(id, "completed")
                Resource.Success(id)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "İndirme sırasında hata oluştu")
            }
        }
    }

    override suspend fun cancelDownload(downloadId: Long): Resource<Unit> {
        return try {
            downloadDao.updateStatus(downloadId, "cancelled")
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "İptal edilemedi")
        }
    }

    override fun observeProgress(downloadId: Long): Flow<Float> = flow {
        // DownloadDao'dan izleme yapılır
        downloadDao.getDownloadById(downloadId)?.let { entity ->
            if (entity.fileSize > 0) {
                emit(entity.downloadedSize.toFloat() / entity.fileSize.toFloat())
            } else {
                emit(0f)
            }
        }
    }

    override suspend fun getActiveDownloads(): Resource<List<Long>> {
        return try {
            val downloads = downloadDao.getDownloadsByStatus("downloading")
            Resource.Success(downloads.map { it.id })
        } catch (e: Exception) {
            Resource.Error(e.message ?: "İndirmeler yüklenemedi")
        }
    }
}
