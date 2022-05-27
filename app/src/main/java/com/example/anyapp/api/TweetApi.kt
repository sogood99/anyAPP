package com.example.anyapp.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface TweetApi {
    @Multipart
    @POST("api/tweet/")
    fun tweet(
        @Part("userId") userId: RequestBody,
        @Part imageFile: MultipartBody.Part,
    ): Call<ResponseBody>
}