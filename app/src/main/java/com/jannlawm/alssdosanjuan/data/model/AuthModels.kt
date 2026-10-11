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

data class User(
    val id: Int,
    val lrn: String?,
    val username: String,
    val email: String,
    val role: String,
    val full_name: String
)

data class UserProfileResponse(
    val success: Boolean,
    val data: User
)
