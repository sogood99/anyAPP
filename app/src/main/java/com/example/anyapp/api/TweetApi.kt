package com.example.anyapp.api

import com.example.anyapp.feed.Tweet
import com.example.anyapp.profile.ProfileDetail
import com.example.anyapp.util.FeedType
import com.example.anyapp.util.LikeResponse
import com.example.anyapp.util.ProfileResponse
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
        @Part("repliesId") replyId: Int?,
        @Part imageFile: MultipartBody.Part?,
        @Part videoFile: MultipartBody.Part?,
    ): Call<Tweet>

    @FormUrlEncoded
    @POST("api/tweet/detail/")
    fun tweetDetail(
        @Header("Authorization") token: String?,
        @Field("tweet") tweetId: Int,
    ): Call<Tweet>

    @FormUrlEncoded
    @POST("api/tweet/feed/")
    fun getFeed(
        @Header("Authorization") authorization: String?,
        @Field("option") options: FeedType?,
        @Field("repliesId") repliesId: Int?,
        @Field("userId") userId: Int?,
    ): Call<List<Tweet>>

    @FormUrlEncoded
    @POST("api/tweet/like/")
    fun likeTweet(
        @Header("Authorization") authorization: String?,
        @Field("tweet") tweet: Int,
    ): Call<LikeResponse>

    @FormUrlEncoded
    @POST("api/tweet/like/detail/")
    fun likeDetail(
        @Field("tweet") tweet: Int,
    ): Call<List<ProfileResponse>>

    @FormUrlEncoded
    @POST("api/tweet/search/")
    fun search(
        @Header("Authorization") authorization: String?,
        @Field("searchArg") searchArg: String,
    ): Call<List<Tweet>>
}