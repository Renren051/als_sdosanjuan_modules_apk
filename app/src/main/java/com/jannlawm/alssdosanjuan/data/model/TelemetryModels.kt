package com.jannlawm.alssdosanjuan.data.model

data class SyncProgressRequest(
    val device_identifier: String,
    val items: List<TelemetryItem>
)

data class TelemetryItem(
    val module_id: Int,
    val completion_status: String, // "not_started" | "in_progress" | "completed"
    val last_page_read: Int,
    val total_pages: Int,
    val last_opened_at: String
)

data class SyncProgressResponse(
    val success: Boolean,
    val message: String?
)
