package com.jannlawm.alssdosanjuan.ui.module

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jannlawm.alssdosanjuan.data.repository.ModuleRepository
import com.jannlawm.alssdosanjuan.data.repository.TelemetryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

data class ModuleDetailUiState(
    val isDownloading: Boolean = false,
    val downloadedFile: File? = null,
    val errorMessage: String? = null,
    val lastPageRead: Int = 1,
    val totalPages: Int = 20,
    val completionStatus: String = "in_progress" // "not_started" | "in_progress" | "completed"
)

class ModuleDetailViewModel(application: Application) : AndroidViewModel(application) {
    private val moduleRepository = ModuleRepository(application)
    private val telemetryRepository = TelemetryRepository(application)

    private val _uiState = MutableStateFlow(ModuleDetailUiState())
    val uiState: StateFlow<ModuleDetailUiState> = _uiState.asStateFlow()

    fun downloadModule(moduleId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isDownloading = true, errorMessage = null) }
            val result = moduleRepository.downloadModulePdf(moduleId)
            result.fold(
                onSuccess = { file ->
                    _uiState.update { it.copy(isDownloading = false, downloadedFile = file) }
                    recordProgress(moduleId)
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isDownloading = false, errorMessage = error.localizedMessage ?: "Download failed") }
                }
            )
        }
    }

    fun updateReadingProgress(moduleId: Int, page: Int, total: Int, status: String) {
        _uiState.update { it.copy(lastPageRead = page, totalPages = total, completionStatus = status) }
        recordProgress(moduleId)
    }

    private fun recordProgress(moduleId: Int) {
        viewModelScope.launch {
            val state = _uiState.value
            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            val time = dateFormat.format(Date())
            telemetryRepository.saveProgress(
                moduleId = moduleId,
                status = state.completionStatus,
                page = state.lastPageRead,
                total = state.totalPages,
                time = time
            )
        }
    }
}
