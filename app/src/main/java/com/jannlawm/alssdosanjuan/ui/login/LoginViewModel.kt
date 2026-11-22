package com.jannlawm.alssdosanjuan.ui.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jannlawm.alssdosanjuan.data.local.TokenManager
import com.jannlawm.alssdosanjuan.data.model.User
import com.jannlawm.alssdosanjuan.data.remote.ApiClient
import com.jannlawm.alssdosanjuan.data.remote.Constants
import com.jannlawm.alssdosanjuan.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val loginId: String = "learner_elem",
    val password: String = "Password123!",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val loggedInUser: User? = null,
    val currentServerUrl: String = Constants.DEFAULT_BASE_URL
)

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val authRepository = AuthRepository(application)
    private val tokenManager = TokenManager(application)

    private val _uiState = MutableStateFlow(
        LoginUiState(
            loginId = "learner_elem",
            password = "Password123!",
            currentServerUrl = tokenManager.getBaseUrl()
        )
    )
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun resetState() {
        val currentServer = tokenManager.getBaseUrl()
        _uiState.update {
            it.copy(
                isLoading = false,
                errorMessage = null,
                loggedInUser = null,
                currentServerUrl = currentServer
            )
        }
    }

    fun onLoginIdChanged(id: String) {
        _uiState.update { it.copy(loginId = id, errorMessage = null) }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update { it.copy(password = password, errorMessage = null) }
    }

    fun fillCredentials(id: String, pass: String) {
        _uiState.update { it.copy(loginId = id, password = pass, errorMessage = null) }
    }

    fun setServerUrl(newUrl: String) {
        val trimmed = newUrl.trim().let { if (it.endsWith("/")) it else "$it/" }
        tokenManager.saveBaseUrl(trimmed)
        Constants.currentBaseUrl = trimmed
        ApiClient.resetClient()
        _uiState.update { it.copy(currentServerUrl = trimmed, errorMessage = null) }
    }

    fun login() {
        val currentState = _uiState.value
        if (currentState.loginId.isBlank() || currentState.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter both username/LRN and password.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = authRepository.login(currentState.loginId, currentState.password)
            result.fold(
                onSuccess = { user ->
                    _uiState.update { it.copy(isLoading = false, loggedInUser = user) }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.localizedMessage ?: "Login failed") }
                }
            )
        }
    }
}

