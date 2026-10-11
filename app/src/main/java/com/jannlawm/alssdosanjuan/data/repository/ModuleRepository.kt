package com.jannlawm.alssdosanjuan.data.repository

import android.content.Context
import com.jannlawm.alssdosanjuan.data.local.AppDbHelper
import com.jannlawm.alssdosanjuan.data.local.CachedModuleEntity
import com.jannlawm.alssdosanjuan.data.model.ModuleItem
import com.jannlawm.alssdosanjuan.data.model.Strand
import com.jannlawm.alssdosanjuan.data.remote.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import java.io.File
import java.io.FileOutputStream

class ModuleRepository(context: Context) {
    private val apiService = ApiClient.getApiService(context)
    private val dbHelper = AppDbHelper(context)
    private val appContext = context.applicationContext

    suspend fun fetchStrands(): Result<List<Strand>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getStrands()
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!.data)
                } else {
                    Result.failure(Exception("Failed to fetch strands"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun fetchAndCacheModules(strandId: Int?, level: String?, query: String?): Result<List<ModuleItem>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getModules(strandId, level, query)
                if (response.isSuccessful && response.body() != null) {
                    val modules = response.body()!!.data
                    val entities = modules.map { m ->
                        CachedModuleEntity(
                            id = m.id,
                            title = m.title,
                            description = m.description,
                            level = m.level,
                            moduleNumber = m.module_number,
                            fileSize = m.file_size,
                            strandTitle = m.strand?.title,
                            localPdfPath = null
                        )
                    }
                    dbHelper.insertModules(entities)
                    Result.success(modules)
                } else {
                    Result.failure(Exception("Failed to fetch modules"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun downloadModulePdf(moduleId: Int): Result<File> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.downloadModulePdf(moduleId)
                if (response.isSuccessful && response.body() != null) {
                    val body: ResponseBody = response.body()!!
                    val file = File(appContext.filesDir, "module_$moduleId.pdf")
                    val inputStream = body.byteStream()
                    val outputStream = FileOutputStream(file)
                    inputStream.use { input ->
                        outputStream.use { output ->
                            input.copyTo(output)
                        }
                    }
                    Result.success(file)
                } else {
                    Result.failure(Exception("Failed to download PDF"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
