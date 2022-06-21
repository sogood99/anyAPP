package com.example.anyapp.notification

import android.util.Log
import com.example.anyapp.util.NotificationResponse

class NotificationList {
    companion object {
        private val notificationList: MutableList<NotificationResponse> = mutableListOf()
    }

    fun append(newList: List<NotificationResponse>?) {
        if (newList == null) {
            return
        }
        for (notification in newList) {
            notificationList.add(notification)
        }
        Log.v("List", notificationList.toString())
    }

    fun pop(): List<NotificationResponse> {
        val retList = notificationList.toList()
        notificationList.clear()
        return retList
    }
}