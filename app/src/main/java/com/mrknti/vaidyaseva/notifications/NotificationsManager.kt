package com.mrknti.vaidyaseva.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.mrknti.vaidyaseva.Graph
import com.mrknti.vaidyaseva.R
import com.mrknti.vaidyaseva.data.chat.ChatMessage
import com.mrknti.vaidyaseva.data.eventBus.EventBus
import com.mrknti.vaidyaseva.data.eventBus.NewChatEvent
import com.mrknti.vaidyaseva.data.eventBus.ServiceAcknowledgeEvent
import com.mrknti.vaidyaseva.data.eventBus.ServiceCompletedEvent
import com.mrknti.vaidyaseva.data.eventBus.ServiceRaisedEvent
import com.mrknti.vaidyaseva.data.userService.Service
import com.mrknti.vaidyaseva.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object NotificationType {
    const val REQUEST_RAISED = "REQUEST_RAISED"
    const val REQUEST_ACKNOWLEDGED = "REQUEST_ACK"
    const val REQUEST_COMPLETE = "REQUEST_COMPLETE"
    const val CHAT_ADDED = "CHAT_ADDED"
}

class NotificationsManager(private val context: Context) {

    val moshi = Graph.moshi
    private val defaultChannel = "Default"

    fun handleNotification(notificationData: NotificationData) {
        when (notificationData.messageType) {
            NotificationType.CHAT_ADDED -> {
                val chat = moshi.adapter(ChatMessage::class.java).fromJson(notificationData.payload)!!
                CoroutineScope(Dispatchers.IO).launch {
                    EventBus.publish(NewChatEvent(chat))
                }
            }
            NotificationType.REQUEST_RAISED -> {
                val service = moshi.adapter(Service::class.java).fromJson(notificationData.payload)!!
                CoroutineScope(Dispatchers.IO).launch {
                    EventBus.publish(ServiceRaisedEvent(service))
                }
            }
            NotificationType.REQUEST_ACKNOWLEDGED -> {
                val service = moshi.adapter(Service::class.java).fromJson(notificationData.payload)!!
                CoroutineScope(Dispatchers.IO).launch {
                    EventBus.publish(ServiceAcknowledgeEvent(service))
                }
            }
            NotificationType.REQUEST_COMPLETE -> {
                val service = moshi.adapter(Service::class.java).fromJson(notificationData.payload)!!
                CoroutineScope(Dispatchers.IO).launch {
                    EventBus.publish(ServiceCompletedEvent(service))
                }
            }
        }
        showNotification(VsNotificationImpl(notificationData))
    }

    private fun showNotification(notification: VsNotification) {
        val intent = Intent(context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )
        val channelId = "Default"
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setColor(context.getColor(R.color.teal_700))
            .setContentTitle(notification.title)
            .setContentText(notification.body)
            .setShowWhen(true)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val manager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(0, builder.build())
    }

    fun syncChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && getNotificationChannels().isEmpty()) {
            val channel = NotificationChannel(
                defaultChannel,
                "Default channel",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun getNotificationChannels(): List<NotificationChannel> {
        var existingChannels = mutableListOf<NotificationChannel>()
        NotificationManagerCompat.from(context.applicationContext).run {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                try {
                    existingChannels = notificationChannels
                } catch (_ : Exception) {

                }
            }
        }
        return existingChannels
    }

}