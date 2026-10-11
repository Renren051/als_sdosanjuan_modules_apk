package com.jannlawm.alssdosanjuan.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "telemetry_logs")
data class TelemetryEntity(
    @PrimaryKey val moduleId: Int,
    val completionStatus: String, // "not_started" | "in_progress" | "completed"
    val lastPageRead: Int,
    val totalPages: Int,
    val lastOpenedAt: String,
    val isSynced: Boolean = false
)
