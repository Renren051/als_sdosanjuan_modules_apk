package com.jannlawm.alssdosanjuan.data.repository

import android.content.Context
import com.jannlawm.alssdosanjuan.data.model.ChatMessageItem
import com.jannlawm.alssdosanjuan.data.model.ChatRequest
import com.jannlawm.alssdosanjuan.data.remote.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ChatRepository(context: Context) {
    private val apiService = ApiClient.getApiService(context)

    suspend fun askAlsjay(message: String, history: List<ChatMessageItem> = emptyList()): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val request = ChatRequest(message = message, history = history)
                val response = apiService.chatWithAlsjay(request)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!.reply)
                } else {
                    Result.failure(Exception("Failed to get response from ALSJay"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
