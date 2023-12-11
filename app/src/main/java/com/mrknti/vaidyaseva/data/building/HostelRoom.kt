package com.mrknti.vaidyaseva.data.building

import com.mrknti.vaidyaseva.data.user.User
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.Date

@JsonClass(generateAdapter = true)
data class HostelRoom(
    val id: Int,
    @Json(name = "roomName")
    val name: String,
    @Json(name = "occupancyDtos")
    val occupancies: List<RoomOccupancy>,
    @Json(name = "occupied")
    val isOccupied: Boolean,
)

@JsonClass(generateAdapter = true)
data class RoomOccupancy(
    val id: Int,
    val roomId: Int,
    @Json(name = "occupancyStatus")
    val status: Int,
    @Json(name = "userDto")
    val occupant: User?,
    val checkInTime: Date?,
    val checkoutTime: Date?
)

data class OccupancyPayload(
    val id: Int,
    val roomId: Int,
    val buildingId: Int,
    val occupancyStatus: Int,
    val checkInTime: Date,
    val checkoutTime: Date,
    val userDto: User
)