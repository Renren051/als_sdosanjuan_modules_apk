package com.jannlawm.alssdosanjuan.data.repository

import android.content.Context
import com.jannlawm.alssdosanjuan.data.local.TokenManager
import com.jannlawm.alssdosanjuan.data.model.LoginRequest
import com.jannlawm.alssdosanjuan.data.model.User
import com.jannlawm.alssdosanjuan.data.remote.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(private val context: Context) {
    private val apiService = ApiClient.getApiService(context)
    private val tokenManager = TokenManager(context)

    suspend fun login(loginId: String, password: String): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                val request = LoginRequest(login = loginId, password = password, device_name = "Android-LMS")
                val response = apiService.login(request)
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body.success) {
                        tokenManager.saveToken(body.data.token)
                        tokenManager.saveUserRole(body.data.user.role)
                        tokenManager.saveUser(body.data.user)
                        Result.success(body.data.user)
                    } else {
                        Result.failure(Exception("Login failed"))
                    }
                } else {
                    Result.failure(Exception("Invalid credentials or server error (${response.code()})"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getMe(): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getMe()
                if (response.isSuccessful && response.body() != null) {
                    val user = response.body()!!.data
                    tokenManager.saveUser(user)
                    Result.success(user)
                } else {
                    Result.failure(Exception("Failed to fetch profile"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    fun logout() {
        // Module metadata is public catalog data used to find downloaded PDFs offline.
        tokenManager.clear()
    }
}
