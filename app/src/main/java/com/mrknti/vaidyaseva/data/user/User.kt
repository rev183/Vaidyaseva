package com.mrknti.vaidyaseva.data.user

data class User(
    val id: Int,
    val username: String? = null,
    val displayName: String,
    val role: List<String>? = null,
)