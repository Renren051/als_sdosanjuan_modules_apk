package com.jannlawm.alssdosanjuan.data.model

data class TeacherStudentsResponse(
    val success: Boolean,
    val count: Int = 0,
    val data: List<TeacherStudentItem>
)

data class TeacherStudentItem(
    val id: Int,
    val lrn: String? = null,
    val username: String,
    val full_name: String,
    val email: String? = null,
    val contact_number: String? = null,
    val street_address: String? = null,
    val barangay: String? = null,
    val city: String? = null,
    val guardian_name: String? = null,
    val guardian_relationship: String? = null,
    val emergency_contact_name: String? = null,
    val emergency_contact_number: String? = null,
    val is_4ps_beneficiary: Boolean = false,
    val is_ip: Boolean = false,
    val is_pwd: Boolean = false,
    val program_track: String? = null,
    val track_label: String? = null,
    val tracking_number: String? = null,
    val clc_id: Int? = null,
    val clc_name: String? = null,
    val enrollment_status: String? = null,
    val remarks: String? = null,
    val completed_count: Int = 0,
    val in_progress_count: Int = 0,
    val downloaded_count: Int = 0,
    val overall_progress_percent: Int = 0,
    val last_active_at: String? = null
)

data class TeacherModuleProgressResponse(
    val success: Boolean,
    val data: List<TeacherModuleProgress>,
    val meta: TeacherProgressMeta? = null
)

data class TeacherProgressMeta(
    val current_page: Int = 1,
    val last_page: Int = 1,
    val per_page: Int = 100,
    val total: Int = 0
)

data class TeacherModuleProgress(
    val learner_id: Int,
    val learner_name: String,
    val module_id: Int,
    val module_title: String,
    val strand_title: String? = null,
    val completion_status: String,
    val progress_percent: Int,
    val last_page_read: Int? = null,
    val total_pages: Int? = null,
    val last_opened_at: String? = null,
    val synced_at: String? = null,
    val latest_game_score: Int? = null,
    val latest_game_max_score: Int? = null,
    val latest_game_attempted_at: String? = null
)
