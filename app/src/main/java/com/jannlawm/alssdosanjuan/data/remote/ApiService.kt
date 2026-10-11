package com.jannlawm.alssdosanjuan.data.remote

import com.jannlawm.alssdosanjuan.data.model.*
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // 1. Authentication
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("auth/me")
    suspend fun getMe(): Response<UserProfileResponse>

    @POST("auth/logout")
    suspend fun logout(): Response<Unit>

    // 2. Curriculum & Modules
    @GET("strands")
    suspend fun getStrands(@Query("level") level: String? = null): Response<StrandsResponse>

    @GET("modules")
    suspend fun getModules(
        @Query("strand_id") strandId: Int? = null,
        @Query("level") level: String? = null,
        @Query("sub_level") subLevel: String? = null,
        @Query("language") language: String? = null,
        @Query("q") query: String? = null
    ): Response<ModulesResponse>

    @GET("modules/{id}/download")
    @Streaming
    suspend fun downloadModulePdf(
        @Path("id") moduleId: Int,
        @Query("view_only") viewOnly: Boolean = false
    ): Response<ResponseBody>

    // 3. Offline Telemetry Sync
    @POST("learner/sync-progress")
    suspend fun syncProgress(@Body request: SyncProgressRequest): Response<SyncProgressResponse>

    @POST("learner/sync-game-scores")
    suspend fun syncGameScores(@Body request: SyncGameScoresRequest): Response<SyncGameScoresResponse>

    @GET("learner/modules/{id}/game-status")
    suspend fun getModuleGameStatus(@Path("id") moduleId: Int): Response<ModuleGameStatusResponse>

    @GET("teacher/module-progress")
    suspend fun getTeacherModuleProgress(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 100,
        @Query("q") query: String? = null
    ): Response<TeacherModuleProgressResponse>

}
