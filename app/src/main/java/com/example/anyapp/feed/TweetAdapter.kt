package com.example.anyapp.feed

import android.app.Activity
import android.content.Intent
import android.graphics.Outline
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.Toast
import androidx.core.app.ActivityOptionsCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.anyapp.R
import com.example.anyapp.api.TweetApi
import com.example.anyapp.databinding.ItemTweetBinding
import com.example.anyapp.util.Constants.Companion.BASE_URL
import com.example.anyapp.util.LikeResponse
import com.example.anyapp.util.UserToken
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.HttpURLConnection
import androidx.core.util.Pair as UtilPair

class TweetAdapter(
    var tweets: List<Tweet>
) : RecyclerView.Adapter<TweetAdapter.TweetViewHolder>() {

    companion object {
        const val EXTRA_TWEET_ID = "com.example.anyapp.Tweet_ID"
        const val EXTRA_POSITION = "com.example.anyapp.RvPosition"
        const val EXTRA_VIDEO_POSITION = "com.example.anyapp.VideoPosition"
    }

    inner class TweetViewHolder(val binding: ItemTweetBinding) :
        RecyclerView.ViewHolder(binding.root)

    // for accessing backend api using retrofit
    private val retrofit = Retrofit
        .Builder().addConverterFactory(GsonConverterFactory.create())
        .baseUrl(BASE_URL)
        .build()
    private val tweetApi: TweetApi = retrofit.create(TweetApi::class.java)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TweetViewHolder {
        // how new item_tweets are created
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemTweetBinding.inflate(layoutInflater, parent, false)
        return TweetViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TweetViewHolder, position: Int) {
        // how the tweet.kt data class is synced with item_tweet
        holder.binding.apply {
            profileName.text = tweets[position].profileName
            username.text = "@" + tweets[position].username
            textContent.text = tweets[position].text

            var likeCountNum = tweets[position].likes
            likeCount.text = likeCountNum.toString()

            var isLikedTweet = tweets[position].isLiked
            // set color for button if liked
            if (isLikedTweet) {
                likeButton.setBackgroundColor(
                    root.resources.getColor(R.color.light_blue, root.context.theme)
                )
            } else {
                likeButton.setBackgroundColor(
                    root.resources.getColor(R.color.white, root.context.theme)
                )
            }

            val tweetId = tweets[position].tweetId
            likeButton.setOnClickListener {
                val call =
                    tweetApi.likeTweet(UserToken(it.context as Activity?).readToken(), tweetId)

                call.enqueue(object : Callback<LikeResponse> {
                    override fun onResponse(
                        call: Call<LikeResponse>,
                        response: Response<LikeResponse>
                    ) {
                        if (response.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                            Toast.makeText(root.context, "Please Log In", Toast.LENGTH_SHORT).show()
                        }
                        val respObj: LikeResponse = response.body() ?: return
                        Log.v("Pity", respObj.toString())
                        if (isLikedTweet != respObj.isLike) {
                            isLikedTweet = respObj.isLike
                            if (isLikedTweet) {
                                likeCountNum++
                                likeButton.setBackgroundColor(
                                    root.resources.getColor(R.color.light_blue, root.context.theme)
                                )
                            } else {
                                likeCountNum--
                                likeButton.setBackgroundColor(
                                    root.resources.getColor(R.color.white, root.context.theme)
                                )
                            }
                            likeCount.text = likeCountNum.toString()
                        }
                    }

                    override fun onFailure(call: Call<LikeResponse>, t: Throwable) {
                        Log.v("Pity", t.toString())
                    }
                })
            }

            // check if this tweet is a reply of another
            val repliesId = tweets[position].repliesId
            if (repliesId != null) {
                replyText.text = "replies to tweet@$repliesId"
                replyText.setOnClickListener {
                    val intent = Intent(root.context, TweetDetail::class.java).apply {
                        putExtra(EXTRA_TWEET_ID, repliesId)
                    }
                    root.context.startActivity(intent)
                }
            } else {
                val parent: ViewGroup? = replyText.parent as? ViewGroup
                parent?.let {
                    parent.removeView(replyText)
                }
            }

            if (tweets[position].imageUrl != null) {
                // load image if tweet.imageContent has content
                val url = BASE_URL + "/" + tweets[position].imageUrl
                Picasso.get().load(url).into(imageContent)
            } else {
                // otherwise delete it
                val parent: ViewGroup? = imageContent.parent as? ViewGroup
                parent?.let {
                    parent.removeView(imageContent)
                }
            }

            if (tweets[position].userIconUrl != "") {
                // load image if tweet.imageContent has content
                val url = BASE_URL + "/" + tweets[position].userIconUrl
                Picasso.get().load(url).into(userIcon)
            } else {
                val url = "$BASE_URL/image/userIcon/default.jpg"
                Picasso.get().load(url).into(userIcon)
            }

            if (tweets[position].videoUrl != null) {
                // rounded corners
                videoContent.clipToOutline = true
                videoContent.outlineProvider = object : ViewOutlineProvider() {
                    override fun getOutline(view: View?, outline: Outline?) {
                        if (view != null) {
                            outline?.setRoundRect(0, 0, view.width, view.height, 40F)
                        }
                    }
                }
                // same as image
                val url = BASE_URL + "/" + tweets[position].videoUrl
                url.let {
                    val player = ExoPlayer.Builder(videoContent.context).build()
                    videoContent.player = player
                    val mediaItem = MediaItem.fromUri(it)
                    player.setMediaItem(mediaItem)
                    player.prepare()
                }
            } else {
                val parent: ViewGroup? = videoContent.parent as? ViewGroup
                parent?.let {
                    parent.removeView(videoContent)
                }
            }

            tweetCard.setOnClickListener {
                // pause video if exists
                videoContent.player?.pause()
                // when clicked tweet card, start TweetDetail activity
                val intent = Intent(root.context, TweetDetail::class.java).apply {
                    putExtra(EXTRA_TWEET_ID, tweets[position].tweetId)
                    putExtra(EXTRA_POSITION, position)
                    putExtra(EXTRA_VIDEO_POSITION, videoContent.player?.contentPosition)
                }
                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    root.context as Activity,
                    UtilPair.create(userIcon as View, "userIcon$position"),
                    UtilPair.create(profileName as View, "profileName$position"),
                    UtilPair.create(username as View, "username$position"),
                    UtilPair.create(textContent as View, "textContent$position"),
                    UtilPair.create(imageContent as View, "imageContent$position"),
                    UtilPair.create(videoContent as View, "videoContent$position"),
                    UtilPair.create(replyText as View, "replyText$position"),
                )
                root.context.startActivity(intent, options.toBundle())
            }
        }
    }

    override fun getItemCount(): Int {
        return tweets.size
    }
}