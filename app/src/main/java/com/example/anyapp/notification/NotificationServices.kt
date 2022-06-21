package com.example.anyapp.notification

import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.anyapp.R
import com.example.anyapp.api.AccountApi
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
        const val MS: Long = 10000
    }

    private var userToken: String? = null

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
                                        val builder = NotificationCompat.Builder(
                                            this@NotificationServices,
                                            "anyAppNotification"
                                        )
                                            .setSmallIcon(R.drawable.ic_like_icon)
                                            .setContentTitle("New Like")
                                            .setContentText("Hello World!")
                                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                            .setAutoCancel(true)

                                        with(NotificationManagerCompat.from(this@NotificationServices)) {
                                            notify(index, builder.build())
                                        }
                                    }
                                    "reply" -> {
                                        val builder = NotificationCompat.Builder(
                                            this@NotificationServices,
                                            "anyAppNotification"
                                        )
                                            .setSmallIcon(R.drawable.ic_tweet_icon)
                                            .setContentTitle("New Reply")
                                            .setContentText("Hello World!")
                                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                            .setAutoCancel(true)

                                        with(NotificationManagerCompat.from(this@NotificationServices)) {
                                            notify(index, builder.build())
                                        }
                                    }
                                    "follow" -> {
                                        val builder = NotificationCompat.Builder(
                                            this@NotificationServices,
                                            "anyAppNotification"
                                        )
                                            .setSmallIcon(R.drawable.ic_user_icon)
                                            .setContentTitle("New Follow")
                                            .setContentText("Followed by")
                                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
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
    }
}