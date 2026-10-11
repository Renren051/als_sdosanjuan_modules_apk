package com.jannlawm.alssdosanjuan.data.model

data class ChatRequest(
    val message: String,
    val history: List<ChatMessageItem> = emptyList()
)

data class ChatMessageItem(
    val role: String,
    val content: String
)

data class ChatResponse(
    val status: String,
    val reply: String
)
