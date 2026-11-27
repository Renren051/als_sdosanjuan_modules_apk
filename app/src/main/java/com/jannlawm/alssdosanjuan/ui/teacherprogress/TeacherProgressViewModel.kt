package com.jannlawm.alssdosanjuan.ui.teacherprogress

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jannlawm.alssdosanjuan.data.model.AnnouncementItem
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class TeacherProgressUiState(
    val students: List<TeacherStudentItem> = emptyList(),
    val rawStudents: List<TeacherStudentItem> = emptyList(),
    val availableClcs: List<String> = emptyList(),
    val selectedClcName: String? = null,
    val rows: List<TeacherModuleProgress> = emptyList(),
    val announcements: List<AnnouncementItem> = emptyList(),
    val selectedTab: Int = 0, // 0 = "My Students", 1 = "Telemetry", 2 = "Advisories"
    val selectedStudentId: Int? = null,
    val selectedStudentName: String? = null,
    val inspectedStudent: TeacherStudentItem? = null,
    val isSavingRemark: Boolean = false,
    val noticeMessage: String? = null,
    val lastSyncedLabel: String? = null,
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

    fun filterByClc(clcName: String?) {
        _uiState.update { state ->
            val filtered = applyClcFilter(state.rawStudents, clcName)
            state.copy(selectedClcName = clcName, students = filtered)
        }
    }

    private fun applyClcFilter(list: List<TeacherStudentItem>, clcName: String?): List<TeacherStudentItem> {
        if (clcName.isNullOrBlank()) return list
        return list.filter { it.clc_name.equals(clcName, ignoreCase = true) }
    }

    fun openStudentDetail(student: TeacherStudentItem) {
        _uiState.update { it.copy(inspectedStudent = student) }
    }

    fun closeStudentDetail() {
        _uiState.update { it.copy(inspectedStudent = null) }
    }

    fun saveStudentRemark(learnerId: Int, remarks: String) {
        if (remarks.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSavingRemark = true) }
            repository.saveStudentRemark(learnerId, remarks.trim()).fold(
                onSuccess = { msg ->
                    _uiState.update { state ->
                        val updatedRaw = state.rawStudents.map { s ->
                            if (s.id == learnerId) s.copy(remarks = remarks.trim()) else s
                        }
                        val updatedFiltered = applyClcFilter(updatedRaw, state.selectedClcName)
                        val updatedInspected = state.inspectedStudent?.let { cur ->
                            if (cur.id == learnerId) cur.copy(remarks = remarks.trim()) else cur
                        }
                        state.copy(
                            rawStudents = updatedRaw,
                            students = updatedFiltered,
                            inspectedStudent = updatedInspected,
                            isSavingRemark = false,
                            noticeMessage = msg
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isSavingRemark = false,
                            noticeMessage = err.localizedMessage ?: "Failed to save evaluation remarks."
                        )
                    }
                }
            )
        }
    }

    fun clearNotice() {
        _uiState.update { it.copy(noticeMessage = null) }
    }

    fun filterByStudent(student: TeacherStudentItem?) {
        _uiState.update {
            it.copy(
                selectedStudentId = student?.id,
                selectedStudentName = student?.full_name,
                selectedTab = 1,
                inspectedStudent = null
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
            val hasData = _uiState.value.rawStudents.isNotEmpty()
            _uiState.update {
                it.copy(
                    isLoading = !isPullToRefresh && !hasData,
                    isRefreshing = isPullToRefresh,
                    isLoadingMore = false,
                    currentPage = 1,
                    errorMessage = null
                )
            }

            val studentsDeferred = async { repository.fetchStudents(query) }
            val progressDeferred = async { repository.fetchProgress(page = 1, query = query, learnerId = learnerId) }
            val announcementsDeferred = async { repository.fetchAnnouncements() }

            val studentsResult = studentsDeferred.await()
            val progressResult = progressDeferred.await()
            val announcementsResult = announcementsDeferred.await()

            val rawStudents = studentsResult.getOrDefault(_uiState.value.rawStudents)
            val clcs = rawStudents.mapNotNull { it.clc_name?.takeIf { c -> c.isNotBlank() } }.distinct().sorted()
            val currentClc = _uiState.value.selectedClcName?.takeIf { clcs.contains(it) }
            val filteredStudents = applyClcFilter(rawStudents, currentClc)
            val progressPage = progressResult.getOrNull()

            val syncedAt = if (studentsResult.isSuccess || progressResult.isSuccess) {
                SimpleDateFormat("h:mm a", Locale.US).format(Date())
            } else {
                _uiState.value.lastSyncedLabel
            }

            val errorMessage = when {
                studentsResult.isFailure && progressResult.isFailure && rawStudents.isEmpty() ->
                    studentsResult.exceptionOrNull()?.localizedMessage ?: "Could not load teacher data."
                else -> null
            }

            _uiState.update {
                it.copy(
                    students = filteredStudents,
                    rawStudents = rawStudents,
                    availableClcs = clcs,
                    selectedClcName = currentClc,
                    rows = progressPage?.rows ?: it.rows,
                    announcements = announcementsResult.getOrDefault(it.announcements),
                    currentPage = progressPage?.meta?.current_page ?: 1,
                    lastPage = progressPage?.meta?.last_page ?: 1,
                    totalRecords = progressPage?.meta?.total ?: it.totalRecords,
                    lastSyncedLabel = syncedAt,
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
