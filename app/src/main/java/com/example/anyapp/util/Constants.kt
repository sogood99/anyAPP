package com.example.anyapp.util

enum class FeedType {
    // Different types of feed ordering for FeedFragment parameter passing
    Popular, Following, Profile, Notifications
}

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

class Constants {
    companion object {
        var USER_TOKEN = "Token 4e3fcbc5125e1fc54a2a22cd6004fa88a8d8d4b4"
        const val BASE_URL = "http://10.0.2.2:8000"
    }
}