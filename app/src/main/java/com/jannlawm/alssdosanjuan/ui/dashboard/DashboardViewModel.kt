package com.jannlawm.alssdosanjuan.ui.dashboard

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jannlawm.alssdosanjuan.data.local.TokenManager
import com.jannlawm.alssdosanjuan.data.model.AnnouncementItem
import com.jannlawm.alssdosanjuan.data.model.EnrollmentStatusDetail
import com.jannlawm.alssdosanjuan.data.model.ModuleItem
import com.jannlawm.alssdosanjuan.data.model.Strand
import com.jannlawm.alssdosanjuan.data.model.User
import com.jannlawm.alssdosanjuan.data.repository.LearnerPortalRepository
import com.jannlawm.alssdosanjuan.data.repository.ModuleRepository
import com.jannlawm.alssdosanjuan.data.repository.OfflineStorageStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
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
    val downloadingModuleIds: Set<Int> = emptySet(),
    val selectedBottomTab: Int = 0, // 0 = Modules, 1 = Advisories, 2 = My Profile
    val announcements: List<AnnouncementItem> = emptyList(),
    val isLoadingAnnouncements: Boolean = false,
    val enrollmentDetail: EnrollmentStatusDetail? = null,
    val isLoadingProfile: Boolean = false,
    val isUploadingDocument: Boolean = false,
    val storageStats: OfflineStorageStats = OfflineStorageStats(),
    val portalNoticeMessage: String? = null
)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val moduleRepository = ModuleRepository(application)
    private val portalRepository = LearnerPortalRepository(application)
    private val tokenManager = TokenManager(application)

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        // Step 1: Pre-populate from cached user session immediately
        val savedUser = tokenManager.getUser()
        val enrolledLevel = when (savedUser?.latest_enrollment?.program_track) {
            "BLP" -> "BLP"
            "A&E_Elementary" -> "Elementary"
            "A&E_Junior_High" -> "Junior_High"
            else -> null
        }

        if (savedUser != null) {
            _uiState.update {
                it.copy(
                    user = savedUser,
                    selectedLevel = enrolledLevel
                )
            }
        }

        // Step 2: Instant 0ms render from local SQLite cache
        loadLocalCache(enrolledLevel)

        // Step 3: Fast concurrent background sync with server
        syncWithServer(isPullToRefresh = false)
    }

    private fun loadLocalCache(level: String?) {
        val localModules = moduleRepository.getLocalCachedModules(level)
        if (localModules.isNotEmpty()) {
            val downloadedIds = localModules.filter { m ->
                moduleRepository.getCachedPdfFile(m.id) != null
            }.map { it.id }.toSet()

            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    allTrackModules = localModules,
                    rawModules = localModules,
                    downloadedModuleIds = downloadedIds
                )
            }
            applyLocalFilters()
        }
    }

    fun setUser(user: User) {
        val previousUserId = _uiState.value.user?.id
        val previousLevel = _uiState.value.selectedLevel
        val newLevel = when (user.latest_enrollment?.program_track) {
            "BLP" -> "BLP"
            "A&E_Elementary" -> "Elementary"
            "A&E_Junior_High" -> "Junior_High"
            else -> null
        }

        _uiState.update {
            it.copy(
                user = user,
                selectedLevel = newLevel
            )
        }

        // Only reload if user or track changed, or if cache is empty
        if (previousUserId != user.id || previousLevel != newLevel || _uiState.value.allTrackModules.isEmpty()) {
            loadLocalCache(newLevel)
            syncWithServer(isPullToRefresh = false)
        }
    }

    fun syncWithServer(isPullToRefresh: Boolean = false) {
        viewModelScope.launch {
            val hasCachedData = _uiState.value.allTrackModules.isNotEmpty()
            _uiState.update {
                it.copy(
                    // Only show blocking spinner on cold start when no cache exists
                    isLoading = !isPullToRefresh && !hasCachedData,
                    isRefreshing = isPullToRefresh,
                    errorMessage = null
                )
            }

            val currentLevel = _uiState.value.selectedLevel

            // Parallel network fetch: run strands, modules, announcements, and storage check concurrently
            val clcId = _uiState.value.user?.latest_enrollment?.clc?.id
            val strandsDeferred = async(Dispatchers.IO) {
                moduleRepository.fetchStrands(currentLevel)
            }
            val modulesDeferred = async(Dispatchers.IO) {
                moduleRepository.fetchAndCacheModules(level = currentLevel)
            }
            val announcementsDeferred = async(Dispatchers.IO) {
                portalRepository.fetchAnnouncements(clcId)
            }
            val storageDeferred = async(Dispatchers.IO) {
                portalRepository.getStorageStats()
            }

            val strandsResult = strandsDeferred.await()
            val modulesResult = modulesDeferred.await()
            val announcementsResult = announcementsDeferred.await()
            val storageStats = storageDeferred.await()

            val loadedModules = modulesResult.getOrDefault(_uiState.value.allTrackModules)
            val isOffline = strandsResult.isFailure && modulesResult.isFailure

            val downloadedIds = withContext(Dispatchers.IO) {
                loadedModules.filter { m ->
                    moduleRepository.getCachedPdfFile(m.id) != null
                }.map { it.id }.toSet()
            }

            _uiState.update { state ->
                val rawStrands = strandsResult.getOrDefault(state.strands)
                val computedStrands = rawStrands.map { strand ->
                    val matchingCount = loadedModules.count { m ->
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

                state.copy(
                    isLoading = false,
                    isRefreshing = false,
                    isOffline = isOffline,
                    strands = computedStrands,
                    allTrackModules = loadedModules,
                    rawModules = loadedModules,
                    downloadedModuleIds = downloadedIds,
                    announcements = announcementsResult.getOrDefault(state.announcements),
                    storageStats = storageStats,
                    errorMessage = if (isOffline && loadedModules.isEmpty()) {
                        "You are currently offline. Download modules while connected to read them offline."
                    } else null
                )
            }

            applyLocalFilters()
        }
    }

    /**
     * Instant in-memory filtering: 0ms UI response time on chip taps and search typing.
     */
    private fun applyLocalFilters() {
        val state = _uiState.value
        val source = if (state.allTrackModules.isNotEmpty()) state.allTrackModules else state.rawModules
        val query = state.searchQuery.trim().lowercase()
        val strandId = state.selectedStrandId
        val selectedStrandCode = state.strands.find { it.id == strandId }?.code?.lowercase()
        val subLevel = state.selectedSubLevel?.lowercase()
        val language = state.selectedLanguage?.lowercase()
        val downloadedOnly = state.filterDownloadedOnly
        val downloadedIds = state.downloadedModuleIds

        val filtered = source.filter { m ->
            // Strand filter: match by ID or Strand Code
            val matchesStrand = strandId == null ||
                (m.strand?.id != null && m.strand.id != 0 && m.strand.id == strandId) ||
                (selectedStrandCode != null && m.strand?.code?.lowercase() == selectedStrandCode)

            // Sub-level filter (e.g., Lower Elementary, Advanced Elementary)
            val matchesSubLevel = subLevel.isNullOrBlank() ||
                m.sub_level?.lowercase() == subLevel

            // Language filter (e.g., English, Filipino)
            val matchesLanguage = language.isNullOrBlank() ||
                m.language?.lowercase() == language

            // Search query (title, description, module code)
            val matchesQuery = query.isBlank() || (
                m.title.lowercase().contains(query) ||
                (m.description?.lowercase()?.contains(query) == true) ||
                (m.module_number?.lowercase()?.contains(query) == true)
            )

            // Downloaded only filter
            val matchesDownloaded = !downloadedOnly || downloadedIds.contains(m.id)

            matchesStrand && matchesSubLevel && matchesLanguage && matchesQuery && matchesDownloaded
        }

        _uiState.update { it.copy(modules = filtered) }
    }

    fun refresh() {
        syncWithServer(isPullToRefresh = true)
    }

    fun toggleFilterDownloadedOnly() {
        _uiState.update { it.copy(filterDownloadedOnly = !it.filterDownloadedOnly) }
        applyLocalFilters()
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
                        state.copy(
                            downloadingModuleIds = state.downloadingModuleIds - moduleId,
                            downloadedModuleIds = newDownloaded
                        )
                    }
                    applyLocalFilters()
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
                    state.copy(downloadedModuleIds = remainingDownloadedIds)
                }
                applyLocalFilters()
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
            _uiState.update { it.copy(downloadedModuleIds = downloaded) }
            applyLocalFilters()
        }
    }

    fun filterByStrand(strandId: Int?) {
        _uiState.update { it.copy(selectedStrandId = strandId) }
        applyLocalFilters()
    }

    fun filterByLevel(level: String?) {
        val user = _uiState.value.user
        if (user != null && user.isLearner) {
            // Strictly locked to enrolled program track
            return
        }
        _uiState.update { it.copy(selectedLevel = level) }
        syncWithServer(isPullToRefresh = false)
    }

    fun filterBySubLevel(subLevel: String?) {
        _uiState.update { it.copy(selectedSubLevel = subLevel) }
        applyLocalFilters()
    }

    fun filterByLanguage(language: String?) {
        _uiState.update { it.copy(selectedLanguage = language) }
        applyLocalFilters()
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        applyLocalFilters()
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
        applyLocalFilters()
    }

    fun selectBottomTab(tabIndex: Int) {
        _uiState.update { it.copy(selectedBottomTab = tabIndex) }
        if (tabIndex == 1 && _uiState.value.announcements.isEmpty()) {
            refreshAnnouncements()
        } else if (tabIndex == 2) {
            refreshStorageStats()
            if (_uiState.value.enrollmentDetail == null) {
                loadProfileAndDocuments()
            }
        }
    }

    fun refreshAnnouncements() {
        val clcId = _uiState.value.user?.latest_enrollment?.clc?.id
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingAnnouncements = true) }
            portalRepository.fetchAnnouncements(clcId).fold(
                onSuccess = { list ->
                    _uiState.update { it.copy(announcements = list, isLoadingAnnouncements = false) }
                },
                onFailure = {
                    _uiState.update { it.copy(isLoadingAnnouncements = false) }
                }
            )
        }
    }

    fun loadProfileAndDocuments() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingProfile = true) }
            portalRepository.fetchEnrollmentStatus().fold(
                onSuccess = { detail ->
                    _uiState.update { it.copy(enrollmentDetail = detail, isLoadingProfile = false) }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isLoadingProfile = false,
                            portalNoticeMessage = err.localizedMessage ?: "Could not load profile details."
                        )
                    }
                }
            )
        }
    }

    fun uploadDocument(docType: String, fileUri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingDocument = true, portalNoticeMessage = null) }
            portalRepository.uploadDocument(docType, fileUri).fold(
                onSuccess = { message ->
                    _uiState.update { it.copy(isUploadingDocument = false, portalNoticeMessage = message) }
                    loadProfileAndDocuments()
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isUploadingDocument = false,
                            portalNoticeMessage = err.localizedMessage ?: "Document upload failed."
                        )
                    }
                }
            )
        }
    }

    fun refreshStorageStats() {
        viewModelScope.launch {
            val stats = portalRepository.getStorageStats()
            _uiState.update { it.copy(storageStats = stats) }
        }
    }

    fun clearCompletedStorage() {
        viewModelScope.launch {
            val count = portalRepository.clearCompletedModules()
            val stats = portalRepository.getStorageStats()
            refreshDownloadedState()
            _uiState.update {
                it.copy(
                    storageStats = stats,
                    portalNoticeMessage = if (count > 0) "Removed $count completed module PDF(s) to free storage." else "No completed module PDFs found to clear."
                )
            }
        }
    }

    fun clearAllStorage() {
        viewModelScope.launch {
            val count = portalRepository.clearAllDownloadedModules()
            val stats = portalRepository.getStorageStats()
            refreshDownloadedState()
            _uiState.update {
                it.copy(
                    storageStats = stats,
                    portalNoticeMessage = if (count > 0) "Cleared all $count downloaded module PDF(s)." else "Offline cache is already empty."
                )
            }
        }
    }

    fun clearPortalNotice() {
        _uiState.update { it.copy(portalNoticeMessage = null) }
    }
}
