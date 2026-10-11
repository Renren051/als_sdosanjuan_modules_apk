package com.jannlawm.alssdosanjuan.ui.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jannlawm.alssdosanjuan.R
import com.jannlawm.alssdosanjuan.data.model.ModuleItem
import com.jannlawm.alssdosanjuan.data.model.Strand
import com.jannlawm.alssdosanjuan.data.model.User
import com.jannlawm.alssdosanjuan.ui.theme.DepEdBlue
import com.jannlawm.alssdosanjuan.ui.theme.DepEdRed
import com.jannlawm.alssdosanjuan.ui.theme.DepEdYellow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    user: User,
    isDarkMode: Boolean,
    onToggleDarkMode: (Boolean) -> Unit,
    dashboardViewModel: DashboardViewModel = viewModel(),
    onModuleClick: (ModuleItem) -> Unit,
    onOpenChat: () -> Unit,
    onLogout: () -> Unit
) {
    val state by dashboardViewModel.uiState.collectAsState()

    LaunchedEffect(user.id) {
        dashboardViewModel.setUser(user)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.sdo_als_logo),
                            contentDescription = "SDO ALS Logo",
                            modifier = Modifier.size(38.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "DepEd SDO San Juan ALS",
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${user.full_name} • ${user.trackLabel}",
                                style = MaterialTheme.typography.bodySmall,
                                color = DepEdYellow,
                                maxLines = 1
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DepEdBlue),
                actions = {
                    // Dark/Light Mode Toggle
                    IconButton(onClick = { onToggleDarkMode(!isDarkMode) }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Toggle Theme",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = onOpenChat) {
                        Icon(Icons.Default.Star, contentDescription = "ALSJay Chat", tint = DepEdYellow)
                    }
                    TextButton(onClick = onLogout) {
                        Text("Logout", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onOpenChat,
                containerColor = DepEdRed,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Star, contentDescription = null) },
                text = { Text("Ask ALSJay AI", fontWeight = FontWeight.Bold) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Enrolled Program Track Banner
            Surface(
                color = DepEdBlue.copy(alpha = 0.08f),
                shape = MaterialTheme.shapes.medium,
                border = BorderStroke(1.dp, DepEdBlue.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "ENROLLED PROGRAM TRACK",
                            style = MaterialTheme.typography.labelSmall,
                            color = DepEdBlue,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = user.trackLabel,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Surface(
                        color = DepEdBlue,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = "${state.modules.size} Modules",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { dashboardViewModel.updateSearchQuery(it) },
                label = { Text("Search modules by title...") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(text = "Learning Strands", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = DepEdBlue)
            Spacer(modifier = Modifier.height(6.dp))

            // Strands Horizontal List
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = state.selectedStrandId == null,
                        onClick = { dashboardViewModel.filterByStrand(null) },
                        label = { Text("All Strands") }
                    )
                }
                items(state.strands) { strand ->
                    FilterChip(
                        selected = state.selectedStrandId == strand.id,
                        onClick = { dashboardViewModel.filterByStrand(strand.id) },
                        label = { Text("${strand.code} (${strand.modules_count})") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sub-Level & Language Filter Chips Scoped to Enrolled Track
            Text(
                text = if (user.isLearner) "Program Sub-Levels" else "Curriculum Program Tracks",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = DepEdBlue
            )
            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = state.selectedSubLevel == null && state.selectedLanguage == null,
                        onClick = {
                            dashboardViewModel.filterBySubLevel(null)
                            dashboardViewModel.filterByLanguage(null)
                        },
                        label = { Text("All Track Modules") }
                    )
                }

                if (!user.isLearner || user.isBlp) {
                    item {
                        FilterChip(
                            selected = state.selectedSubLevel == "Basic Literacy",
                            onClick = {
                                dashboardViewModel.filterBySubLevel(if (state.selectedSubLevel == "Basic Literacy") null else "Basic Literacy")
                            },
                            label = { Text("Basic Literacy") }
                        )
                    }
                }

                if (!user.isLearner || user.isElementary) {
                    item {
                        FilterChip(
                            selected = state.selectedSubLevel == "Lower Elementary",
                            onClick = {
                                dashboardViewModel.filterBySubLevel(if (state.selectedSubLevel == "Lower Elementary") null else "Lower Elementary")
                            },
                            label = { Text("Lower Elementary") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = state.selectedSubLevel == "Advanced Elementary",
                            onClick = {
                                dashboardViewModel.filterBySubLevel(if (state.selectedSubLevel == "Advanced Elementary") null else "Advanced Elementary")
                            },
                            label = { Text("Advanced Elementary") }
                        )
                    }
                }

                if (!user.isLearner || user.isJuniorHigh) {
                    item {
                        FilterChip(
                            selected = state.selectedSubLevel == "Junior High School",
                            onClick = {
                                dashboardViewModel.filterBySubLevel(if (state.selectedSubLevel == "Junior High School") null else "Junior High School")
                            },
                            label = { Text("Junior High School") }
                        )
                    }
                }

                item {
                    FilterChip(
                        selected = state.selectedLanguage == "English",
                        onClick = {
                            dashboardViewModel.filterByLanguage(if (state.selectedLanguage == "English") null else "English")
                        },
                        label = { Text("English") }
                    )
                }
                item {
                    FilterChip(
                        selected = state.selectedLanguage == "Filipino",
                        onClick = {
                            dashboardViewModel.filterByLanguage(if (state.selectedLanguage == "Filipino") null else "Filipino")
                        },
                        label = { Text("Filipino") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Self-Learning Modules (${state.modules.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = DepEdBlue
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = DepEdBlue)
                }
            } else if (state.modules.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No modules found for this filter.", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.modules) { module ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onModuleClick(module) },
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = DepEdRed.copy(alpha = 0.12f),
                                        shape = MaterialTheme.shapes.small
                                    ) {
                                        Text(
                                            text = module.module_number ?: "Module",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = DepEdRed,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        if (!module.language.isNullOrEmpty()) {
                                            Surface(
                                                color = if (module.language == "Filipino") DepEdRed.copy(alpha = 0.15f) else DepEdBlue.copy(alpha = 0.15f),
                                                shape = MaterialTheme.shapes.small
                                            ) {
                                                Text(
                                                    text = module.language,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = if (module.language == "Filipino") DepEdRed else DepEdBlue,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                        Surface(
                                            color = DepEdBlue.copy(alpha = 0.12f),
                                            shape = MaterialTheme.shapes.small
                                        ) {
                                            Text(
                                                text = module.sub_level ?: module.level.replace("_", " "),
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                style = MaterialTheme.typography.labelMedium,
                                                color = DepEdBlue,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = module.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                if (!module.description.isNullOrEmpty()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = module.description,
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 2,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (module.strand != null) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Strand: ${module.strand.title ?: module.strand.name ?: module.strand.code}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
