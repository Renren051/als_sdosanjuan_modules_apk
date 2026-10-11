package com.jannlawm.alssdosanjuan.data.model

data class LoginRequest(
    val login: String,
    val password: String,
    val device_name: String
)

data class LoginResponse(
    val success: Boolean,
    val data: LoginData
)

data class LoginData(
    val token: String,
    val user: User
)

data class ClcInfo(
    val id: Int? = null,
    val clc_name: String? = null,
    val barangay: String? = null
)

data class TeacherProfile(
    val first_name: String? = null,
    val last_name: String? = null
)

data class TeacherInfo(
    val id: Int? = null,
    val username: String? = null,
    val email: String? = null,
    val profile: TeacherProfile? = null
)

data class EnrollmentInfo(
    val id: Int? = null,
    val program_track: String? = null,
    val track_label: String? = null,
    val enrollment_status: String? = null,
    val clc: ClcInfo? = null,
    val assigned_teacher: TeacherInfo? = null
)

data class User(
    val id: Int,
    val lrn: String?,
    val username: String,
    val email: String,
    val role: String,
    val full_name: String,
    val latest_enrollment: EnrollmentInfo? = null
) {
    val isLearner: Boolean
        get() = role.equals("learner", ignoreCase = true)

    val programTrack: String?
        get() = latest_enrollment?.program_track

    val trackLabel: String
        get() = when (programTrack) {
            "BLP" -> "Basic Literacy Program (BLP)"
            "A&E_Elementary" -> "A&E Lower & Advanced Elementary"
            "A&E_Junior_High" -> "A&E Secondary (Junior High School)"
            else -> latest_enrollment?.track_label ?: "ALS Program"
        }

    val isBlp: Boolean
        get() = programTrack == "BLP"

    val isElementary: Boolean
        get() = programTrack == "A&E_Elementary"

    val isJuniorHigh: Boolean
        get() = programTrack == "A&E_Junior_High"
}

data class UserProfileResponse(
    val success: Boolean,
    val data: User
)
