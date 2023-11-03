package com.mrknti.vaidyaseva.data.building

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BuildingData(
    val id: Int,
    val name: String?,
    val managerName: String?,
    @Json(name = "checkInCount")
    val numOccupiedRooms: Int?,
    @Json(name = "freeCount")
    val freeRooms: Int?,
    @Json(name = "totalCount")
    val totalRooms: Int?,
    val rooms: List<HostelRoom>?
)