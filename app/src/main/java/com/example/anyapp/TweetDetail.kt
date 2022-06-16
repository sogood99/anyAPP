package com.example.anyapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.anyapp.databinding.ActivityTweetDetailBinding
import com.example.anyapp.util.FeedType

class TweetDetail : AppCompatActivity() {
    private lateinit var binding: ActivityTweetDetailBinding

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
        binding.textContent.text = id.toString()
    }
}