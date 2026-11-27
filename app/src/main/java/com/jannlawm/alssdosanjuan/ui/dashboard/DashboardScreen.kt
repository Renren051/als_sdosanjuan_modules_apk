package com.jannlawm.alssdosanjuan.ui.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    onLogout: () -> Unit
) {
    val state by dashboardViewModel.uiState.collectAsState()
    val context = LocalContext.current
    var moduleToDelete by remember { mutableStateOf<ModuleItem?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(user.id) {
        dashboardViewModel.setUser(user)
    }

    LaunchedEffect(Unit) {
        dashboardViewModel.refreshDownloadedState()
        dashboardViewModel.refreshStorageStats()
    }

    LaunchedEffect(state.portalNoticeMessage) {
        state.portalNoticeMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            dashboardViewModel.clearPortalNotice()
        }
    }

    moduleToDelete?.let { module ->
        AlertDialog(
            onDismissRequest = { moduleToDelete = null },
            title = { Text("Delete downloaded module?") },
            text = {
                Text("Remove \"${module.title}\" from this device? You can download it again later.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        dashboardViewModel.deleteDownloadedModule(module.id)
                        moduleToDelete = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { moduleToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                NavigationBarItem(
                    selected = state.selectedBottomTab == 0,
                    onClick = { dashboardViewModel.selectBottomTab(0) },
                    icon = { Icon(Icons.Outlined.MenuBook, contentDescription = "Modules") },
                    label = { Text("Modules", fontWeight = FontWeight.SemiBold) }
                )
                NavigationBarItem(
                    selected = state.selectedBottomTab == 1,
                    onClick = { dashboardViewModel.selectBottomTab(1) },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (state.announcements.isNotEmpty()) {
                                    Badge { Text("${state.announcements.size}") }
                                }
                            }
                        ) {
                            Icon(Icons.Outlined.Campaign, contentDescription = "Advisories")
                        }
                    },
                    label = { Text("Advisories", fontWeight = FontWeight.SemiBold) }
                )
                NavigationBarItem(
                    selected = state.selectedBottomTab == 2,
                    onClick = { dashboardViewModel.selectBottomTab(2) },
                    icon = { Icon(Icons.Outlined.Person, contentDescription = "My Profile") },
                    label = { Text("My Profile", fontWeight = FontWeight.SemiBold) }
                )
            }
        },
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
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(
                            modifier = Modifier.weight(1f, fill = false),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "DepEd SDO San Juan",
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = user.full_name,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.90f),
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                Text(
                                    text = " \u2022 ",
                                    color = DepEdYellow,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = user.trackLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = DepEdYellow,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isDarkMode) Color(0xFF0F172A) else DepEdBlue
                ),
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        // Theme Toggle Button (Dark / Light Mode)
                        IconButton(
                            onClick = { onToggleDarkMode(!isDarkMode) },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isDarkMode) Color.White.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.15f)
                                )
                                .border(
                                    width = 1.dp,
                                    color = Color.White.copy(alpha = 0.35f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                        ) {
                            Icon(
                                painter = painterResource(
                                    id = if (isDarkMode) R.drawable.ic_light_mode else R.drawable.ic_dark_mode
                                ),
                                contentDescription = if (isDarkMode) "Switch to Light Mode" else "Switch to Dark Mode",
                                tint = if (isDarkMode) DepEdYellow else Color.White,
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        // Logout Button
                        OutlinedButton(
                            onClick = onLogout,
                            modifier = Modifier.height(38.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isDarkMode) Color(0xFF1E293B) else Color.Transparent,
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                text = "Logout",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            )
        },
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = {
                when (state.selectedBottomTab) {
                    1 -> dashboardViewModel.refreshAnnouncements()
                    2 -> {
                        dashboardViewModel.loadProfileAndDocuments()
                        dashboardViewModel.refreshStorageStats()
                    }
                    else -> dashboardViewModel.refresh()
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (state.selectedBottomTab) {
                1 -> StudentAdvisoriesContent(
                    state = state,
                    onRefreshAnnouncements = { dashboardViewModel.refreshAnnouncements() }
                )
                2 -> StudentProfileAndDocsContent(
                    user = user,
                    state = state,
                    onRefreshProfile = {
                        dashboardViewModel.loadProfileAndDocuments()
                        dashboardViewModel.refreshStorageStats()
                    },
                    onUploadDocument = { docType, uri ->
                        dashboardViewModel.uploadDocument(docType, uri)
                    },
                    onClearCompletedStorage = { dashboardViewModel.clearCompletedStorage() },
                    onClearAllStorage = { dashboardViewModel.clearAllStorage() }
                )
                else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Latest Division / CLC Advisory Banner (taps to Advisories tab)
                state.announcements.firstOrNull()?.let { latest ->
                    item {
                        Surface(
                            onClick = { dashboardViewModel.selectBottomTab(1) },
                            color = DepEdYellow.copy(alpha = 0.16f),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, DepEdYellow.copy(alpha = 0.6f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Campaign,
                                    contentDescription = null,
                                    tint = DepEdBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = latest.title,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${latest.clc_name ?: "Division Advisory"} · Tap to view all (${state.announcements.size})",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                // Offline Notice Banner
                if (state.isOffline) {
                    item {
                        Surface(
                            color = Color(0xFFFEF3C7),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Color(0xFFF59E0B)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Color(0xFFD97706),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Offline Mode Active",
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF92400E),
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                    Text(
                                        text = "Showing offline cache. You have ${state.downloadedModuleIds.size} downloaded modules available to read.",
                                        color = Color(0xFFB45309),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }

                // Enrolled Program Track Hero Banner
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFF002B80),
                                            Color(0xFF0E46B5)
                                        )
                                    )
                                )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                // DepEd Tricolor Stripe Accent
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth(0.35f)
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(99.dp))
                                ) {
                                    Box(modifier = Modifier.weight(1f).fillMaxHeight().background(DepEdBlue))
                                    Box(modifier = Modifier.weight(1f).fillMaxHeight().background(DepEdRed))
                                    Box(modifier = Modifier.weight(1f).fillMaxHeight().background(DepEdYellow))
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "ENROLLED PROGRAM TRACK",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = DepEdYellow,
                                            fontWeight = FontWeight.ExtraBold,
                                            letterSpacing = 1.sp
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = user.trackLabel,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White
                                        )
                                    }

                                    val totalCount = if (state.allTrackModules.isNotEmpty()) {
                                        state.allTrackModules.size
                                    } else {
                                        state.modules.size
                                    }

                                    Surface(
                                        color = Color.White.copy(alpha = 0.18f),
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f))
                                    ) {
                                        Text(
                                            text = "$totalCount Modules",
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Search Bar with Clear Button
                item {
                    OutlinedTextField(
                        value = state.searchQuery,
                        onValueChange = { dashboardViewModel.updateSearchQuery(it) },
                        placeholder = { Text("Search modules by title, code, or strand...") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = DepEdBlue
                            )
                        },
                        trailingIcon = {
                            if (state.searchQuery.isNotBlank()) {
                                IconButton(onClick = { dashboardViewModel.updateSearchQuery("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear search",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DepEdBlue,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        )
                    )
                }

                // Learning Strands Section
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Learning Strands",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = DepEdBlue
                            )
                            Text(
                                text = "${state.strands.size} Strands",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        val totalModulesCount = if (state.allTrackModules.isNotEmpty()) {
                            state.allTrackModules.size
                        } else {
                            state.strands.sumOf { it.modules_count }
                        }

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // "All Strands" Chip
                            item {
                                FilterChip(
                                    selected = state.selectedStrandId == null,
                                    onClick = { dashboardViewModel.filterByStrand(null) },
                                    label = {
                                        Text(
                                            text = "All Strands ($totalModulesCount)",
                                            fontWeight = if (state.selectedStrandId == null) FontWeight.Bold else FontWeight.Medium
                                        )
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = DepEdBlue,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }

                            // Dynamic Strand Chips with Genuine Module Counts
                            items(state.strands) { strand ->
                                FilterChip(
                                    selected = state.selectedStrandId == strand.id,
                                    onClick = {
                                        val newId = if (state.selectedStrandId == strand.id) null else strand.id
                                        dashboardViewModel.filterByStrand(newId)
                                    },
                                    label = {
                                        Text(
                                            text = "${strand.code} (${strand.modules_count})",
                                            fontWeight = if (state.selectedStrandId == strand.id) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = DepEdBlue,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                }

                // Curriculum Filters & Sub-Levels
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (user.isLearner) "Curriculum Filters" else "Track Sub-Levels",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = DepEdBlue
                            )
                            if (state.selectedStrandId != null || state.selectedSubLevel != null || state.selectedLanguage != null || state.filterDownloadedOnly) {
                                Text(
                                    text = "Reset All",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = DepEdRed,
                                    modifier = Modifier.clickable { dashboardViewModel.clearAllFilters() }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Downloaded Filter Pill
                            item {
                                FilterChip(
                                    selected = state.filterDownloadedOnly,
                                    onClick = { dashboardViewModel.toggleFilterDownloadedOnly() },
                                    label = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp),
                                                tint = if (state.filterDownloadedOnly) Color.White else Color(0xFF16A34A)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Downloaded (${state.downloadedModuleIds.size})",
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF16A34A),
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }

                            // Sub-Level / Language Pills
                            if (!user.isLearner || user.isBlp) {
                                item {
                                    FilterChip(
                                        selected = state.selectedSubLevel == "Basic Literacy",
                                        onClick = {
                                            dashboardViewModel.filterBySubLevel(
                                                if (state.selectedSubLevel == "Basic Literacy") null else "Basic Literacy"
                                            )
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        label = { Text("Basic Literacy") }
                                    )
                                }
                            }

                            if (!user.isLearner || user.isElementary) {
                                item {
                                    FilterChip(
                                        selected = state.selectedSubLevel == "Lower Elementary",
                                        onClick = {
                                            dashboardViewModel.filterBySubLevel(
                                                if (state.selectedSubLevel == "Lower Elementary") null else "Lower Elementary"
                                            )
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        label = { Text("Lower Elementary") }
                                    )
                                }
                                item {
                                    FilterChip(
                                        selected = state.selectedSubLevel == "Advanced Elementary",
                                        onClick = {
                                            dashboardViewModel.filterBySubLevel(
                                                if (state.selectedSubLevel == "Advanced Elementary") null else "Advanced Elementary"
                                            )
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        label = { Text("Advanced Elementary") }
                                    )
                                }
                            }

                            if (!user.isLearner || user.isJuniorHigh) {
                                item {
                                    FilterChip(
                                        selected = state.selectedSubLevel == "Junior High School",
                                        onClick = {
                                            dashboardViewModel.filterBySubLevel(
                                                if (state.selectedSubLevel == "Junior High School") null else "Junior High School"
                                            )
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        label = { Text("Junior High School") }
                                    )
                                }
                            }

                            item {
                                FilterChip(
                                    selected = state.selectedLanguage == "English",
                                    onClick = {
                                        dashboardViewModel.filterByLanguage(
                                            if (state.selectedLanguage == "English") null else "English"
                                        )
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    label = { Text("English") }
                                )
                            }

                            item {
                                FilterChip(
                                    selected = state.selectedLanguage == "Filipino",
                                    onClick = {
                                        dashboardViewModel.filterByLanguage(
                                            if (state.selectedLanguage == "Filipino") null else "Filipino"
                                        )
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    label = { Text("Filipino") }
                                )
                            }
                        }
                    }
                }

                // Section Header for Modules
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Self-Learning Modules (${state.modules.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = DepEdBlue
                        )
                    }
                }

                // Empty State
                if (state.modules.isEmpty() && !state.isLoading) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.SearchOff,
                                    contentDescription = null,
                                    modifier = Modifier.size(54.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "No Modules Found",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "No modules matched your current filter or search criteria.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { dashboardViewModel.clearAllFilters() },
                                    colors = ButtonDefaults.buttonColors(containerColor = DepEdBlue),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Reset All Filters", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Module Cards List
                items(state.modules, key = { it.id }) { module ->
                    val isDownloaded = state.downloadedModuleIds.contains(module.id)
                    val isDownloading = state.downloadingModuleIds.contains(module.id)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onModuleClick(module) },
                        shape = RoundedCornerShape(18.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            // Top Tag Badges Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Module Code Pill
                                val codeText = module.module_number ?: "M-${module.id}"
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = DepEdRed.copy(alpha = 0.10f),
                                    border = BorderStroke(1.dp, DepEdRed.copy(alpha = 0.25f))
                                ) {
                                    Text(
                                        text = codeText,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Black,
                                        color = DepEdRed
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    // Language Pill
                                    if (!module.language.isNullOrBlank()) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = DepEdBlue.copy(alpha = 0.10f)
                                        ) {
                                            Text(
                                                text = module.language,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = DepEdBlue,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    // Sub-Level Pill
                                    val subLevelLabel = module.sub_level ?: module.level
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Text(
                                            text = subLevelLabel,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Module Title
                            Text(
                                text = module.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                lineHeight = 21.sp
                            )

                            // Description
                            if (!module.description.isNullOrEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = module.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Bottom Row: Strand Info + Action Button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val strandName = module.strand?.title ?: module.strand?.name ?: module.strand?.code ?: "Curriculum Strand"
                                Row(
                                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Book,
                                        contentDescription = null,
                                        tint = DepEdBlue,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = strandName,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = DepEdBlue,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    // Download explicitly, or open the cached module.
                                    Surface(
                                        onClick = {
                                            if (isDownloaded) {
                                                onModuleClick(module)
                                            } else if (!isDownloading) {
                                                dashboardViewModel.downloadModule(module.id)
                                            }
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        color = when {
                                            isDownloaded -> Color(0xFF16A34A)
                                            isDownloading -> Color(0xFFE2E8F0)
                                            else -> MaterialTheme.colorScheme.surfaceVariant
                                        },
                                        contentColor = when {
                                            isDownloaded -> Color.White
                                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                                        },
                                        border = BorderStroke(
                                            1.dp,
                                            if (isDownloaded) Color(0xFF15803D) else MaterialTheme.colorScheme.outlineVariant
                                        ),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            if (isDownloading) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(14.dp),
                                                    strokeWidth = 2.dp,
                                                    color = DepEdBlue
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "Downloading...",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            } else if (isDownloaded) {
                                                Icon(
                                                    imageVector = Icons.Outlined.Book,
                                                    contentDescription = "Open module",
                                                    modifier = Modifier.size(15.dp),
                                                    tint = Color.White
                                                )
                                                Spacer(modifier = Modifier.width(5.dp))
                                                Text(
                                                    text = "Open",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Outlined.CloudDownload,
                                                    contentDescription = "Download Module",
                                                    modifier = Modifier.size(16.dp),
                                                    tint = DepEdBlue
                                                )
                                                Spacer(modifier = Modifier.width(5.dp))
                                                Text(
                                                    text = "Download",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = DepEdBlue
                                                )
                                            }
                                        }
                                    }

                                    if (isDownloaded) {
                                        Surface(
                                            onClick = { moduleToDelete = module },
                                            shape = RoundedCornerShape(10.dp),
                                            color = MaterialTheme.colorScheme.errorContainer,
                                            contentColor = MaterialTheme.colorScheme.onErrorContainer,
                                            border = BorderStroke(
                                                1.dp,
                                                MaterialTheme.colorScheme.error.copy(alpha = 0.35f)
                                            ),
                                            modifier = Modifier.size(34.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Outlined.DeleteOutline,
                                                    contentDescription = "Delete downloaded module",
                                                    modifier = Modifier.size(18.dp)
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
            }
        }
    }
}
