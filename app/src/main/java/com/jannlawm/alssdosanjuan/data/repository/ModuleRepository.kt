package com.jannlawm.alssdosanjuan.data.repository

import android.content.Context
import com.jannlawm.alssdosanjuan.data.local.AppDbHelper
import com.jannlawm.alssdosanjuan.data.local.CachedModuleEntity
import com.jannlawm.alssdosanjuan.data.local.TokenManager
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

    fun getCachedPdfFile(moduleId: Int): File? {
        val file = File(appContext.filesDir, "module_$moduleId.pdf")
        return if (file.exists() && file.length() > 0) file else null
    }

    fun deleteCachedPdfFile(moduleId: Int): Boolean {
        val file = File(appContext.filesDir, "module_$moduleId.pdf")
        return !file.exists() || file.delete()
    }

    suspend fun hasPlayedModuleGame(moduleId: Int): Boolean = withContext(Dispatchers.IO) {
        val learnerId = TokenManager(appContext).getUser()?.id ?: return@withContext false
        if (dbHelper.hasPlayedModuleGame(learnerId, moduleId)) return@withContext true

        val playedOnServer = try {
            val response = apiService.getModuleGameStatus(moduleId)
            response.isSuccessful && response.body()?.let { body ->
                body.success && body.data?.played == true
            } == true
        } catch (_: Exception) {
            false
        }
        if (playedOnServer) dbHelper.markModuleGamePlayed(learnerId, moduleId)
        playedOnServer
    }

    suspend fun fetchStrands(level: String? = null): Result<List<Strand>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getStrands(level)
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

    suspend fun fetchAndCacheModules(
        strandId: Int? = null,
        level: String? = null,
        subLevel: String? = null,
        language: String? = null,
        query: String? = null
    ): Result<List<ModuleItem>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getModules(strandId, level, subLevel, language, query)
                if (response.isSuccessful && response.body() != null) {
                    val modules = response.body()!!.data
                    val entities = modules.map { m ->
                        CachedModuleEntity(
                            id = m.id,
                            title = m.title,
                            description = m.description,
                            level = m.level,
                            subLevel = m.sub_level,
                            language = m.language,
                            moduleNumber = m.module_number,
                            fileSize = m.file_size,
                            strandTitle = m.strand?.title ?: m.strand?.name,
                            localPdfPath = null
                        )
                    }
                    dbHelper.insertModules(entities)
                    Result.success(modules)
                } else {
                    // Fallback to SQLite cached modules
                    val local = dbHelper.getCachedModules(level, subLevel, language, query).map { it.toModuleItem() }
                    Result.success(local)
                }
            } catch (e: Exception) {
                // Offline fallback: load from local SQLite cache!
                val local = dbHelper.getCachedModules(level, subLevel, language, query).map { it.toModuleItem() }
                Result.success(local)
            }
        }
    }

    suspend fun downloadModulePdf(moduleId: Int): Result<File> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.downloadModulePdf(moduleId, viewOnly = false)
                if (response.isSuccessful && response.body() != null) {
                    val body: ResponseBody = response.body()!!
                    val file = File(appContext.filesDir, "module_$moduleId.pdf")
                    body.byteStream().use { input ->
                        FileOutputStream(file).use { output -> input.copyTo(output) }
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

    suspend fun fetchPdfForOnlineViewing(moduleId: Int): Result<File> {
        return withContext(Dispatchers.IO) {
            val previewFile = File(appContext.cacheDir, "module_preview_$moduleId.pdf")
            try {
                val response = apiService.downloadModulePdf(moduleId, viewOnly = true)
                if (response.isSuccessful && response.body() != null) {
                    previewFile.delete()
                    response.body()!!.byteStream().use { input ->
                        FileOutputStream(previewFile).use { output -> input.copyTo(output) }
                    }
                    if (previewFile.length() > 0) {
                        Result.success(previewFile)
                    } else {
                        previewFile.delete()
                        Result.failure(Exception("The module PDF is empty."))
                    }
                } else {
                    Result.failure(Exception("Could not load module for online viewing (${response.code()})."))
                }
            } catch (error: Exception) {
                previewFile.delete()
                Result.failure(error)
            }
        }
    }
}
