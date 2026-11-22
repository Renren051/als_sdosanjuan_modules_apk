package com.jannlawm.alssdosanjuan.ui.teacherprogress

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jannlawm.alssdosanjuan.data.model.TeacherModuleProgress
import com.jannlawm.alssdosanjuan.data.model.TeacherStudentItem
import com.jannlawm.alssdosanjuan.data.repository.TeacherProgressRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TeacherProgressUiState(
    val students: List<TeacherStudentItem> = emptyList(),
    val rawStudents: List<TeacherStudentItem> = emptyList(),
    val rows: List<TeacherModuleProgress> = emptyList(),
    val selectedTab: Int = 0, // 0 = "My Students", 1 = "Module Telemetry"
    val selectedStudentId: Int? = null,
    val selectedStudentName: String? = null,
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

    init {
        refresh()
    }

    fun selectTab(tabIndex: Int) {
        _uiState.update { it.copy(selectedTab = tabIndex) }
    }

    fun filterByStudent(student: TeacherStudentItem?) {
        _uiState.update {
            it.copy(
                selectedStudentId = student?.id,
                selectedStudentName = student?.full_name,
                selectedTab = 1
            )
        }
        refresh(learnerId = student?.id)
    }

    fun clearStudentFilter() {
        _uiState.update {
            it.copy(
                selectedStudentId = null,
                selectedStudentName = null
            )
        }
        refresh(learnerId = null)
    }

    fun refresh(query: String = "", isPullToRefresh: Boolean = false, learnerId: Int? = _uiState.value.selectedStudentId) {
        requestJob?.cancel()
        requestJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = !isPullToRefresh,
                    isRefreshing = isPullToRefresh,
                    isLoadingMore = false,
                    currentPage = 1,
                    errorMessage = null
                )
            }

            val studentsDeferred = async { repository.fetchStudents(query) }
            val progressDeferred = async { repository.fetchProgress(page = 1, query = query, learnerId = learnerId) }

            val studentsResult = studentsDeferred.await()
            val progressResult = progressDeferred.await()

            val students = studentsResult.getOrDefault(_uiState.value.rawStudents)
            val progressPage = progressResult.getOrNull()

            val errorMessage = when {
                studentsResult.isFailure && progressResult.isFailure ->
                    studentsResult.exceptionOrNull()?.localizedMessage ?: "Could not load teacher data."
                else -> null
            }

            _uiState.update {
                it.copy(
                    students = students,
                    rawStudents = students,
                    rows = progressPage?.rows ?: it.rows,
                    currentPage = progressPage?.meta?.current_page ?: 1,
                    lastPage = progressPage?.meta?.last_page ?: 1,
                    totalRecords = progressPage?.meta?.total ?: it.totalRecords,
                    isLoading = false,
                    isRefreshing = false,
                    errorMessage = errorMessage
                )
            }
        }
    }

    fun loadMore(query: String = "") {
        val state = _uiState.value
        if (state.isLoadingMore || state.isLoading || state.currentPage >= state.lastPage) return
        requestJob?.cancel()
        requestJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true, errorMessage = null) }
            repository.fetchProgress(page = state.currentPage + 1, query = query, learnerId = state.selectedStudentId).fold(
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
