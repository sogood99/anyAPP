package com.example.anyapp.notification

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.anyapp.databinding.ActivityNotificationBinding
import com.example.anyapp.databinding.ItemNotificationBinding
import com.example.anyapp.feed.TweetAdapter
import com.example.anyapp.feed.TweetDetail
import com.example.anyapp.profile.ProfileDetail

class Notification : AppCompatActivity() {
    private lateinit var binding: ActivityNotificationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNotificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            // setup back
            toolBar.setNavigationOnClickListener {
                finishAfterTransition()
            }

            // setup new notifications
            val notificationList = NotificationList().pop()
            notificationList.forEachIndexed { index, notificationResponse ->
                val notificationItem = ItemNotificationBinding.inflate(layoutInflater)
                when (notificationResponse.type) {
                    "reply" -> {
                        notificationItem.notificationInfo.text =
                            "Reply: " + notificationResponse.replyTweetBrief
                        notificationItem.userMenuButton.setOnClickListener {
                            val intent = Intent(root.context, TweetDetail::class.java).apply {
                                putExtra(TweetAdapter.EXTRA_TWEET_ID, notificationResponse.tweetId)
                            }
                            root.context.startActivity(intent)
                        }
                    }
                    "like" -> {
                        notificationItem.notificationInfo.text =
                            "Like from: " + notificationResponse.likeUserInfo
                        notificationItem.userMenuButton.setOnClickListener {
                            val intent = Intent(root.context, TweetDetail::class.java).apply {
                                putExtra(TweetAdapter.EXTRA_TWEET_ID, notificationResponse.tweetId)
                            }
                            root.context.startActivity(intent)
                        }
                    }
                    "follow" -> {
                        notificationItem.notificationInfo.text =
                            "Follow from: " + notificationResponse.followUserInfo
                        notificationItem.userMenuButton.setOnClickListener {
                            val intent = Intent(root.context, ProfileDetail::class.java).apply {
                                putExtra(
                                    TweetAdapter.EXTRA_USER_ID,
                                    notificationResponse.followUserId
                                )
                            }
                            root.context.startActivity(intent)
                        }
                    }
                }
                linearLayout.addView(notificationItem.root)
            }
        }
    }
}