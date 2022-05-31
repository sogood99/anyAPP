package com.example.anyapp.util

enum class FeedType {
    // Different types of feed ordering for FeedFragment parameter passing
    Popular, Following, Profile, Notifications
}

class Constants {
    companion object {
        var USER_TOKEN = "Token cb8bfb36c9f35898284afbb2f38636d1035aff4a"
        const val BASE_URL = "http://10.0.2.2:8000"
    }
}