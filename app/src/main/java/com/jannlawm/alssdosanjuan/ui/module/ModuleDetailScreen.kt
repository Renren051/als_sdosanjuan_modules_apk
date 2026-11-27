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
import androidx.compose.material.icons.outlined.CloudDownload
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
    var showModuleGame by remember(module.id) { mutableStateOf(false) }

    LaunchedEffect(module.id) {
        moduleDetailViewModel.loadModule(module)
    }

    DisposableEffect(module.id) {
        onDispose { moduleDetailViewModel.releaseViewer() }
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
                    if (state.pdfFile != null && state.isDownloaded) {
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
                        val accentBlue = MaterialTheme.colorScheme.primary
                        val accentRed = MaterialTheme.colorScheme.tertiary
                        Surface(
                            color = accentBlue.copy(alpha = 0.14f),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = "Page ${state.currentPage} of ${state.totalPages}",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = accentBlue
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
                        onClick = {
                            if (!state.isCompleted) {
                                moduleDetailViewModel.markCompleted(module.id)
                            }
                            showModuleGame = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (state.isCompleted) Color(0xFF1B5E20) else DepEdRed,
                            contentColor = Color.White,
                            disabledContainerColor = Color(0xFFE0E0E0),
                            disabledContentColor = Color(0xFF7A7A7A)
                        ),
                        enabled = !state.isLoading &&
                            !state.isCheckingGameStatus &&
                            !state.hasPlayedGame &&
                            state.pdfFile != null &&
                            (state.isCompleted || state.currentPage >= state.totalPages)
                    ) {
                        Icon(
                            if (state.isCompleted || state.hasPlayedGame) Icons.Default.CheckCircle else Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when {
                                state.hasPlayedGame -> "Game Already Played"
                                state.isCompleted -> "Play Learning Game"
                                else -> "Mark Module Completed"
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
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
                    val accentBlue = MaterialTheme.colorScheme.primary
                    val accentRed = MaterialTheme.colorScheme.tertiary
                    Surface(
                        color = accentBlue.copy(alpha = 0.14f),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = module.sub_level ?: module.level.replace("_", " "),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = accentBlue,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (!module.language.isNullOrEmpty()) {
                        Surface(
                            color = if (module.language == "Filipino") accentRed.copy(alpha = 0.14f) else accentBlue.copy(alpha = 0.14f),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = module.language,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (module.language == "Filipino") accentRed else accentBlue,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (module.strand != null) {
                    Text(
                        text = module.strand.code,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (!state.isDownloaded) {
                Button(
                    onClick = { moduleDetailViewModel.downloadAndOpen(module.id, state.currentPage) },
                    enabled = !state.isDownloading,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DepEdBlue, contentColor = Color.White)
                ) {
                    if (state.isDownloading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Downloading for offline reading…")
                    } else {
                        Icon(Icons.Outlined.CloudDownload, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Download for offline reading")
                    }
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
                if (state.isLoading) {
                    // Loading for online viewing or initial download
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
                            text = if (state.isDownloading) "Downloading Module for Offline Reading..." else "Opening Module for Online Viewing...",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = DepEdBlue
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (state.isDownloading) {
                                "The module is being saved on this device. You can keep reading once it finishes."
                            } else {
                                "You can read this module online now. Download it only if you also want offline access."
                            },
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
                            text = "Unable to open module",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = state.errorMessage ?: "Connect to the internet to view this module or download it for offline use.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.DarkGray,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { moduleDetailViewModel.loadModule(module) },
                            colors = ButtonDefaults.buttonColors(containerColor = DepEdBlue, contentColor = Color.White)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Try Viewing Online Again")
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
                } else if (state.pdfFile != null) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        CircularProgressIndicator(color = DepEdBlue)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Preparing module page…", color = DepEdBlue, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            text = "The module is ready to view online.",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = DepEdBlue
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Connect to the internet to read it, or download it for offline access.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.DarkGray,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
            }

            if (showModuleGame) {
                ModuleLearningGameDialog(
                    moduleTitle = module.title,
                    moduleLevel = module.level,
                    moduleSubLevel = module.sub_level,
                    moduleLanguage = module.language,
                    onDismiss = { showModuleGame = false },
                    onCompleted = { gameType, score, moves, elapsedSeconds ->
                        moduleDetailViewModel.saveModuleGameScore(
                            moduleId = module.id,
                            gameType = gameType,
                            score = score,
                            moves = moves,
                            elapsedSeconds = elapsedSeconds
                        )
                    }
                )
            }
        }
    }
}
