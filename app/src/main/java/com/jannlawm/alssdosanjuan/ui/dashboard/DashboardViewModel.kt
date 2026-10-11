package com.jannlawm.alssdosanjuan.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jannlawm.alssdosanjuan.data.model.ModuleItem
import com.jannlawm.alssdosanjuan.data.model.Strand
import com.jannlawm.alssdosanjuan.data.model.User
import com.jannlawm.alssdosanjuan.data.repository.ModuleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class DashboardUiState(
    val strands: List<Strand> = emptyList(),
    val allTrackModules: List<ModuleItem> = emptyList(),
    val rawModules: List<ModuleItem> = emptyList(),
    val modules: List<ModuleItem> = emptyList(),
    val selectedStrandId: Int? = null,
    val selectedLevel: String? = null,
    val selectedSubLevel: String? = null,
    val selectedLanguage: String? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isOffline: Boolean = false,
    val filterDownloadedOnly: Boolean = false,
    val errorMessage: String? = null,
    val user: User? = null,
    val downloadedModuleIds: Set<Int> = emptySet(),
    val downloadingModuleIds: Set<Int> = emptySet()
)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val moduleRepository = ModuleRepository(application)

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadStrandsAndModules()
    }

    fun setUser(user: User) {
        val currentUserId = _uiState.value.user?.id
        if (currentUserId != user.id) {
            val enrolledLevel = when (user.latest_enrollment?.program_track) {
                "BLP" -> "BLP"
                "A&E_Elementary" -> "Elementary"
                "A&E_Junior_High" -> "Junior_High"
                else -> null
            }
            _uiState.update {
                it.copy(
                    user = user,
                    selectedLevel = enrolledLevel,
                    selectedSubLevel = null,
                    selectedStrandId = null
                )
            }
            loadStrandsAndModules()
        }
    }

    fun loadStrandsAndModules(isPullToRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = !isPullToRefresh,
                    isRefreshing = isPullToRefresh,
                    errorMessage = null
                )
            }

            val currentLevel = _uiState.value.selectedLevel
            val strandsResult = moduleRepository.fetchStrands(currentLevel)
            val modulesResult = moduleRepository.fetchAndCacheModules(
                _uiState.value.selectedStrandId,
                currentLevel,
                _uiState.value.selectedSubLevel,
                _uiState.value.selectedLanguage,
                _uiState.value.searchQuery
            )

            val loadedModules = modulesResult.getOrDefault(_uiState.value.rawModules)
            val isOffline = strandsResult.isFailure

            val downloadedIds = withContext(Dispatchers.IO) {
                loadedModules.filter { m ->
                    moduleRepository.getCachedPdfFile(m.id) != null
                }.map { it.id }.toSet()
            }

            _uiState.update { state ->
                // Maintain allTrackModules whenever we fetch unfiltered track modules
                val allModulesForCounts = if (state.selectedStrandId == null && state.selectedSubLevel == null && state.selectedLanguage == null && state.searchQuery.isBlank() && loadedModules.isNotEmpty()) {
                    loadedModules
                } else if (state.allTrackModules.isNotEmpty()) {
                    state.allTrackModules
                } else {
                    loadedModules
                }

                val rawStrands = strandsResult.getOrDefault(state.strands)
                val computedStrands = rawStrands.map { strand ->
                    val matchingCount = allModulesForCounts.count { m ->
                        (m.strand?.id != null && m.strand.id != 0 && m.strand.id == strand.id) ||
                        (m.strand?.code != null && m.strand.code.equals(strand.code, ignoreCase = true)) ||
                        (m.strand?.title != null && (m.strand.title.equals(strand.name, ignoreCase = true) || m.strand.title.equals(strand.code, ignoreCase = true)))
                    }
                    val count = when {
                        strand.modules_count > 0 -> strand.modules_count
                        strand.published_modules_count > 0 -> strand.published_modules_count
                        matchingCount > 0 -> matchingCount
                        else -> 0
                    }
                    strand.copy(modules_count = count)
                }

                val filtered = if (state.filterDownloadedOnly) {
                    loadedModules.filter { downloadedIds.contains(it.id) }
                } else {
                    loadedModules
                }

                state.copy(
                    isLoading = false,
                    isRefreshing = false,
                    isOffline = isOffline,
                    strands = computedStrands,
                    allTrackModules = allModulesForCounts,
                    rawModules = loadedModules,
                    modules = filtered,
                    downloadedModuleIds = downloadedIds,
                    errorMessage = if (isOffline && loadedModules.isEmpty()) {
                        "You are currently offline. Download modules while connected to read them offline."
                    } else null
                )
            }
        }
    }

    fun refresh() {
        loadStrandsAndModules(isPullToRefresh = true)
    }

    fun toggleFilterDownloadedOnly() {
        _uiState.update { state ->
            val newFilter = !state.filterDownloadedOnly
            val filtered = if (newFilter) {
                state.rawModules.filter { state.downloadedModuleIds.contains(it.id) }
            } else {
                state.rawModules
            }
            state.copy(
                filterDownloadedOnly = newFilter,
                modules = filtered
            )
        }
    }

    fun downloadModule(moduleId: Int) {
        if (_uiState.value.downloadingModuleIds.contains(moduleId)) return
        if (_uiState.value.downloadedModuleIds.contains(moduleId)) return

        viewModelScope.launch {
            _uiState.update { it.copy(downloadingModuleIds = it.downloadingModuleIds + moduleId) }
            val result = moduleRepository.downloadModulePdf(moduleId)
            result.fold(
                onSuccess = {
                    _uiState.update { state ->
                        val newDownloaded = state.downloadedModuleIds + moduleId
                        val newModules = if (state.filterDownloadedOnly) {
                            state.rawModules.filter { newDownloaded.contains(it.id) }
                        } else {
                            state.modules
                        }
                        state.copy(
                            downloadingModuleIds = state.downloadingModuleIds - moduleId,
                            downloadedModuleIds = newDownloaded,
                            modules = newModules
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            downloadingModuleIds = it.downloadingModuleIds - moduleId,
                            errorMessage = "Download failed: ${error.localizedMessage}"
                        )
                    }
                }
            )
        }
    }

    fun deleteDownloadedModule(moduleId: Int) {
        if (_uiState.value.downloadingModuleIds.contains(moduleId)) return

        viewModelScope.launch {
            val deleted = withContext(Dispatchers.IO) {
                moduleRepository.deleteCachedPdfFile(moduleId)
            }

            if (deleted) {
                _uiState.update { state ->
                    val remainingDownloadedIds = state.downloadedModuleIds - moduleId
                    val visibleModules = if (state.filterDownloadedOnly) {
                        state.rawModules.filter { remainingDownloadedIds.contains(it.id) }
                    } else {
                        state.modules
                    }
                    state.copy(
                        downloadedModuleIds = remainingDownloadedIds,
                        modules = visibleModules
                    )
                }
            } else {
                _uiState.update {
                    it.copy(errorMessage = "Could not delete the downloaded module from this device.")
                }
            }
        }
    }

    fun refreshDownloadedState() {
        viewModelScope.launch(Dispatchers.IO) {
            val downloaded = _uiState.value.rawModules.filter { m ->
                moduleRepository.getCachedPdfFile(m.id) != null
            }.map { it.id }.toSet()
            _uiState.update { state ->
                val filtered = if (state.filterDownloadedOnly) {
                    state.rawModules.filter { downloaded.contains(it.id) }
                } else {
                    state.rawModules
                }
                state.copy(
                    downloadedModuleIds = downloaded,
                    modules = filtered
                )
            }
        }
    }

    fun filterByStrand(strandId: Int?) {
        _uiState.update { it.copy(selectedStrandId = strandId) }
        loadStrandsAndModules()
    }

    fun filterByLevel(level: String?) {
        val user = _uiState.value.user
        if (user != null && user.isLearner) {
            // Strictly locked to enrolled program track
            return
        }
        _uiState.update { it.copy(selectedLevel = level) }
        loadStrandsAndModules()
    }

    fun filterBySubLevel(subLevel: String?) {
        _uiState.update { it.copy(selectedSubLevel = subLevel) }
        loadStrandsAndModules()
    }

    fun filterByLanguage(language: String?) {
        _uiState.update { it.copy(selectedLanguage = language) }
        loadStrandsAndModules()
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        loadStrandsAndModules()
    }

    fun clearAllFilters() {
        _uiState.update {
            it.copy(
                selectedStrandId = null,
                selectedSubLevel = null,
                selectedLanguage = null,
                filterDownloadedOnly = false,
                searchQuery = ""
            )
        }
        loadStrandsAndModules()
    }
}
