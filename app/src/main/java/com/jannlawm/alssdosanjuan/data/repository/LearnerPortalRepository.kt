package com.jannlawm.alssdosanjuan.data.repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.jannlawm.alssdosanjuan.data.local.AppDbHelper
import com.jannlawm.alssdosanjuan.data.model.AnnouncementItem
import com.jannlawm.alssdosanjuan.data.model.EnrollmentStatusDetail
import com.jannlawm.alssdosanjuan.data.remote.ApiClient
import com.jannlawm.alssdosanjuan.data.remote.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

data class OfflineStorageStats(
    val totalFiles: Int = 0,
    val totalBytes: Long = 0L,
    val completedDeletableCount: Int = 0,
    val completedDeletableBytes: Long = 0L
)

class LearnerPortalRepository(private val context: Context) {
    private val apiService: ApiService
        get() = ApiClient.getApiService(context)
    private val appContext = context.applicationContext
    private val dbHelper = AppDbHelper(appContext)

    suspend fun fetchAnnouncements(clcId: Int? = null): Result<List<AnnouncementItem>> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.getAnnouncements(clcId = clcId, audience = "learners")
                val body = response.body()
                if (response.isSuccessful && body?.success == true) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception("Could not load announcements (${response.code()})."))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun fetchEnrollmentStatus(): Result<EnrollmentStatusDetail> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.getEnrollmentStatus()
                val body = response.body()
                if (response.isSuccessful && body?.success == true && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception("Could not load profile & enrollment details (${response.code()})."))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun uploadDocument(docType: String, fileUri: Uri): Result<String> =
        withContext(Dispatchers.IO) {
            var tempFile: File? = null
            try {
                val resolver = appContext.contentResolver
                val mimeType = resolver.getType(fileUri) ?: "image/jpeg"
                val fileName = resolveFileName(fileUri, mimeType)

                tempFile = File(appContext.cacheDir, "upload_${System.currentTimeMillis()}_$fileName")
                resolver.openInputStream(fileUri)?.use { input ->
                    FileOutputStream(tempFile).use { output -> input.copyTo(output) }
                } ?: return@withContext Result.failure(Exception("Unable to read selected file."))

                if (tempFile.length() == 0L) {
                    return@withContext Result.failure(Exception("Selected file is empty."))
                }

                val typeBody = docType.toRequestBody("text/plain".toMediaTypeOrNull())
                val fileRequestBody = tempFile.asRequestBody(mimeType.toMediaTypeOrNull())
                val part = MultipartBody.Part.createFormData("document_file", fileName, fileRequestBody)

                val response = apiService.uploadDocument(typeBody, part)
                val body = response.body()
                if (response.isSuccessful && body?.success == true) {
                    Result.success(body.message ?: "Document uploaded successfully.")
                } else {
                    val errText = response.errorBody()?.string()?.take(200)
                    Result.failure(Exception("Upload failed (${response.code()}). ${errText ?: ""}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            } finally {
                tempFile?.delete()
            }
        }

    private fun resolveFileName(uri: Uri, mimeType: String): String {
        var name: String? = null
        appContext.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (idx >= 0 && cursor.moveToFirst()) {
                name = cursor.getString(idx)
            }
        }
        val ext = when {
            mimeType.contains("pdf") -> "pdf"
            mimeType.contains("png") -> "png"
            else -> "jpg"
        }
        val clean = name?.replace(Regex("[^a-zA-Z0-9._-]"), "_") ?: "document.$ext"
        return if (clean.contains('.')) clean else "$clean.$ext"
    }

    suspend fun getStorageStats(): OfflineStorageStats = withContext(Dispatchers.IO) {
        val pdfFiles = appContext.filesDir.listFiles { f ->
            f.isFile && f.name.startsWith("module_") && f.name.endsWith(".pdf")
        } ?: emptyArray()
        val completedIds = try {
            dbHelper.getCompletedModuleIds()
        } catch (_: Exception) {
            emptySet()
        }

        var totalBytes = 0L
        var completedCount = 0
        var completedBytes = 0L

        for (file in pdfFiles) {
            val size = file.length()
            totalBytes += size
            val id = file.name.removePrefix("module_").removeSuffix(".pdf").toIntOrNull()
            if (id != null && completedIds.contains(id)) {
                completedCount++
                completedBytes += size
            }
        }

        OfflineStorageStats(
            totalFiles = pdfFiles.size,
            totalBytes = totalBytes,
            completedDeletableCount = completedCount,
            completedDeletableBytes = completedBytes
        )
    }

    suspend fun clearCompletedModules(): Int = withContext(Dispatchers.IO) {
        val completedIds = try {
            dbHelper.getCompletedModuleIds()
        } catch (_: Exception) {
            emptySet()
        }
        var deletedCount = 0
        for (id in completedIds) {
            val file = File(appContext.filesDir, "module_$id.pdf")
            if (file.exists() && file.delete()) {
                deletedCount++
            }
        }
        deletedCount
    }

    suspend fun clearAllDownloadedModules(): Int = withContext(Dispatchers.IO) {
        val pdfFiles = appContext.filesDir.listFiles { f ->
            f.isFile && f.name.startsWith("module_") && f.name.endsWith(".pdf")
        } ?: emptyArray()
        var deletedCount = 0
        for (file in pdfFiles) {
            if (file.delete()) deletedCount++
        }
        deletedCount
    }
}

