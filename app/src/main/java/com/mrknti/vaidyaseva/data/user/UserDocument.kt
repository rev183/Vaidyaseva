package com.mrknti.vaidyaseva.data.user

import java.util.Date

data class UserDocument(
    val id: Int,
    val userId: Int,
    val documentType: Int,
    val expiryTime: Date?
)