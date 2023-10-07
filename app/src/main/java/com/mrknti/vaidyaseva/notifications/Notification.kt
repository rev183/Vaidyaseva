package com.mrknti.vaidyaseva.notifications

data class Notification(
    val title: String,
    val body: String?,
    val messageType: String,
    val payload: String
)