package com.jannlawm.alssdosanjuan.ui.dashboard

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
                            Text(text = "DepEd SDO San Juan ALS", style = MaterialTheme.typography.titleSmall, color = Color.White, fontWeight = FontWeight.Bold)
                            Text(text = "${user.full_name} (${user.role.replaceFirstChar { it.uppercase() }})", style = MaterialTheme.typography.bodySmall, color = DepEdYellow)
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

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Learning Strands", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = DepEdBlue)
            Spacer(modifier = Modifier.height(8.dp))

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

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Self-Learning Modules", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = DepEdBlue)
            Spacer(modifier = Modifier.height(8.dp))

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = DepEdBlue)
                }
            } else if (state.modules.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No modules found.", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
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
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
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
                                            text = "Module #${module.module_number}",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = DepEdRed,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Surface(
                                        color = DepEdBlue.copy(alpha = 0.12f),
                                        shape = MaterialTheme.shapes.small
                                    ) {
                                        Text(
                                            text = module.level.replace("_", " "),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = DepEdBlue,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = module.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                if (!module.description.isNullOrEmpty()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = module.description, style = MaterialTheme.typography.bodyMedium, maxLines = 2, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                if (module.strand != null) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(text = "Strand: ${module.strand.title}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
