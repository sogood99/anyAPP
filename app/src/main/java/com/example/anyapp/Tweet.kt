package com.example.anyapp

data class Tweet(
    // check issue for documentation
    val tweetId: Int,
    val username: String,
    val userID: String,
    val textContent: String,
    val imageContent: String?,
    val videoContent: String?,
)