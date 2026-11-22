package com.jannlawm.alssdosanjuan.ui.teacherprogress

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jannlawm.alssdosanjuan.R
import com.jannlawm.alssdosanjuan.data.model.TeacherModuleProgress
import com.jannlawm.alssdosanjuan.data.model.TeacherStudentItem
import com.jannlawm.alssdosanjuan.data.model.User
import com.jannlawm.alssdosanjuan.ui.theme.DepEdBlue
import com.jannlawm.alssdosanjuan.ui.theme.DepEdRed
import com.jannlawm.alssdosanjuan.ui.theme.DepEdYellow
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherProgressScreen(
    user: User?,
    isDarkMode: Boolean,
    onToggleDarkMode: (Boolean) -> Unit,
    onLogout: () -> Unit,
    progressViewModel: TeacherProgressViewModel = viewModel()
) {
    val state by progressViewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(searchQuery) {
        delay(300)
        progressViewModel.refresh(query = searchQuery)
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Sign Out", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to sign out from the Teacher Dashboard?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    }
                ) {
                    Text("Sign Out", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f))
                                .padding(2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.sdo_als_logo),
                                contentDescription = "SDO ALS Logo",
                                modifier = Modifier.size(34.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(verticalArrangement = Arrangement.Center) {
                            Text(
                                text = "Teacher Dashboard",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = user?.full_name ?: "Mobile Teacher",
                                style = MaterialTheme.typography.bodySmall,
                                color = DepEdYellow,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { onToggleDarkMode(!isDarkMode) }) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Dark Mode",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = { progressViewModel.refresh(query = searchQuery, isPullToRefresh = true) }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Sign Out",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DepEdBlue,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { progressViewModel.refresh(query = searchQuery, isPullToRefresh = true) },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                // Header Metric Summary Cards
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricMiniCard(
                        title = "My Students",
                        value = "${state.students.size}",
                        icon = Icons.Outlined.People,
                        color = DepEdBlue,
                        modifier = Modifier.weight(1f)
                    )
                    MetricMiniCard(
                        title = "Enrolled",
                        value = "${state.students.count { it.enrollment_status.equals("enrolled", ignoreCase = true) }}",
                        icon = Icons.Outlined.CheckCircle,
                        color = Color(0xFF10B981),
                        modifier = Modifier.weight(1f)
                    )
                    MetricMiniCard(
                        title = "Completed",
                        value = "${state.students.sumOf { it.completed_count }}",
                        icon = Icons.Outlined.School,
                        color = DepEdYellow,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Segmented Tabs: "My Students" vs "Module Telemetry"
                TabRow(
                    selectedTabIndex = state.selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = DepEdBlue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Tab(
                        selected = state.selectedTab == 0,
                        onClick = { progressViewModel.selectTab(0) },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.People, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("My Students (${state.students.size})", fontWeight = FontWeight.Bold)
                            }
                        }
                    )
                    Tab(
                        selected = state.selectedTab == 1,
                        onClick = { progressViewModel.selectTab(1) },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Module Telemetry (${state.rows.size})", fontWeight = FontWeight.Bold)
                            }
                        }
                    )
                }

                // Active Student Filter Banner (when filtering modules by student)
                if (state.selectedTab == 1 && state.selectedStudentName != null) {
                    Surface(
                        color = DepEdBlue.copy(alpha = 0.10f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, DepEdBlue.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Filtered to: ${state.selectedStudentName}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = DepEdBlue
                            )
                            IconButton(
                                onClick = { progressViewModel.clearStudentFilter() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Clear filter", tint = DepEdBlue, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    placeholder = {
                        Text(if (state.selectedTab == 0) "Search student name, LRN, track, or CLC" else "Search module title, strand, or student")
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )

                when {
                    state.isLoading -> {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = DepEdBlue)
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = if (state.selectedTab == 0) "Loading assigned students…" else "Loading module telemetry…",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    state.errorMessage != null && state.students.isEmpty() && state.rows.isEmpty() -> {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(24.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(state.errorMessage ?: "Could not load teacher data.")
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick = { progressViewModel.refresh(query = searchQuery) },
                                colors = ButtonDefaults.buttonColors(containerColor = DepEdBlue)
                            ) {
                                Text("Try again")
                            }
                        }
                    }

                    // TAB 0: MY STUDENTS
                    state.selectedTab == 0 -> {
                        if (state.students.isEmpty()) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.People,
                                    contentDescription = null,
                                    modifier = Modifier.size(54.dp),
                                    tint = DepEdBlue
                                )
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    "No assigned students found.",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "Learners enrolled under your CLC will appear here.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentPadding = PaddingValues(top = 4.dp, bottom = 24.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(state.students, key = { it.id }) { student ->
                                    StudentRosterCard(
                                        student = student,
                                        onViewProgress = { progressViewModel.filterByStudent(student) }
                                    )
                                }
                            }
                        }
                    }

                    // TAB 1: MODULE TELEMETRY
                    else -> {
                        if (state.rows.isEmpty()) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "No matching module progress found.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    "Synced progress appears here after learner devices reconnect to the internet.",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentPadding = PaddingValues(top = 4.dp, bottom = 24.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                item {
                                    Text(
                                        "Showing ${state.rows.size} of ${state.totalRecords} telemetry records",
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
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 4.dp),
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
}

