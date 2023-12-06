package com.mrknti.vaidyaseva.data.user

import androidx.annotation.DrawableRes
import java.util.Date

data class InboxItem(
    val id: Int,
    val title: String,
    val body: String?,
    val messageType: String,
    val payload: String,
    val createdTime: Date,
    @DrawableRes
    val imageRes: Int?
)