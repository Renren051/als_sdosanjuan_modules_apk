package com.jannlawm.alssdosanjuan.data.model

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
