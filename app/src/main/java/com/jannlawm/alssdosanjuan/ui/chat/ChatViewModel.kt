package com.jannlawm.alssdosanjuan.ui.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jannlawm.alssdosanjuan.data.model.ChatMessageItem
import com.jannlawm.alssdosanjuan.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatMessage(
    val sender: String, // "user" or "alsjay"
    val text: String
)

data class ChatUiState(
    val messages: List<ChatMessage> = listOf(
        ChatMessage("alsjay", "Mabuhay! Ako si ALSJay, ang iyong DepEd SDO San Juan virtual assistant. Paano kita matutulungan ngayon sa iyong pag-aaral sa ALS?")
    ),
    val currentInput: String = "",
    val isLoading: Boolean = false
)

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val chatRepository = ChatRepository(application)

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    fun onInputChanged(text: String) {
        _uiState.update { it.copy(currentInput = text) }
    }

    fun sendMessage() {
        val input = _uiState.value.currentInput.trim()
        if (input.isBlank()) return

        val userMessage = ChatMessage("user", input)
        val history = _uiState.value.messages.map {
            ChatMessageItem(role = if (it.sender == "user") "user" else "assistant", content = it.text)
        }

        _uiState.update {
            it.copy(
                messages = it.messages + userMessage,
                currentInput = "",
                isLoading = true
            )
        }

        viewModelScope.launch {
            val result = chatRepository.askAlsjay(input, history)
            result.fold(
                onSuccess = { reply ->
                    val assistantMessage = ChatMessage("alsjay", reply)
                    _uiState.update {
                        it.copy(
                            messages = it.messages + assistantMessage,
                            isLoading = false
                        )
                    }
                },
                onFailure = { error ->
                    val errorMessage = ChatMessage("alsjay", "Pasensya na, may problema sa koneksyon. (${error.localizedMessage})")
                    _uiState.update {
                        it.copy(
                            messages = it.messages + errorMessage,
                            isLoading = false
                        )
                    }
                }
            )
        }
    }
}
