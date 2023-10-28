package com.mrknti.vaidyaseva.data.authentication

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AuthData(
    @Json(name = "id")
    val userId: Int,
    val username: String?,
    val displayName: String,
    @Json(name = "token")
    val authToken: String,
    val roles: List<String>
)