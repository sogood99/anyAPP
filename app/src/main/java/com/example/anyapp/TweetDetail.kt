package com.example.anyapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import com.example.anyapp.api.TweetApi
import com.example.anyapp.databinding.ActivityTweetDetailBinding
import com.example.anyapp.util.Constants
import com.example.anyapp.util.FeedType
import com.example.anyapp.util.UserToken
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class TweetDetail : AppCompatActivity() {
    private lateinit var binding: ActivityTweetDetailBinding

    private val retrofit = Retrofit
        .Builder().addConverterFactory(GsonConverterFactory.create())
        .baseUrl(Constants.BASE_URL)
        .build()
    private val tweetApi = retrofit.create(TweetApi::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTweetDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        postponeEnterTransition()

        // get intent params
        val tweetId = intent.getIntExtra(TweetAdapter.EXTRA_TWEET_ID, -1)
        val position = intent.getIntExtra(TweetAdapter.EXTRA_POSITION, -1)
        if (tweetId < 0) {
            // dont call without specifying tweetid
            finish()
        }

        // set replies
        val feedFragment = FeedFragment.newInstance(FeedType.Replies, repliesId = tweetId)
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.replyFeedLayout, feedFragment)
            commit()
        }

        // set back button
        binding.toolBar.setNavigationOnClickListener {
            onBackPressed()
        }

        // put in new tweet fragment
        val newTweetFragment = NewTweetFragment.newInstance(isReply = true, replyId = tweetId)
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.newTweet, newTweetFragment)
        transaction.addToBackStack("NewTweet")
        transaction.commit()

        // its bottomsheet style
        BottomSheetBehavior.from(binding.newTweet).apply {
            peekHeight = 100
            state = BottomSheetBehavior.STATE_COLLAPSED
        }

        // bottom sheet set
        BottomSheetBehavior.from(binding.newTweet)
            .addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(view: View, newState: Int) {
                    val newTweetFragment: NewTweetFragment =
                        supportFragmentManager.findFragmentById(R.id.newTweet) as NewTweetFragment
                    when (newState) {
                        BottomSheetBehavior.STATE_EXPANDED -> {
                            newTweetFragment.show()
                        }
                        BottomSheetBehavior.STATE_COLLAPSED -> {
                            newTweetFragment.hide()
                        }
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) = Unit

            })


        val call = tweetApi.tweetDetail(UserToken(this).readToken(), tweetId)
        call.enqueue(object : Callback<Tweet> {
            override fun onResponse(call: Call<Tweet>, response: Response<Tweet>) {
                Log.v("Pity", response.body().toString())
                val tweet = response.body()
                tweet?.let {
                    binding.apply {
                        // set all initial data
                        profileName.text = tweet.profileName
                        username.text = "@" + tweet.username
                        textContent.text = tweet.text

                        profileName.transitionName = "profileName$position"
                        username.transitionName = "username$position"
                        textContent.transitionName = "textContent$position"

                        // usual imageUrl & videoUrl setting
                        if (it.userIconUrl != "") {
                            // load image if tweet.imageContent has content
                            val url = Constants.BASE_URL + "/" + it.userIconUrl
                            Picasso.get().load(url).into(userIcon)
                        } else {
                            val url = "${Constants.BASE_URL}/image/userIcon/default.jpg"
                            Picasso.get().load(url).into(userIcon)
                        }
                        // set transition name for activity shared element transition
                        userIcon.transitionName = "userIcon$position"

                        if (it.videoUrl != null) {
                            // same as image
                            val url = Constants.BASE_URL + "/" + it.videoUrl
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

                        if (it.imageUrl != null) {
                            // load image if tweet.imageContent has content
                            val url = Constants.BASE_URL + "/" + it.imageUrl
                            Picasso.get().load(url).into(imageContent)
                        } else {
                            // otherwise delete it
                            val parent: ViewGroup? = imageContent.parent as? ViewGroup
                            parent?.let {
                                parent.removeView(imageContent)
                            }
                        }
                        imageContent.transitionName = "imageContent$position"

                        startPostponedEnterTransition()
                    }
                }
            }

            override fun onFailure(call: Call<Tweet>, t: Throwable) {
                Log.v("Pity", t.toString())
            }
        })
    }

    override fun onBackPressed() {
        finishAfterTransition()
    }
}