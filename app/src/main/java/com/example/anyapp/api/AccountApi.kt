package com.example.anyapp.api

import com.example.anyapp.util.CreateUserResponse
import com.example.anyapp.util.LoginResponse
import com.example.anyapp.util.ProfileResponse
import retrofit2.Call
import retrofit2.http.*

interface AccountApi {
    @FormUrlEncoded
    @POST("api/user/create/")
    fun createUser(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("email") email: String,
    ): Call<CreateUserResponse>

    @FormUrlEncoded
    @POST("api/user/login/")
    fun login(
        @Field("username") username: String,
        @Field("password") password: String,
    ): Call<LoginResponse>

    @GET("api/user/profile/")
    fun getProfile(
        @Header("Authorization") authorization: String,
    ): Call<ProfileResponse>
}