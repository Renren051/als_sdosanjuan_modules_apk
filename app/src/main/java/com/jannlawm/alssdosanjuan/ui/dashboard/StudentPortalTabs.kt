package com.jannlawm.alssdosanjuan.ui.dashboard

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannlawm.alssdosanjuan.data.model.AnnouncementItem
import com.jannlawm.alssdosanjuan.data.model.LearnerDocumentItem
import com.jannlawm.alssdosanjuan.data.model.User
import com.jannlawm.alssdosanjuan.ui.theme.DepEdBlue
import com.jannlawm.alssdosanjuan.ui.theme.DepEdRed
import com.jannlawm.alssdosanjuan.ui.theme.DepEdYellow
import java.util.Locale

private val REQUIRED_DOCUMENT_TYPES = listOf(
    "psa_birth_cert" to "PSA Birth Certificate",
    "barangay_clearance" to "Barangay Clearance / Residency",
    "form_137_sf10" to "DepEd Form 137 / SF10",
    "photo_id" to "2x2 Recent Identification Photo",
    "portfolio_document" to "Portfolio / Assessment Document"
)

@Composable
fun StudentAdvisoriesContent(
    state: DashboardUiState,
    onRefreshAnnouncements: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Division & CLC Advisories",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = DepEdBlue
                    )
                    Text(
                        text = "Official DepEd SDO San Juan ALS updates",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onRefreshAnnouncements) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh advisories", tint = DepEdBlue)
                }
            }
        }

        if (state.isLoadingAnnouncements && state.announcements.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = DepEdBlue)
                }
            }
        } else if (state.announcements.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Campaign,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = DepEdBlue
                        )
                        Spacer(Modifier.height(10.dp))
                        Text("No Active Advisories", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Division schedules, modular distribution alerts, and CLC announcements will appear here.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(state.announcements, key = { it.id }) { item ->
                AnnouncementCard(item)
            }
        }
    }
}

