package com.example.anyapp

data class Tweet(
    val tweetId: Int,
    val userId: Int,
    val userIconUrl: String,
    val username: String,
    val text: String,
    val imageUrl: String?,
    val videoUrl: String?,
    val repliesId: Int,
    val createDate: String,
    val likes: Int,
    val isLiked: Boolean,
)
