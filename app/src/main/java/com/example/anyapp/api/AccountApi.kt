package com.example.anyapp.api

import com.example.anyapp.util.CreateUserResponse
import com.example.anyapp.util.LoginResponse
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Part

interface AccountApi {
    @POST("api/user/create/")
    fun createUser(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("email") email: String,
    ): Call<CreateUserResponse>

    @POST("api/user/login/")
    fun login(
        @Part("username") username: String,
        @Part("password") password: String,
    ): Call<LoginResponse>

    @POST("api/user/profile")
    fun getProfile(
        @Header("Authorization") authorization: String,
    )
}