package com.example.anyapp.util

enum class FeedType{
    // Different types of feed ordering for FeedFragment parameter passing
    Popular, Following, Profile, Notifications
}

class Constants {
    companion object {
        const val BASE_URL = "http://10.0.2.2:8000"
    }
}