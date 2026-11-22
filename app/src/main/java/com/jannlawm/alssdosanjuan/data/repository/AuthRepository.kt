package com.jannlawm.alssdosanjuan.data.repository

import android.content.Context
import com.jannlawm.alssdosanjuan.data.local.TokenManager
import com.jannlawm.alssdosanjuan.data.model.LoginRequest
import com.jannlawm.alssdosanjuan.data.model.User
import com.jannlawm.alssdosanjuan.data.remote.ApiClient
import com.jannlawm.alssdosanjuan.data.remote.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class AuthRepository(private val context: Context) {
    private val apiService: ApiService
        get() = ApiClient.getApiService(context)
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
                        Result.failure(Exception("Login failed. Please verify your credentials."))
                    }
                } else {
                    val errorMsg = try {
                        val rawErr = response.errorBody()?.string()
                        val json = if (!rawErr.isNullOrBlank()) org.json.JSONObject(rawErr) else null
                        val parsed = when {
                            json != null && json.has("message") -> json.getString("message")
                            json != null && json.has("error") -> json.getString("error")
                            else -> null
                        }
                        parsed ?: when (response.code()) {
                            422 -> "Invalid credentials. The provided username/LRN and password do not match our records."
                            401 -> "Invalid credentials. Please verify your username/LRN and password."
                            404 -> "Endpoint not found (404). Please verify server address."
                            500, 502, 503 -> "Server error (${response.code()}). The backend service may be restarting."
                            else -> "Invalid credentials or server error (${response.code()})"
                        }
                    } catch (e: Exception) {
                        when (response.code()) {
                            422 -> "Invalid credentials. The provided username/LRN and password do not match our records."
                            401 -> "Invalid credentials. Please verify your username/LRN and password."
                            404 -> "Endpoint not found (404). Please verify server address."
                            500, 502, 503 -> "Server error (${response.code()}). The backend service may be restarting."
                            else -> "Invalid credentials or server error (${response.code()})"
                        }
                    }
                    Result.failure(Exception(errorMsg))
                }
            } catch (e: Exception) {
                val currentUrl = tokenManager.getBaseUrl()
                val friendlyMsg = when (e) {
                    is ConnectException -> "Cannot connect to server ($currentUrl). If testing locally, ensure 'php artisan serve --host=0.0.0.0' is running on your PC, or switch server to Live Cloud."
                    is SocketTimeoutException -> "Connection timed out ($currentUrl). If using Live Cloud, the server may be waking up (please retry). If local, check PC IP address."
                    is UnknownHostException -> "Unknown host. Please check your internet connection and server address."
                    else -> e.localizedMessage ?: "Network connection error occurred."
                }
                Result.failure(Exception(friendlyMsg))
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

