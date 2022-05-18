package com.example.anyapp

data class Tweet(
    // check issue for documentation
    val username: String,
    val userID: String,
    val textContent: String,
    val imageContent: String?,
)