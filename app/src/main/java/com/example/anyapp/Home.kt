package com.example.anyapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.anyapp.databinding.ActivityHomeBinding

class Home : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Create binding to activity_home
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
            Tweet(
                "ABC",
                "id",
                "Fuck Republicans and Democrats",
                "https://www.google.co.jp/images/branding/googlelogo/1x/googlelogo_light_color_272x92dp.png"
            ),
            Tweet("1223", "nothaId", "Same Bruh", null)
        )
        val adapter = TweetAdapter(tweetList)
        binding.homeTweets.adapter = adapter
        binding.homeTweets.layoutManager = LinearLayoutManager(this)


        // For selecting the Home
        binding.homeButton.setOnClickListener { button ->
            val title = "New One Bites the Dusto"
            val tweet = Tweet("1223", "nothaId", "Same Bruh", null)
            tweetList.add(tweet)
            adapter.notifyItemInserted(tweetList.size)
            true
        }
    }
}