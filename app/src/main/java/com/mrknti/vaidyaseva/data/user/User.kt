package com.mrknti.vaidyaseva.data.user

import android.os.Parcelable
import com.mrknti.vaidyaseva.data.UserRole
import com.mrknti.vaidyaseva.data.building.RoomOccupancy
import com.squareup.moshi.Json
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val id: Int,
    val username: String? = null,
    val displayName: String,
    val roles: List<String>,
) : Parcelable {
    fun canProxyBook(): Boolean {
        val ableRoles = listOf(UserRole.ADMIN.value, UserRole.MANAGER.value, UserRole.RECRUITER.value)
        roles.forEach {
            if (ableRoles.contains(it)) return true
        }
        return false
    }
}

data class UserInfo(
    @Json(name = "userDto")
    val user: User,
    @Json(name = "occupancyDto")
    val occupancy: RoomOccupancy?,
    @Json(name = "documentDtos")
    val documents: List<UserDocument>?,
    val buildingId: Int?,
    val buildingName: String?,
    val roomName: String?,
)