package com.example.anyapp.search

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.anyapp.databinding.ActivityTweetSearchBinding

class TweetSearch : AppCompatActivity() {
    private lateinit var binding: ActivityTweetSearchBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTweetSearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}