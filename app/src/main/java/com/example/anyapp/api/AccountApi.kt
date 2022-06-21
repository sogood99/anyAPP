package com.example.anyapp.api

import com.example.anyapp.util.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
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
    @POST("api/user/update/")
    fun updateUser(
        @Header("Authorization") authorization: String?,
        @Field("password") password: String?,
        @Field("newPassword") newPassword: String?,
    ): Call<ResponseBody>

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

    @FormUrlEncoded
    @POST("api/user/profile/")
    fun getProfileDetail(
        @Header("Authorization") authorization: String?,
        @Field("userId") userId: Int,
    ): Call<ProfileDetailResponse>

    @Multipart
    @POST("api/user/profile/update/")
    fun updateProfile(
        @Header("Authorization") token: String,
        @Part("profileName") profileName: RequestBody,
        @Part("profileInfo") profileInfo: RequestBody,
        @Part profileIcon: MultipartBody.Part?,
        @Part profileBkgImg: MultipartBody.Part?,
    ): Call<ProfileResponse>

    @FormUrlEncoded
    @POST("api/user/follow/")
    fun follow(
        @Header("Authorization") authorization: String?,
        @Field("followUserId") followedUserId: Int,
    ): Call<FollowResponse>

    @FormUrlEncoded
    @POST("api/user/follow/detail/")
    fun followDetail(
        @Header("Authorization") authorization: String?,
        @Field("userId") userId: Int?,
    ): Call<List<ProfileResponse>>

    @FormUrlEncoded
    @POST("api/user/block/")
    fun block(
        @Header("Authorization") authorization: String?,
        @Field("blockUserId") blockedUserId: Int?,
    ): Call<BlockResponse>

    @GET("api/user/block/detail/")
    fun blockDetail(
        @Header("Authorization") authorization: String?,
    ): Call<List<ProfileResponse>>

    @GET("api/user/notification/")
    fun getNotification(
        @Header("Authorization") authorization: String?,
    ): Call<List<NotificationResponse>>
}