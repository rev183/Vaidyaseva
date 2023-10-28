package com.mrknti.vaidyaseva.notifications

abstract class VsNotification(data: NotificationData) {
    val title: String = data.title
    val body: String = data.body ?: ""
}

class VsNotificationImpl(data: NotificationData) : VsNotification(data)