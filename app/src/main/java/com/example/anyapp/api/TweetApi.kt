package com.example.anyapp.api

import com.example.anyapp.Tweet
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface TweetApi {
    @Multipart
    @POST("api/tweet/")
    fun tweet(
        @Header("Authorization") token: String,
        @Part("text") text: RequestBody,
        @Part imageFile: MultipartBody.Part?,
    ): Call<Tweet>

    @GET("api/tweet/feed/")
    fun getFeed(): Call<List<Tweet>>
}