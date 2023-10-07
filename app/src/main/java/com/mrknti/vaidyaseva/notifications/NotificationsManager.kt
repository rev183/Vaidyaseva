package com.mrknti.vaidyaseva.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.mrknti.vaidyaseva.Graph
import com.mrknti.vaidyaseva.R
import com.mrknti.vaidyaseva.data.chat.ChatMessage
import com.mrknti.vaidyaseva.data.eventBus.EventBus
import com.mrknti.vaidyaseva.data.eventBus.NewChatEvent
import com.mrknti.vaidyaseva.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object NotificationType {
    const val REQUEST_RAISED = "REQUEST_RAISED"
    const val CHAT_ADDED = "CHAT_ADDED"
}

class NotificationsManager(private val context: Context) {

    val moshi = Graph.moshi

    fun handleNotification(notification: Notification) {
        when (notification.messageType) {
            NotificationType.CHAT_ADDED -> {
                val chat = moshi.adapter(ChatMessage::class.java).fromJson(notification.payload)!!
                CoroutineScope(Dispatchers.IO).launch {
                    EventBus.publish(NewChatEvent(chat))
                }
            }
        }
    }

    fun showNotification(notification: Notification) {
        val intent = Intent(context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )
        val channelId = "Default"
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(notification.title)
            .setContentText(notification.body).setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val manager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Default channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            manager.createNotificationChannel(channel)
        }
        manager.notify(0, builder.build())
    }

}