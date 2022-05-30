package com.example.anyapp.util

// Retro2 Api Response Data Classes
data class CreateUserResponse(
    val id: Int,
    val username: String,
    val email: String,
    val token: String,
)

data class LoginResponse(
    val token: String,
)

data class ProfileResponse(
    val userId: Int,
    val username: String,
    val profileName: String,
    val userIconUrl: String,
    val userBkgUrl: String,
    val createDate: String,
    val profileInfo: String,
)
