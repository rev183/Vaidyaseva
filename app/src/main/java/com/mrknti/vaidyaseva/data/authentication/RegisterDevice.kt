package com.mrknti.vaidyaseva.data.authentication

import com.squareup.moshi.Json

data class RegisterDevice(
    val deviceId: Int,
    val deviceName: String?,
    @Json(name = "registrationToken")
    val fcmToken: String,
)