package com.jannlawm.alssdosanjuan.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jannlawm.alssdosanjuan.data.model.ModuleItem
import com.jannlawm.alssdosanjuan.data.model.Strand
import com.jannlawm.alssdosanjuan.data.repository.ModuleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DashboardUiState(
    val strands: List<Strand> = emptyList(),
    val modules: List<ModuleItem> = emptyList(),
    val selectedStrandId: Int? = null,
    val selectedLevel: String? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val moduleRepository = ModuleRepository(application)

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadStrandsAndModules()
    }

    fun loadStrandsAndModules() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val strandsResult = moduleRepository.fetchStrands()
            val modulesResult = moduleRepository.fetchAndCacheModules(_uiState.value.selectedStrandId, _uiState.value.selectedLevel, _uiState.value.searchQuery)

            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    strands = strandsResult.getOrDefault(state.strands),
                    modules = modulesResult.getOrDefault(state.modules),
                    errorMessage = if (strandsResult.isFailure || modulesResult.isFailure) "Loaded with some offline limitations." else null
                )
            }
        }
    }

    fun filterByStrand(strandId: Int?) {
        _uiState.update { it.copy(selectedStrandId = strandId) }
        loadStrandsAndModules()
    }

    fun filterByLevel(level: String?) {
        _uiState.update { it.copy(selectedLevel = level) }
        loadStrandsAndModules()
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        loadStrandsAndModules()
    }
}
