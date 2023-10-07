package com.mrknti.vaidyaseva.data.chat

import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.Date

@JsonClass(generateAdapter = true)
data class ChatMessage(
    val id: Int,
    val threadId: Int,
    @Json(name = "userId")
    val senderId: Int,
    @Json(name = "displayName")
    val senderName: String,
    @Json(name = "chat")
    val body: String,
    @Json(name = "createdTime")
    val createdAt: Date,
)