@Composable
private fun MetricMiniCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
        border = BorderStroke(1.dp, color.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }
            Spacer(Modifier.height(2.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = color)
        }
    }
}

@Composable
private fun StudentRosterCard(
    student: TeacherStudentItem,
    onViewProgress: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Student Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(DepEdBlue.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        val initials = student.full_name
                            .split(" ")
                            .filter { it.isNotBlank() }
                            .take(2)
                            .map { it.first() }
                            .joinToString("")
                        Text(
                            text = initials.ifBlank { "L" },
                            fontWeight = FontWeight.Bold,
                            color = DepEdBlue,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = student.full_name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        student.lrn?.let { lrn ->
                            Text(
                                text = "LRN: $lrn",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Track Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = DepEdBlue.copy(alpha = 0.10f),
                    border = BorderStroke(1.dp, DepEdBlue.copy(alpha = 0.25f))
                ) {
                    Text(
                        text = student.track_label ?: student.program_track ?: "ALS",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = DepEdBlue
                    )
                }
            }

            // CLC & Barangay Information
            Spacer(Modifier.height(10.dp))
            student.clc_name?.let { clc ->
                Text(
                    text = "CLC: $clc",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            student.barangay?.let { brgy ->
                Text(
                    text = "Barangay: $brgy, San Juan City",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Progress Bar
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LinearProgressIndicator(
                    progress = { student.overall_progress_percent.coerceIn(0, 100) / 100f },
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(99.dp)),
                    color = DepEdBlue,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Text(
                    text = "${student.overall_progress_percent}%",
                    modifier = Modifier.padding(start = 10.dp),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelMedium
                )
            }

            // Stats Row & Action Button
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    CountPill(label = "Done", count = student.completed_count, color = Color(0xFF10B981))
                    CountPill(label = "Reading", count = student.in_progress_count, color = DepEdBlue)
                    CountPill(label = "Saved", count = student.downloaded_count, color = DepEdYellow)
                }

                TextButton(
                    onClick = onViewProgress,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text("Telemetry", fontWeight = FontWeight.Bold, color = DepEdBlue, fontSize = 12.sp)
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(14.dp), tint = DepEdBlue)
                }
            }
        }
    }
}

@Composable
private fun CountPill(label: String, count: Int, color: Color) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.12f),
        border = BorderStroke(0.5.dp, color.copy(alpha = 0.35f))
    ) {
        Text(
            text = "$label: $count",
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun ProgressCard(progress: TeacherModuleProgress) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
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
                    modifier = Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(99.dp)),
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
                    "Latest educational game: ${progress.latest_game_score}/${progress.latest_game_max_score ?: 100}",
                    style = MaterialTheme.typography.labelMedium,
                    color = DepEdBlue,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
