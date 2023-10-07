package com.mrknti.vaidyaseva.data.userService

import android.os.Parcel
import android.os.Parcelable
import com.mrknti.vaidyaseva.data.user.User
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize
import java.util.Date

@JsonClass(generateAdapter = true)
data class Service(
    val id: Int,
    @Json(name = "threadId")
    val threadId: Int,
    @Json(name = "assignedUser")
    val assignee: User?,
    @Json(name = "requestedUser")
    val requester: User,
    @Json(name = "requestType")
    val type: String,
    @Json(name = "createdTime")
    val createdAt: Date,
    @Json(name = "hasAcknowledged")
    val isAcknowledged: Boolean,
    @Json(name = "completionTime")
    val completedAt: Date?,
    @Json(name = "serviceStatus")
    val status: String,
)

