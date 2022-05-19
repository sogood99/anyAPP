package com.example.anyapp

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.anyapp.databinding.ActivityHomeBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

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

        // for creating new tweets
        binding.newTweetButton.setOnClickListener { button ->
            binding.newTweetButton.visibility = View.GONE
            binding.newTweet.visibility = View.VISIBLE
            true
        }

        // for choosing new button
        binding.choosePhotoBtn.setOnClickListener { button ->
            val takePicture = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val choosePicture = Intent(
                Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )
            try {
                MaterialAlertDialogBuilder(
                    this,
                    com.google.android.material.R.style.Theme_Material3_Dark_Dialog
                )
                    .setTitle("Image")
                    .setMessage("Choose Method")
                    .setNegativeButton("Take Picture") { dialog, which ->
                        startActivityForResult(takePicture, 1)
                    }
                    .setPositiveButton("Choose Gallery") { dialog, which ->
                        startActivityForResult(choosePicture, 1)
                    }
                    .show()
            } catch (e: ActivityNotFoundException) {

            }
            true
        }

        // Testing out tweets
        var tweetList = mutableListOf(
            Tweet(
                "ABC",
                "id",
                "Fuck Republicans and Democrats",
                "https://i.imgur.com/DvpvklR.png"
            ),
            Tweet(
                "ABC",
                "id",
                "Fuck Republicans and Democrats",
                "https://i.stack.imgur.com/DLadx.png"
            ),
            Tweet(
                "ABC",
                "id",
                "Fuck Republicans and Democrats",
                "https://i.imgur.com/DvpvklR.png"
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