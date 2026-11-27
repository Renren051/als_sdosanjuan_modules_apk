package com.jannlawm.alssdosanjuan.ui.teacherprogress

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannlawm.alssdosanjuan.data.model.TeacherStudentItem
import com.jannlawm.alssdosanjuan.ui.theme.DepEdBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherStudentDetailSheet(
    student: TeacherStudentItem,
    isSavingRemark: Boolean,
    onDismiss: () -> Unit,
    onSaveRemark: (learnerId: Int, remarks: String) -> Unit,
    onViewTelemetry: () -> Unit
) {
    val context = LocalContext.current
    var remarkText by remember(student.id, student.remarks) {
        mutableStateOf(student.remarks.orEmpty())
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = student.full_name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "LRN: ${student.lrn ?: "Not Assigned"} · ${student.tracking_number ?: student.username}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { shareStudentSummary(context, student) }) {
                    Icon(Icons.Default.Share, contentDescription = "Share progress summary", tint = DepEdBlue)
                }
            }

            // Track + Demographic Badges
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                DetailBadge(student.track_label ?: student.program_track ?: "ALS", DepEdBlue)
                DetailBadge(
                    student.enrollment_status?.replaceFirstChar { it.uppercase() } ?: "Enrolled",
                    Color(0xFF10B981)
                )
                if (student.is_4ps_beneficiary) DetailBadge("4Ps", Color(0xFF7C3AED))
                if (student.is_pwd) DetailBadge("PWD", Color(0xFFD97706))
                if (student.is_ip) DetailBadge("IP", Color(0xFF0284C7))
            }

            // Contact Actions (Call / SMS)
            val primaryPhone = student.contact_number ?: student.emergency_contact_number
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DepEdBlue.copy(alpha = 0.06f)),
                border = BorderStroke(1.dp, DepEdBlue.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Learner & Guardian Contact", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = DepEdBlue)

                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Outlined.Home, contentDescription = null, modifier = Modifier.size(16.dp), tint = DepEdBlue)
                        Spacer(Modifier.width(6.dp))
                        val addressParts = listOfNotNull(
                            student.street_address?.takeIf { it.isNotBlank() },
                            student.barangay?.takeIf { it.isNotBlank() }?.let { "Brgy. $it" },
                            student.city?.takeIf { it.isNotBlank() } ?: "San Juan City"
                        )
                        Text(
                            text = addressParts.joinToString(", "),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    if (!student.guardian_name.isNullOrBlank()) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(Icons.Outlined.People, contentDescription = null, modifier = Modifier.size(16.dp), tint = DepEdBlue)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Guardian: ${student.guardian_name}" +
                                    (student.guardian_relationship?.let { " ($it)" } ?: ""),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    if (!primaryPhone.isNullOrBlank()) {
                        Text(
                            text = "Phone: $primaryPhone",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    try {
                                        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$primaryPhone")))
                                    } catch (_: Exception) {}
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                            ) {
                                Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Call", fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = {
                                    try {
                                        context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$primaryPhone")))
                                    } catch (_: Exception) {}
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, DepEdBlue)
                            ) {
                                Icon(Icons.Default.Sms, contentDescription = null, modifier = Modifier.size(16.dp), tint = DepEdBlue)
                                Spacer(Modifier.width(6.dp))
                                Text("Text (SMS)", fontWeight = FontWeight.Bold, color = DepEdBlue)
                            }
                        }
                    } else {
                        Text(
                            text = "No phone number on file.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Quick Field Evaluation / Remarks
            Text("Field Evaluation & Remarks", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = remarkText,
                onValueChange = { remarkText = it },
                placeholder = { Text("Enter reading evaluation, portfolio notes, or home visit remark...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4,
                shape = RoundedCornerShape(12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onSaveRemark(student.id, remarkText) },
                    enabled = !isSavingRemark && remarkText.isNotBlank(),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DepEdBlue)
                ) {
                    if (isSavingRemark) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                    } else {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Save Remark", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
                OutlinedButton(
                    onClick = onViewTelemetry,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Outlined.Analytics, contentDescription = null, modifier = Modifier.size(16.dp), tint = DepEdBlue)
                    Spacer(Modifier.width(4.dp))
                    Text("Telemetry", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DepEdBlue)
                }
            }
        }
    }
}

@Composable
private fun DetailBadge(label: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.12f),
        border = BorderStroke(0.5.dp, color.copy(alpha = 0.3f))
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

fun shareStudentSummary(context: Context, student: TeacherStudentItem) {
    val summary = buildString {
        appendLine("DepEd ALS SDO San Juan City - Learner Progress Report")
        appendLine("------------------------------------------------")
        appendLine("Learner: ${student.full_name}")
        appendLine("LRN: ${student.lrn ?: "N/A"}")
        appendLine("Track: ${student.track_label ?: student.program_track ?: "ALS"}")
        appendLine("CLC: ${student.clc_name ?: "N/A"}")
        appendLine("Status: ${student.enrollment_status?.replaceFirstChar { it.uppercase() } ?: "Enrolled"}")
        appendLine("Overall Progress: ${student.overall_progress_percent}%")
        appendLine("Modules Completed: ${student.completed_count} | Reading: ${student.in_progress_count} | Saved: ${student.downloaded_count}")
        student.remarks?.takeIf { it.isNotBlank() }?.let {
            appendLine("Field Remarks: $it")
        }
    }
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "ALS Progress Summary - ${student.full_name}")
        putExtra(Intent.EXTRA_TEXT, summary)
    }
    try {
        context.startActivity(Intent.createChooser(intent, "Share Learner Progress"))
    } catch (_: Exception) {}
}
