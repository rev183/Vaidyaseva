package com.mrknti.vaidyaseva.data.authentication

import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AuthData(
    @Json(name = "id")
    val userId: Int,
    @Json(name = "token")
    val authToken: String,
    val roles: List<String>
)