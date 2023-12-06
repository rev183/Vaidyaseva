package com.mrknti.vaidyaseva.notifications

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.mrknti.vaidyaseva.Graph
import com.mrknti.vaidyaseva.data.network.handleError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class FCMNotificationService : FirebaseMessagingService() {

    private val TAG = "FCMNotificationService"

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val repository = Graph.authRepository
        val dataStoreManager = Graph.dataStoreManager
        CoroutineScope(Dispatchers.IO).launch {
            delay(1000)
            val oldToken = dataStoreManager.fcmToken.first()
            if (oldToken == token && dataStoreManager.isFCMRegistrationCompleted) {
                return@launch
            }
            if (dataStoreManager.isLoggedIn) {
                Log.d(TAG, "Register FCM receiver oldToken: $oldToken, newToken: $token")
                repository.registerFCMToken(token, dataStoreManager.getRegisteredDevice().first())
                    .handleError { Log.e(TAG, "Failed to register FCM token", it) }
                    .collect {
                        dataStoreManager.saveFCMToken(token)
                        dataStoreManager.isFCMRegistrationCompleted = true
                        dataStoreManager.saveRegisteredDevice(it.deviceId)
                    }
            } else {
                dataStoreManager.saveFCMToken(token)
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "onMessageReceived: ${message.data}")
        try {
            val notificationData =
                Graph.moshi.adapter(NotificationData::class.java).fromJson(message.data["data"]!!)!!
            val notificationsManager = Graph.notificationsManager
            notificationsManager.handleNotification(notificationData)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to handle notification", e)
        }

    }
}