@Composable
fun AnnouncementCard(item: AnnouncementItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = if (item.is_pinned) 3.dp else 1.dp),
        border = BorderStroke(
            width = if (item.is_pinned) 1.5.dp else 1.dp,
            color = if (item.is_pinned) DepEdYellow else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = DepEdBlue.copy(alpha = 0.10f)
                ) {
                    Text(
                        text = item.clc_name ?: "Division-Wide",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = DepEdBlue,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (item.is_pinned) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = DepEdYellow.copy(alpha = 0.25f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.PushPin, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color(0xFF92400E))
                            Spacer(Modifier.width(4.dp))
                            Text("Pinned", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF92400E))
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            Text(text = item.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            if (!item.subtitle.isNullOrBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = DepEdBlue,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(8.dp))
            Text(
                text = item.body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )

            item.published_at?.take(10)?.let { date ->
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "Posted $date",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun StudentProfileAndDocsContent(
    user: User,
    state: DashboardUiState,
    onRefreshProfile: () -> Unit,
    onUploadDocument: (docType: String, uri: Uri) -> Unit,
    onClearCompletedStorage: () -> Unit,
    onClearAllStorage: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var pendingDocType by remember { mutableStateOf<String?>(null) }
    var showClearAllDialog by remember { mutableStateOf(false) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        val docType = pendingDocType
        pendingDocType = null
        if (uri != null && docType != null) {
            onUploadDocument(docType, uri)
        }
    }

    if (showClearAllDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllDialog = false },
            title = { Text("Clear All Downloaded Modules?", fontWeight = FontWeight.Bold) },
            text = {
                Text("This will delete all ${state.storageStats.totalFiles} offline PDF modules from this device. You can re-download them anytime.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearAllDialog = false
                        onClearAllStorage()
                    }
                ) {
                    Text("Clear All", color = DepEdRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    val detail = state.enrollmentDetail
    val clcName = detail?.clc?.clc_name ?: user.latest_enrollment?.clc?.clc_name ?: "Assigned Community Learning Center"
    val clcBarangay = detail?.clc?.barangay ?: user.latest_enrollment?.clc?.barangay
    val teacherName = detail?.assigned_teacher?.name ?: user.latest_enrollment?.assigned_teacher?.profile?.let {
        listOfNotNull(it.first_name, it.last_name).joinToString(" ").ifBlank { null }
    } ?: "Assigned Mobile Teacher"
    val teacherPhone = detail?.assigned_teacher?.contact_number
    val teacherEmail = detail?.assigned_teacher?.email ?: user.latest_enrollment?.assigned_teacher?.email

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. ALS Learner Identity & CLC Assignment Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(DepEdBlue.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Outlined.Badge, contentDescription = null, tint = DepEdBlue)
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(user.full_name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(
                                    text = "LRN: ${detail?.lrn ?: user.lrn ?: "Pending LRN"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        IconButton(onClick = onRefreshProfile) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh profile", tint = DepEdBlue)
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val statusText = detail?.status_label ?: user.latest_enrollment?.enrollment_status?.replaceFirstChar { it.uppercase() } ?: "Enrolled"
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF10B981).copy(alpha = 0.14f)
                        ) {
                            Text(
                                text = statusText,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF047857)
                            )
                        }
                        detail?.tracking_number?.let { trk ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = DepEdBlue.copy(alpha = 0.10f)
                            ) {
                                Text(
                                    text = trk,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = DepEdBlue
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    Spacer(Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = DepEdBlue, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text("Community Learning Center", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(clcName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            if (!clcBarangay.isNullOrBlank()) {
                                Text("Brgy. $clcBarangay, San Juan City", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.Top, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Outlined.Person, contentDescription = null, tint = DepEdBlue, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text("Assigned Mobile Teacher", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(teacherName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                teacherEmail?.let {
                                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }

                        if (!teacherPhone.isNullOrBlank()) {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(
                                    onClick = {
                                        try {
                                            context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$teacherPhone")))
                                        } catch (_: Exception) {}
                                    }
                                ) {
                                    Icon(Icons.Default.Call, contentDescription = "Call teacher", tint = Color(0xFF10B981))
                                }
                                IconButton(
                                    onClick = {
                                        try {
                                            context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$teacherPhone")))
                                        } catch (_: Exception) {}
                                    }
                                ) {
                                    Icon(Icons.Default.Sms, contentDescription = "Text teacher", tint = DepEdBlue)
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Enrollment Documents & Upload Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Description, contentDescription = null, tint = DepEdBlue)
                        Spacer(Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Enrollment Documents", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Submit PDF or clear photo (up to 10MB)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (state.isUploadingDocument || state.isLoadingProfile) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = DepEdBlue)
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    val docsByType = (detail?.documents ?: emptyList()).associateBy { it.document_type }

                    REQUIRED_DOCUMENT_TYPES.forEachIndexed { idx, (typeKey, defaultLabel) ->
                        val doc = docsByType[typeKey]
                        if (idx > 0) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                            )
                        }
                        DocumentChecklistRow(
                            label = doc?.type_label ?: defaultLabel,
                            document = doc,
                            isUploading = state.isUploadingDocument,
                            onUploadClick = {
                                pendingDocType = typeKey
                                filePickerLauncher.launch(arrayOf("application/pdf", "image/*"))
                            }
                        )
                    }
                }
            }
        }

        // 3. Offline Storage Manager Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Storage, contentDescription = null, tint = DepEdBlue)
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text("Offline Storage Manager", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                text = "${state.storageStats.totalFiles} downloaded module(s) · ${formatStorageSize(state.storageStats.totalBytes)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onClearCompletedStorage,
                            enabled = state.storageStats.completedDeletableCount > 0,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DepEdBlue)
                        ) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Clear Done (${state.storageStats.completedDeletableCount})",
                                maxLines = 1,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        OutlinedButton(
                            onClick = { showClearAllDialog = true },
                            enabled = state.storageStats.totalFiles > 0,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, DepEdRed.copy(alpha = 0.5f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = DepEdRed)
                        ) {
                            Text("Clear All", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DocumentChecklistRow(
    label: String,
    document: LearnerDocumentItem?,
    isUploading: Boolean,
    onUploadClick: () -> Unit
) {
    val statusColor = when (document?.verification_status) {
        "verified" -> Color(0xFF10B981)
        "pending" -> Color(0xFFF59E0B)
        "needs_resubmission", "rejected" -> DepEdRed
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val statusText = when (document?.verification_status) {
        "verified" -> "Verified"
        "pending" -> "Pending Review"
        "needs_resubmission" -> "Resubmit"
        "rejected" -> "Rejected"
        else -> "Not Submitted"
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
            Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = statusColor.copy(alpha = 0.14f)
                ) {
                    Text(
                        text = statusText,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
                if (document?.formatted_file_size != null) {
                    Text(
                        text = " · ${document.formatted_file_size}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            document?.verification_notes?.takeIf { it.isNotBlank() }?.let { note ->
                Text(
                    text = "Note: $note",
                    style = MaterialTheme.typography.labelSmall,
                    color = DepEdRed,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        if (document?.verification_status == "verified") {
            Icon(Icons.Default.CheckCircle, contentDescription = "Verified", tint = Color(0xFF10B981))
        } else {
            OutlinedButton(
                onClick = onUploadClick,
                enabled = !isUploading,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(14.dp), tint = DepEdBlue)
                Spacer(Modifier.width(4.dp))
                Text(
                    text = if (document == null) "Upload" else "Replace",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = DepEdBlue
                )
            }
        }
    }
}

private fun formatStorageSize(bytes: Long): String {
    return when {
        bytes < 1024L -> "$bytes B"
        bytes < 1024L * 1024L -> String.format(Locale.US, "%.1f KB", bytes / 1024.0)
        else -> String.format(Locale.US, "%.2f MB", bytes / (1024.0 * 1024.0))
    }
}

