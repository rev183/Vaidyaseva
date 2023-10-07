package com.mrknti.vaidyaseva.notifications

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.mrknti.vaidyaseva.Graph

class FCMNotificationService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d("FCM", "onMessageReceived: ${message.data}")
        val notification =
            Graph.moshi.adapter(Notification::class.java).fromJson(message.data["data"]!!)!!
        val notificationsManager = Graph.notificationsManager
        notificationsManager.handleNotification(notification)
    }
}