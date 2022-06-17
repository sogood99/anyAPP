package com.example.anyapp.api

import com.example.anyapp.Tweet
import com.example.anyapp.util.FeedType
import com.example.anyapp.util.LikeResponse
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
    ): Call<List<Tweet>>

    @FormUrlEncoded
    @POST("api/tweet/like/")
    fun likeTweet(
        @Header("Authorization") authorization: String?,
        @Field("tweet") tweet: Int,
    ): Call<LikeResponse>
}