package com.jannlawm.alssdosanjuan.ui.teacherprogress

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jannlawm.alssdosanjuan.data.model.TeacherModuleProgress
import com.jannlawm.alssdosanjuan.ui.theme.DepEdBlue
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherProgressScreen(
    onBack: () -> Unit,
    progressViewModel: TeacherProgressViewModel = viewModel()
) {
    val state by progressViewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    LaunchedEffect(searchQuery) {
        delay(300)
        progressViewModel.refresh(query = searchQuery)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Student Progress", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { progressViewModel.refresh(query = searchQuery, isPullToRefresh = true) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh progress")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DepEdBlue,
                    titleContentColor = androidx.compose.ui.graphics.Color.White,
                    navigationIconContentColor = androidx.compose.ui.graphics.Color.White,
                    actionIconContentColor = androidx.compose.ui.graphics.Color.White
                )
            )
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { progressViewModel.refresh(query = searchQuery, isPullToRefresh = true) },
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 8.dp),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    placeholder = { Text("Search learner or module") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )

                when {
                    state.isLoading -> {
                        Column(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = DepEdBlue)
                            Spacer(Modifier.height(12.dp))
                            Text("Loading learner progress…", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    state.errorMessage != null && state.rows.isEmpty() -> {
                        Column(
                            modifier = Modifier.weight(1f).fillMaxWidth().padding(24.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(state.errorMessage ?: "Could not load learner progress.")
                            Spacer(Modifier.height(12.dp))
                            Button(onClick = { progressViewModel.refresh(query = searchQuery) }, colors = ButtonDefaults.buttonColors(containerColor = DepEdBlue)) {
                                Text("Try again")
                            }
                        }
                    }
                    state.rows.isEmpty() -> {
                        Column(
                            modifier = Modifier.weight(1f).fillMaxWidth().padding(24.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "No matching module progress found.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(6.dp))
                            Text("Synced progress appears here after learner devices reconnect to the internet.", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            contentPadding = PaddingValues(top = 4.dp, bottom = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            item {
                                Text(
                                    "Showing ${state.rows.size} of ${state.totalRecords} module records",
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            items(state.rows, key = { "${it.learner_id}-${it.module_id}-${it.synced_at.orEmpty()}" }) { row ->
                                ProgressCard(row)
                            }
                            if (state.currentPage < state.lastPage) {
                                item {
                                    Button(
                                        onClick = { progressViewModel.loadMore(searchQuery) },
                                        enabled = !state.isLoadingMore,
                                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = DepEdBlue)
                                    ) {
                                        if (state.isLoadingMore) {
                                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                        } else {
                                            Text("Load more progress")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgressCard(progress: TeacherModuleProgress) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(progress.learner_name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(progress.module_title, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 3.dp))
            progress.strand_title?.takeIf { it.isNotBlank() }?.let {
                Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                LinearProgressIndicator(
                    progress = { progress.progress_percent.coerceIn(0, 100) / 100f },
                    modifier = Modifier.weight(1f).height(8.dp),
                    color = DepEdBlue,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Text("${progress.progress_percent}%", modifier = Modifier.padding(start = 10.dp), fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    "${progress.completion_status.replace('_', ' ').replaceFirstChar { it.uppercase() }} · Page ${progress.last_page_read ?: 0}/${progress.total_pages ?: "?"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text("Synced ${progress.synced_at?.take(10) ?: "—"}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (progress.latest_game_score != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Latest memory game: ${progress.latest_game_score}/${progress.latest_game_max_score ?: 100}",
                    style = MaterialTheme.typography.labelMedium,
                    color = DepEdBlue,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
