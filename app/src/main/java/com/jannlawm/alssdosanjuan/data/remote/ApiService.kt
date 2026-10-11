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
    suspend fun getStrands(): Response<StrandsResponse>

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
    suspend fun downloadModulePdf(@Path("id") moduleId: Int): Response<ResponseBody>

    // 3. Offline Telemetry Sync
    @POST("learner/sync-progress")
    suspend fun syncProgress(@Body request: SyncProgressRequest): Response<SyncProgressResponse>

    // 4. ALSJay AI Assistant
    @POST("chat")
    suspend fun chatWithAlsjay(@Body request: ChatRequest): Response<ChatResponse>
}
