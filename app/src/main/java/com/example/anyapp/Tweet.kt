package com.example.anyapp

data class Tweet(
    val tweetId: Int,
    val userId: Int,
    val username: String,
    val text: String,
    val imageUrl: String?,
    val videoUrl: String?,
    val repliesId: Int,
    val createDate: String,
)
