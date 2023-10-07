package com.mrknti.vaidyaseva.data.user

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val id: Int,
    val displayName: String,
) : Parcelable