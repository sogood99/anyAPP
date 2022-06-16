package com.example.anyapp

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import com.example.anyapp.api.TweetApi
import com.example.anyapp.databinding.ActivityTweetDetailBinding
import com.example.anyapp.util.Constants
import com.example.anyapp.util.FeedType
import com.example.anyapp.util.UserToken
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

        val feedFragment = FeedFragment.newInstance(FeedType.Profile)
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.replyFeedLayout, feedFragment)
            commit()
        }

        binding.toolBar.setNavigationOnClickListener {
            finish()
        }

        val id = intent.getIntExtra(TweetAdapter.EXTRA_TWEET_ID, -1)
        if (id < 0) {
            // dont call without specifying tweetid
            finish()
        }

        val call = tweetApi.tweetDetail(UserToken(this).readToken(), id)
        call.enqueue(object : Callback<Tweet> {
            override fun onResponse(call: Call<Tweet>, response: Response<Tweet>) {
                Log.v("Pity", response.body().toString())
                val tweet = response.body()
                tweet?.let {
                    if (it.imageUrl != null) {
                        // load image if tweet.imageContent has content
                        val url = Constants.BASE_URL + "/" + it.imageUrl
                        Picasso.get().load(url).into(binding.imageContent)
                    } else {
                        // otherwise delete it
                        val parent: ViewGroup? = binding.imageContent.parent as? ViewGroup
                        parent?.let {
                            parent.removeView(binding.imageContent)
                        }
                    }
                }
            }

            override fun onFailure(call: Call<Tweet>, t: Throwable) {
                Log.v("Pity", t.toString())
            }
        })
    }

    override fun onBackPressed() {
        finish()
    }
}