package com.mrknti.vaidyaseva.data.dataModel

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AuthData(
    @Json(name = "token")
    val authToken: String,
    val roles: List<String>
)