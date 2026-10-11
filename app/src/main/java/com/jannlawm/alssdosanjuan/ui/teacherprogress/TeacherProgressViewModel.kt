package com.jannlawm.alssdosanjuan.ui.teacherprogress

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jannlawm.alssdosanjuan.data.model.TeacherModuleProgress
import com.jannlawm.alssdosanjuan.data.repository.TeacherProgressRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TeacherProgressUiState(
    val rows: List<TeacherModuleProgress> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val currentPage: Int = 1,
    val lastPage: Int = 1,
    val totalRecords: Int = 0,
    val errorMessage: String? = null
)

class TeacherProgressViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = TeacherProgressRepository(application)
    private val _uiState = MutableStateFlow(TeacherProgressUiState())
    val uiState: StateFlow<TeacherProgressUiState> = _uiState.asStateFlow()
    private var requestJob: Job? = null

    fun refresh(query: String = "", isPullToRefresh: Boolean = false) {
        requestJob?.cancel()
        requestJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    rows = emptyList(),
                    isLoading = !isPullToRefresh,
                    isRefreshing = isPullToRefresh,
                    isLoadingMore = false,
                    currentPage = 1,
                    errorMessage = null
                )
            }
            repository.fetchProgress(page = 1, query = query).fold(
                onSuccess = { page ->
                    _uiState.update {
                        it.copy(
                            rows = page.rows,
                            currentPage = page.meta.current_page,
                            lastPage = page.meta.last_page,
                            totalRecords = page.meta.total,
                            isLoading = false,
                            isRefreshing = false
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = error.localizedMessage ?: "Could not load learner progress."
                        )
                    }
                }
            )
        }
    }

    fun loadMore(query: String = "") {
        val state = _uiState.value
        if (state.isLoadingMore || state.isLoading || state.currentPage >= state.lastPage) return
        requestJob?.cancel()
        requestJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true, errorMessage = null) }
            repository.fetchProgress(page = state.currentPage + 1, query = query).fold(
                onSuccess = { page ->
                    _uiState.update {
                        it.copy(
                            rows = it.rows + page.rows,
                            currentPage = page.meta.current_page,
                            lastPage = page.meta.last_page,
                            totalRecords = page.meta.total,
                            isLoadingMore = false
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(isLoadingMore = false, errorMessage = error.localizedMessage ?: "Could not load more progress.")
                    }
                }
            )
        }
    }
}
