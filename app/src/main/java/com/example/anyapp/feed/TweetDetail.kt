package com.example.anyapp.feed

import android.app.Activity
import android.content.Intent
import android.graphics.Outline
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.setPadding
import com.example.anyapp.NewTweetFragment
import com.example.anyapp.R
import com.example.anyapp.api.TweetApi
import com.example.anyapp.databinding.ActivityTweetDetailBinding
import com.example.anyapp.databinding.ItemUserBinding
import com.example.anyapp.profile.ProfileDetail
import com.example.anyapp.util.*
import com.example.anyapp.util.Constants.Companion.BASE_URL
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.HttpURLConnection

class TweetDetail : AppCompatActivity() {
    private lateinit var binding: ActivityTweetDetailBinding

    // lateinit
    private var tweetId = -1
    private var position = -1

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
        window.statusBarColor = ContextCompat.getColor(this, R.color.slight_light_blue)

        // get intent params
        tweetId = intent.getIntExtra(TweetAdapter.EXTRA_TWEET_ID, -1)
        position = intent.getIntExtra(TweetAdapter.EXTRA_POSITION, -1)
        val videoPlaybackPosition = intent.getLongExtra(TweetAdapter.EXTRA_VIDEO_POSITION, -1)
        // don't call without specifying tweetId
        assert(tweetId >= 0) { "Bug, do not use Tweet Detail w/o tweetId" }

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
        // hide newTweet when sent
        newTweetFragment.setTweetCallback {
            BottomSheetBehavior.from(binding.newTweet)
                .setState(BottomSheetBehavior.STATE_COLLAPSED)
        }

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

                        // setup like count/button
                        var likeCountNum = tweet.likes
                        likeCount.text = likeCountNum.toString()
                        likeCount.setOnClickListener {
                            drawerLayout.openDrawer(GravityCompat.END)
                        }
                        drawerLayout.setScrimColor(
                            ContextCompat.getColor(
                                this@TweetDetail,
                                android.R.color.transparent
                            )
                        )
                        val textView = TextView(this@TweetDetail)
                        textView.text = "Liked Users"
                        textView.textSize = 24f
                        textView.setPadding(20)
                        navigationViewLikedUsers.addHeaderView(textView)

                        // get all liked users
                        val likedUsersCall = tweetApi.likeDetail(tweetId)
                        likedUsersCall.enqueue(object : Callback<List<ProfileResponse>> {
                            override fun onResponse(
                                call: Call<List<ProfileResponse>>,
                                response: Response<List<ProfileResponse>>
                            ) {
                                response.body()?.let { profileList ->
                                    for (profile in profileList) {
                                        val itemUserBinding =
                                            ItemUserBinding.inflate(layoutInflater)

                                        // sync values
                                        itemUserBinding.profileName.text = profile.profileName
                                        itemUserBinding.username.text = profile.username
                                        Picasso.get().load(BASE_URL + "/" + profile.userIconUrl)
                                            .fit()
                                            .into(itemUserBinding.userIcon)
                                        navigationViewLikedUsers.addHeaderView(itemUserBinding.root)
                                    }
                                }
                            }

                            override fun onFailure(
                                call: Call<List<ProfileResponse>>,
                                t: Throwable
                            ) = Unit

                        })

                        var isLikedTweet = tweet.isLiked
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

                        val tweetId = tweet.tweetId
                        likeButton.setOnClickListener {
                            val likeCall = tweetApi.likeTweet(
                                UserToken(it.context as Activity?).readToken(),
                                tweetId
                            )

                            likeCall.enqueue(object : Callback<LikeResponse> {
                                override fun onResponse(
                                    call: Call<LikeResponse>,
                                    response: Response<LikeResponse>
                                ) {
                                    if (response.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                                        Toast.makeText(
                                            root.context,
                                            "Please Log In",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    val respObj: LikeResponse = response.body() ?: return
                                    Log.v("Pity", respObj.toString())
                                    if (isLikedTweet != respObj.isLike) {
                                        isLikedTweet = respObj.isLike
                                        if (isLikedTweet) {
                                            likeCountNum++
                                            likeButton.setBackgroundColor(
                                                root.resources.getColor(
                                                    R.color.light_blue,
                                                    root.context.theme
                                                )
                                            )
                                        } else {
                                            likeCountNum--
                                            likeButton.setBackgroundColor(
                                                root.resources.getColor(
                                                    R.color.white,
                                                    root.context.theme
                                                )
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

                        profileName.transitionName = "profileName$position"
                        username.transitionName = "username$position"
                        textContent.transitionName = "textContent$position"
                        likeButton.transitionName = "likeButton$position"
                        likeCount.transitionName = "likeCount$position"

                        // check if this tweet is a reply of another
                        val repliesId = tweet.repliesId
                        if (repliesId != null) {
                            replyText.text = "replies to tweet@$repliesId"
                            replyText.setOnClickListener {
                                val intent = Intent(root.context, TweetDetail::class.java).apply {
                                    putExtra(TweetAdapter.EXTRA_TWEET_ID, repliesId)
                                }
                                root.context.startActivity(intent)
                            }
                        } else {
                            replyText.visibility = View.GONE
                        }
                        replyText.transitionName = "replyText$position"

                        // usual imageUrl & videoUrl setting
                        if (it.userIconUrl != "") {
                            // load image if tweet.imageContent has content
                            val url = BASE_URL + "/" + it.userIconUrl
                            Picasso.get().load(url).into(userIcon)
                        } else {
                            val url = "$BASE_URL/image/userIcon/default.jpg"
                            Picasso.get().load(url).into(userIcon)
                        }
                        // set transition name for activity shared element transition
                        userIcon.transitionName = "userIcon$position"

                        if (it.videoUrl != null) {
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
                            val url = BASE_URL + "/" + it.videoUrl
                            url.let {
                                val player = ExoPlayer.Builder(videoContent.context).build()
                                videoContent.player = player
                                val mediaItem = MediaItem.fromUri(it)
                                player.setMediaItem(mediaItem)
                                player.prepare()
                                if (videoPlaybackPosition > 0) {
                                    player.seekTo(videoPlaybackPosition)
                                }
                            }
                        } else {
                            val parent: ViewGroup? = videoContent.parent as? ViewGroup
                            parent?.let {
                                parent.removeView(videoContent)
                            }
                        }

                        if (it.imageUrl != null) {
                            // load image if tweet.imageContent has content
                            val url = BASE_URL + "/" + it.imageUrl
                            Picasso.get().load(url).into(imageContent)
                        } else {
                            // otherwise delete it
                            val parent: ViewGroup? = imageContent.parent as? ViewGroup
                            parent?.let {
                                parent.removeView(imageContent)
                            }
                        }
                        imageContent.transitionName = "imageContent$position"

                        // allow click on profile info
                        val profileClickListener = View.OnClickListener { _ ->
                            val intent = Intent(root.context, ProfileDetail::class.java).apply {
                                putExtra(TweetAdapter.EXTRA_USER_ID, it.userId)
                            }
                            root.context.startActivity(intent)
                        }
                        userIcon.setOnClickListener(profileClickListener)
                        username.setOnClickListener(profileClickListener)
                        profileName.setOnClickListener(profileClickListener)

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
        binding.videoContent.player?.stop()
        finishAfterTransition()
    }
}