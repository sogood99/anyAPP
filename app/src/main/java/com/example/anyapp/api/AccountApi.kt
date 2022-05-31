package com.example.anyapp.api

import com.example.anyapp.util.CreateUserResponse
import com.example.anyapp.util.LoginResponse
import com.example.anyapp.util.ProfileResponse
import retrofit2.Call
import retrofit2.http.*

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

    @GET("api/user/profile/")
    fun getProfile(
        @Header("Authorization") authorization: String,
    ): Call<ProfileResponse>
}