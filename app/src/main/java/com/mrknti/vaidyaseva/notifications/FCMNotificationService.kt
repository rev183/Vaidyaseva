package com.mrknti.vaidyaseva.notifications

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.mrknti.vaidyaseva.Graph
import com.mrknti.vaidyaseva.data.network.handleError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class FCMNotificationService : FirebaseMessagingService() {

    private val TAG = "FCMNotificationService"

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val repository = Graph.authRepository
        val dataStoreManager = Graph.dataStoreManager
        val oldToken = runBlocking { dataStoreManager.fcmToken.first() }
        if (oldToken == token && dataStoreManager.isFCMRegistrationCompleted) {
            return
        }
        CoroutineScope(Dispatchers.IO).launch {
            if (dataStoreManager.isLoggedIn) {
                repository.registerFCMToken(token)
                    .handleError { Log.e(TAG, "Failed to register FCM token", it) }
                    .collect {
                        Graph.dataStoreManager.saveFCMToken(token)
                        Graph.dataStoreManager.isFCMRegistrationCompleted = true
                    }
            } else {
                dataStoreManager.saveFCMToken(token)
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d("FCM", "onMessageReceived: ${message.data}")
        val notificationData =
            Graph.moshi.adapter(NotificationData::class.java).fromJson(message.data["data"]!!)!!
        val notificationsManager = Graph.notificationsManager
        notificationsManager.handleNotification(notificationData)
    }
}