package com.jannlawm.alssdosanjuan.data.model

data class AnnouncementsResponse(
    val success: Boolean,
    val data: List<AnnouncementItem> = emptyList()
)

data class AnnouncementItem(
    val id: Int,
    val title: String,
    val subtitle: String? = null,
    val body: String,
    val target_audience: String? = null,
    val is_pinned: Boolean = false,
    val clc_id: Int? = null,
    val clc_name: String? = null,
    val published_at: String? = null
)

data class EnrollmentStatusResponse(
    val success: Boolean,
    val data: EnrollmentStatusDetail? = null
)

data class EnrollmentStatusDetail(
    val lrn: String? = null,
    val tracking_number: String? = null,
    val status: String? = null,
    val status_label: String? = null,
    val program_track: String? = null,
    val track_label: String? = null,
    val calendar_year: String? = null,
    val delivery_mode: String? = null,
    val schedule_preference: String? = null,
    val remarks: String? = null,
    val enrolled_at: String? = null,
    val clc: ClcInfo? = null,
    val assigned_teacher: AssignedTeacherDetail? = null,
    val documents: List<LearnerDocumentItem> = emptyList(),
    val profile: UserProfileDetail? = null
)

data class AssignedTeacherDetail(
    val id: Int? = null,
    val name: String? = null,
    val contact_number: String? = null,
    val email: String? = null
)

data class LearnerDocumentItem(
    val id: Int,
    val document_type: String,
    val type_label: String? = null,
    val file_name: String? = null,
    val formatted_file_size: String? = null,
    val verification_status: String = "pending",
    val verification_notes: String? = null,
    val created_at: String? = null
)

data class UserProfileDetail(
    val first_name: String? = null,
    val middle_name: String? = null,
    val last_name: String? = null,
    val contact_number: String? = null,
    val street_address: String? = null,
    val barangay: String? = null,
    val city: String? = null,
    val province: String? = null,
    val guardian_name: String? = null,
    val guardian_relationship: String? = null,
    val emergency_contact_number: String? = null
)

data class DocumentUploadResponse(
    val success: Boolean,
    val message: String? = null
)

data class SaveRemarkRequest(
    val remarks: String
)

data class SaveRemarkResponse(
    val success: Boolean,
    val message: String? = null
)

