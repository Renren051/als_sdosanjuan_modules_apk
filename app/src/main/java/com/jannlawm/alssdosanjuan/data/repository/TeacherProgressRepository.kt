package com.jannlawm.alssdosanjuan.data.repository

import android.content.Context
import com.jannlawm.alssdosanjuan.data.model.TeacherModuleProgress
import com.jannlawm.alssdosanjuan.data.model.TeacherProgressMeta
import com.jannlawm.alssdosanjuan.data.remote.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TeacherProgressRepository(context: Context) {
    private val apiService = ApiClient.getApiService(context)

    data class ProgressPage(
        val rows: List<TeacherModuleProgress>,
        val meta: TeacherProgressMeta
    )

    suspend fun fetchProgress(page: Int, query: String): Result<ProgressPage> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getTeacherModuleProgress(page = page, query = query.ifBlank { null })
            val body = response.body()
            if (response.isSuccessful && body?.success == true) {
                Result.success(ProgressPage(body.data, body.meta ?: TeacherProgressMeta()))
            } else {
                Result.failure(Exception("Could not load learner progress (${response.code()})."))
            }
        } catch (error: Exception) {
            Result.failure(error)
        }
    }
}
