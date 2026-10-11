package com.jannlawm.alssdosanjuan.ui.module

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jannlawm.alssdosanjuan.data.model.ModuleItem
import com.jannlawm.alssdosanjuan.data.repository.ModuleRepository
import com.jannlawm.alssdosanjuan.data.repository.TelemetryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

data class ModuleDetailUiState(
    val isLoading: Boolean = true,
    val isDownloading: Boolean = false,
    val isRendering: Boolean = false,
    val pdfFile: File? = null,
    val errorMessage: String? = null,
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val currentPageBitmap: Bitmap? = null,
    val completionStatus: String = "in_progress", // "in_progress" | "completed"
    val isCompleted: Boolean = false
)

class ModuleDetailViewModel(application: Application) : AndroidViewModel(application) {
    private val moduleRepository = ModuleRepository(application)
    private val telemetryRepository = TelemetryRepository(application)

    private val _uiState = MutableStateFlow(ModuleDetailUiState())
    val uiState: StateFlow<ModuleDetailUiState> = _uiState.asStateFlow()

    private val renderLock = Any()
    private var pdfRenderer: PdfRenderer? = null
    private var fileDescriptor: ParcelFileDescriptor? = null
    private var currentModuleId: Int = -1

    fun loadModule(module: ModuleItem) {
        if (currentModuleId == module.id && _uiState.value.pdfFile != null) {
            return
        }
        currentModuleId = module.id
        val cached = moduleRepository.getCachedPdfFile(module.id)
        if (cached != null) {
            openPdfFile(cached, 1, module.id)
        } else {
            downloadAndOpen(module.id, 1)
        }
    }

    fun downloadAndOpen(moduleId: Int, initialPage: Int = 1) {
        currentModuleId = moduleId
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isDownloading = true, errorMessage = null) }
            val result = moduleRepository.downloadModulePdf(moduleId)
            result.fold(
                onSuccess = { file ->
                    openPdfFile(file, initialPage, moduleId)
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isDownloading = false,
                            errorMessage = error.localizedMessage ?: "Failed to download module PDF from server."
                        )
                    }
                }
            )
        }
    }

    private fun openPdfFile(file: File, initialPage: Int, moduleId: Int) {
        try {
            synchronized(renderLock) {
                try {
                    pdfRenderer?.close()
                    fileDescriptor?.close()
                } catch (ignored: Exception) {}

                fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                pdfRenderer = PdfRenderer(fileDescriptor!!)
            }

            val total = pdfRenderer?.pageCount ?: 1
            val startPage = initialPage.coerceIn(1, total)

            _uiState.update {
                it.copy(
                    isLoading = false,
                    isDownloading = false,
                    pdfFile = file,
                    totalPages = total,
                    currentPage = startPage,
                    errorMessage = null
                )
            }

            renderPage(startPage)
            recordProgress(moduleId)
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    isDownloading = false,
                    errorMessage = "Could not open PDF file: ${e.localizedMessage}"
                )
            }
        }
    }

    fun nextPage(moduleId: Int) {
        val state = _uiState.value
        if (state.currentPage < state.totalPages) {
            val next = state.currentPage + 1
            val newStatus = if (next == state.totalPages) "completed" else state.completionStatus
            _uiState.update { it.copy(completionStatus = newStatus, isCompleted = newStatus == "completed") }
            renderPage(next)
            recordProgress(moduleId)
        }
    }

    fun previousPage(moduleId: Int) {
        val state = _uiState.value
        if (state.currentPage > 1) {
            val prev = state.currentPage - 1
            renderPage(prev)
            recordProgress(moduleId)
        }
    }

    fun markCompleted(moduleId: Int) {
        _uiState.update { it.copy(completionStatus = "completed", isCompleted = true) }
        recordProgress(moduleId)
    }

    private fun renderPage(pageNumber: Int) {
        val renderer = pdfRenderer ?: return
        val pageIndex = (pageNumber - 1).coerceIn(0, (renderer.pageCount - 1).coerceAtLeast(0))

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isRendering = true) }
            try {
                val bitmap: Bitmap = synchronized(renderLock) {
                    val page = renderer.openPage(pageIndex)
                    // High-density 2x scale for sharp readability
                    val scale = 2
                    val width = page.width * scale
                    val height = page.height * scale
                    val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(bmp)
                    canvas.drawColor(Color.WHITE)
                    page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    page.close()
                    bmp
                }

                _uiState.update {
                    it.copy(
                        currentPage = pageIndex + 1,
                        currentPageBitmap = bitmap,
                        isRendering = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isRendering = false,
                        errorMessage = "Failed to render page: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    private fun recordProgress(moduleId: Int) {
        viewModelScope.launch {
            val state = _uiState.value
            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            val time = dateFormat.format(Date())
            telemetryRepository.saveProgress(
                moduleId = moduleId,
                status = state.completionStatus,
                page = state.currentPage,
                total = state.totalPages,
                time = time
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        try {
            synchronized(renderLock) {
                pdfRenderer?.close()
                pdfRenderer = null
                fileDescriptor?.close()
                fileDescriptor = null
            }
        } catch (ignored: Exception) {}
    }
}
