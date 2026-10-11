package com.jannlawm.alssdosanjuan.ui.module

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jannlawm.alssdosanjuan.data.model.ModuleItem
import com.jannlawm.alssdosanjuan.ui.theme.DepEdBlue
import com.jannlawm.alssdosanjuan.ui.theme.DepEdRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModuleDetailScreen(
    module: ModuleItem,
    moduleDetailViewModel: ModuleDetailViewModel = viewModel(),
    onBack: () -> Unit
) {
    val state by moduleDetailViewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Module Details", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DepEdBlue)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(text = "Module #${module.module_number}: ${module.title}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = DepEdBlue)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Level: ${module.level}", style = MaterialTheme.typography.bodyMedium, color = DepEdRed, fontWeight = FontWeight.SemiBold)
            if (module.strand != null) {
                Text(text = "Strand: ${module.strand.title}", style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (!module.description.isNullOrEmpty()) {
                Text(text = module.description, style = MaterialTheme.typography.bodyLarge)
            }

            Spacer(modifier = Modifier.height(24.dp))

            HorizontalDivider()

            Spacer(modifier = Modifier.height(24.dp))

            Text(text = "Offline Reading & Telemetry", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Current Page: ${state.lastPageRead} / ${state.totalPages}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Status: ${state.completionStatus}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val nextPage = if (state.lastPageRead < state.totalPages) state.lastPageRead + 1 else state.totalPages
                        val status = if (nextPage == state.totalPages) "completed" else "in_progress"
                        moduleDetailViewModel.updateReadingProgress(module.id, nextPage, state.totalPages, status)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = DepEdBlue)
                ) {
                    Text("Next Page")
                }

                Button(
                    onClick = {
                        moduleDetailViewModel.updateReadingProgress(module.id, state.totalPages, state.totalPages, "completed")
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = DepEdRed)
                ) {
                    Text("Mark Completed")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { moduleDetailViewModel.downloadModule(module.id) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DepEdBlue),
                enabled = !state.isDownloading
            ) {
                if (state.isDownloading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text(text = if (state.downloadedFile != null) "Module Downloaded (Cached)" else "Download PDF for Offline Reading")
                }
            }

            if (state.errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = state.errorMessage!!, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
