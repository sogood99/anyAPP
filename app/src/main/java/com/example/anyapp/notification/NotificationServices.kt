package com.example.anyapp.notification

import android.app.PendingIntent
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.anyapp.R
import com.example.anyapp.api.AccountApi
import com.example.anyapp.feed.TweetAdapter
import com.example.anyapp.feed.TweetDetail
import com.example.anyapp.profile.ProfileDetail
import com.example.anyapp.util.Constants
import com.example.anyapp.util.NotificationResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*

class NotificationServices : android.app.Service() {
    private val retrofit = Retrofit
        .Builder().addConverterFactory(GsonConverterFactory.create())
        .baseUrl(Constants.BASE_URL)
        .build()
    private val accountApi = retrofit.create(AccountApi::class.java)
    private val timer = Timer()

    companion object {
        const val MS: Long = 2000
    }

    private var userToken: String? = null

    override fun onCreate() {
        super.onCreate()

//        val notification =
//            NotificationCompat.Builder(this, "anyAppNotification").setContentText("Test")
//                .setContentText("Text").build()
//        startForeground(1, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        userToken = intent?.extras?.getString("userToken")

        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                // get notification stack
                accountApi.getNotification(userToken).enqueue(
                    object : Callback<List<NotificationResponse>> {
                        override fun onResponse(
                            call: Call<List<NotificationResponse>>,
                            response: Response<List<NotificationResponse>>
                        ) {
                            val newNotifications = response.body()
                            NotificationList().append(newNotifications)

                            newNotifications?.forEachIndexed { index, notificationResponse ->
                                when (notificationResponse.type) {
                                    "like" -> {
                                        val intent = Intent(
                                            this@NotificationServices,
                                            TweetDetail::class.java
                                        ).apply {
                                            putExtra(
                                                TweetAdapter.EXTRA_TWEET_ID,
                                                notificationResponse.tweetId
                                            )
                                        }
                                        val pendingIntent = PendingIntent.getActivity(
                                            this@NotificationServices,
                                            0,
                                            intent,
                                            PendingIntent.FLAG_IMMUTABLE
                                        )

                                        val builder = NotificationCompat.Builder(
                                            this@NotificationServices,
                                            "anyAppNotification"
                                        )
                                            .setSmallIcon(R.drawable.ic_like_icon)
                                            .setContentTitle("New Like")
                                            .setContentText(notificationResponse.likeUserInfo)
                                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                            .setContentIntent(pendingIntent)
                                            .setAutoCancel(true)

                                        with(NotificationManagerCompat.from(this@NotificationServices)) {
                                            notify(index, builder.build())
                                        }
                                    }
                                    "reply" -> {
                                        val intent = Intent(
                                            this@NotificationServices,
                                            TweetDetail::class.java
                                        ).apply {
                                            putExtra(
                                                TweetAdapter.EXTRA_TWEET_ID,
                                                notificationResponse.tweetId
                                            )
                                        }
                                        val pendingIntent = PendingIntent.getActivity(
                                            this@NotificationServices,
                                            0,
                                            intent,
                                            PendingIntent.FLAG_IMMUTABLE
                                        )

                                        val builder = NotificationCompat.Builder(
                                            this@NotificationServices,
                                            "anyAppNotification"
                                        )
                                            .setSmallIcon(R.drawable.ic_tweet_icon)
                                            .setContentTitle("New Reply")
                                            .setContentText(notificationResponse.replyTweetBrief)
                                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                            .setContentIntent(pendingIntent)
                                            .setAutoCancel(true)

                                        with(NotificationManagerCompat.from(this@NotificationServices)) {
                                            notify(index, builder.build())
                                        }
                                    }
                                    "follow" -> {
                                        val intent = Intent(
                                            this@NotificationServices,
                                            ProfileDetail::class.java
                                        ).apply {
                                            putExtra(
                                                TweetAdapter.EXTRA_USER_ID,
                                                notificationResponse.followUserId
                                            )
                                        }
                                        val pendingIntent = PendingIntent.getActivity(
                                            this@NotificationServices,
                                            0,
                                            intent,
                                            PendingIntent.FLAG_IMMUTABLE
                                        )

                                        val builder = NotificationCompat.Builder(
                                            this@NotificationServices,
                                            "anyAppNotification"
                                        )
                                            .setSmallIcon(R.drawable.ic_user_icon)
                                            .setContentTitle("New Follow")
                                            .setContentText(notificationResponse.followUserInfo)
                                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                            .setContentIntent(pendingIntent)
                                            .setAutoCancel(true)

                                        with(NotificationManagerCompat.from(this@NotificationServices)) {
                                            notify(index, builder.build())
                                        }
                                    }
                                }
                            }
                        }

                        override fun onFailure(
                            call: Call<List<NotificationResponse>>,
                            t: Throwable
                        ) = Unit
                    }
                )
            }
        }, 0, MS)
        return START_STICKY
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        timer.cancel()
//        stopForeground(true)
//        stopSelf()
    }
}