package com.mrknti.vaidyaseva.data.building

import com.mrknti.vaidyaseva.data.user.User
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BuildingData(
    val id: Int,
    val name: String?,
    val manager: User?,
    @Json(name = "checkInCount")
    val numOccupiedRooms: Int?,
    @Json(name = "freeCount")
    val freeRooms: Int?,
    @Json(name = "totalCount")
    val totalRooms: Int?,
    val rooms: List<HostelRoom>?
)