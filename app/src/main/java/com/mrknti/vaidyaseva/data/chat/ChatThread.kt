package com.mrknti.vaidyaseva.data.chat

import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.Date

@JsonClass(generateAdapter = true)
data class ChatThread(
    val id: String,
    @Json(name = "createdTime")
    val createdAt: Date,
    @Json(name = "chatDtos")
    val messages: List<ChatMessage>
)