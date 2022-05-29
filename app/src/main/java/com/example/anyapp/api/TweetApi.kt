package com.example.anyapp.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

data class TweetResponse(
    val id: Int,
    val user: Int,
)

interface TweetApi {
    @Multipart
    @POST("api/tweet/")
    fun tweet(
        @Header("Authorization") token: String,
        @Part("text") text: RequestBody,
        @Part imageFile: MultipartBody.Part,
    ): Call<TweetResponse>
}