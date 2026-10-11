package com.jannlawm.alssdosanjuan.ui.module

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jannlawm.alssdosanjuan.data.model.ModuleItem
import com.jannlawm.alssdosanjuan.ui.theme.DepEdBlue
import com.jannlawm.alssdosanjuan.ui.theme.DepEdRed
import com.jannlawm.alssdosanjuan.ui.theme.DepEdYellow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModuleDetailScreen(
    module: ModuleItem,
    moduleDetailViewModel: ModuleDetailViewModel = viewModel(),
    onBack: () -> Unit
) {
    val state by moduleDetailViewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(module.id) {
        moduleDetailViewModel.loadModule(module)
    }

    // Zoom & Pan state for reading PDF pages
    var zoomScale by remember { mutableFloatStateOf(1f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }

    // Reset zoom when page changes
    LaunchedEffect(state.currentPage) {
        zoomScale = 1f
        panOffset = Offset.Zero
    }

    val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
        val newScale = (zoomScale * zoomChange).coerceIn(1f, 3.5f)
        zoomScale = newScale
        if (newScale <= 1.05f) {
            panOffset = Offset.Zero
        } else {
            panOffset += offsetChange
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Module #${module.module_number ?: module.id}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = module.title,
                            style = MaterialTheme.typography.bodySmall,
                            color = DepEdYellow,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    // Open in External PDF Viewer (Drive, Acrobat, WPS, etc.)
                    if (state.pdfFile != null) {
                        IconButton(
                            onClick = {
                                val file = state.pdfFile ?: return@IconButton
                                try {
                                    val uri = FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.fileprovider",
                                        file
                                    )
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        setDataAndType(uri, "application/pdf")
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Open Module in PDF App"))
                                } catch (e: Exception) {
                                    Toast.makeText(context, "No PDF app found to open file", Toast.LENGTH_SHORT).show()
                                }
                            }
                        ) {
                            Icon(
                                Icons.Default.Share,
                                contentDescription = "Open in External App",
                                tint = Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DepEdBlue)
            )
        },
        bottomBar = {
            // Reader Navigation & Completion Bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Previous Page Button
                        FilledTonalButton(
                            onClick = { moduleDetailViewModel.previousPage(module.id) },
                            enabled = state.currentPage > 1 && !state.isLoading,
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text("< Prev")
                        }

                        // Page Counter Indicator
                        Surface(
                            color = DepEdBlue.copy(alpha = 0.1f),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = "Page ${state.currentPage} of ${state.totalPages}",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = DepEdBlue
                            )
                        }

                        // Next Page Button
                        FilledTonalButton(
                            onClick = { moduleDetailViewModel.nextPage(module.id) },
                            enabled = state.currentPage < state.totalPages && !state.isLoading,
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text("Next >")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Mark as Completed Button
                    Button(
                        onClick = { moduleDetailViewModel.markCompleted(module.id) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (state.completionStatus == "completed") Color(0xFF1B5E20) else DepEdRed
                        ),
                        enabled = !state.isLoading
                    ) {
                        Icon(
                            if (state.completionStatus == "completed") Icons.Default.CheckCircle else Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (state.completionStatus == "completed") "Module Completed" else "Mark Module Completed",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // Module Info Strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        color = DepEdBlue.copy(alpha = 0.12f),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = module.sub_level ?: module.level.replace("_", " "),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = DepEdBlue,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (!module.language.isNullOrEmpty()) {
                        Surface(
                            color = if (module.language == "Filipino") DepEdRed.copy(alpha = 0.12f) else DepEdBlue.copy(alpha = 0.12f),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = module.language,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (module.language == "Filipino") DepEdRed else DepEdBlue,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (module.strand != null) {
                    Text(
                        text = module.strand.code,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // PDF Display Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(MaterialTheme.shapes.medium)
                    .background(Color(0xFFE2E8F0))
                    .border(BorderStroke(1.dp, Color(0xFFCBD5E1)), MaterialTheme.shapes.medium),
                contentAlignment = Alignment.Center
            ) {
                if (state.isLoading || state.isDownloading) {
                    // Downloading / Preparing PDF State
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        CircularProgressIndicator(
                            color = DepEdBlue,
                            modifier = Modifier.size(48.dp),
                            strokeWidth = 4.dp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Loading & Preparing Module PDF...",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = DepEdBlue
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Downloading authentic DepEd learning material for offline reading. Once cached, this opens instantly without internet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.DarkGray,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else if (state.errorMessage != null && state.currentPageBitmap == null) {
                    // Error State
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            text = "Unable to load module PDF",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = state.errorMessage ?: "Unknown error occurred while downloading PDF.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.DarkGray,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { moduleDetailViewModel.downloadAndOpen(module.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = DepEdBlue)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Retry Download")
                        }
                    }
                } else if (state.currentPageBitmap != null) {
                    // Rendered PDF Page with Pinch-to-Zoom & Double-Tap
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onDoubleTap = {
                                        // Toggle between 1x and 2x zoom
                                        if (zoomScale > 1.2f) {
                                            zoomScale = 1f
                                            panOffset = Offset.Zero
                                        } else {
                                            zoomScale = 2f
                                        }
                                    }
                                )
                            }
                            .transformable(state = transformState)
                            .graphicsLayer {
                                scaleX = zoomScale
                                scaleY = zoomScale
                                translationX = panOffset.x
                                translationY = panOffset.y
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = state.currentPageBitmap!!.asImageBitmap(),
                            contentDescription = "Module Page ${state.currentPage}",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp)
                                .clip(MaterialTheme.shapes.small)
                                .background(Color.White),
                            contentScale = ContentScale.Fit
                        )

                        // Subtle Rendering Spinner Indicator when changing pages
                        if (state.isRendering) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .background(Color.Black.copy(alpha = 0.5f), MaterialTheme.shapes.small)
                                    .padding(6.dp)
                            ) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
