package com.example.anyapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.anyapp.databinding.ActivityHomeBinding
import com.google.android.material.appbar.MaterialToolbar

class Home : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // For selecting the Menu Items
        binding.homeToolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.miSearch -> {
                    Log.v("Pity", "Clicked Search")
                    true
                }
                R.id.miLogout -> {
                    Log.v("Pity", "Clicked Logout")
                    true
                }
                else -> true
            }
        }

        // For selecting the Home
        binding.homeButton.setOnClickListener { button ->
            Log.v("Pity", "Clicked Home Button")
            true
        }

        // Testing out tweets
        var tweetList = mutableListOf(
            Tweet("ABC", false),
            Tweet("1223", true)
        )
        val adapter = TweetAdapter(tweetList)
        binding.homeTweets.adapter = adapter
        binding.homeTweets.layoutManager = LinearLayoutManager(this)


        // For selecting the Home
        binding.homeButton.setOnClickListener { button ->
            val title = "New One Bites the Dusto"
            val tweet = Tweet(title, false)
            tweetList.add(tweet)
            adapter.notifyItemInserted(tweetList.size)
            true
        }
    }
}