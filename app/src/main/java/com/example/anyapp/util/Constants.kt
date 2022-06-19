package com.example.anyapp.util

import android.app.Activity
import android.content.Context
import com.example.anyapp.R

enum class FeedType {
    // Different types of feed ordering for FeedFragment parameter passing
    Recent, Popular, Following, Profile, ProfileDetail, Notifications, Replies, Search, NotSet
}

class Constants {
    companion object {
        const val BASE_URL = "http://192.168.1.7:8000"
    }
}

class UserToken(val activity: Activity?) {
    val sharedPreferences = activity?.getSharedPreferences(
        activity.getString(R.string.preference_file_key),
        Context.MODE_PRIVATE
    )

    fun setToken(token: String?) {
        sharedPreferences?.edit()?.putString(activity?.getString(R.string.token_key), token)
            ?.apply()
    }

    fun readToken(): String? {
        return sharedPreferences?.getString(activity?.getString(R.string.token_key), null)
    }
}