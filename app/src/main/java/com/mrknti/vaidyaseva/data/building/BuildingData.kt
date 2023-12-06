package com.mrknti.vaidyaseva.data.building

import com.mrknti.vaidyaseva.data.getBuildingGalleryUrl
import com.mrknti.vaidyaseva.data.user.User
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BuildingData(
    val id: Int,
    val name: String?,
    val manager: User?,
    @Json(name = "buildingType")
    val type: Int,
    @Json(name = "checkInCount")
    val numOccupiedRooms: Int?,
    @Json(name = "freeCount")
    val freeRooms: Int?,
    @Json(name = "totalCount")
    val totalRooms: Int?,
    val rooms: List<HostelRoom>?,
    @Json(name = "galleryDtos")
    val gallery: List<BuildingImage>?,
) {
    fun getGalleryUrls(): List<String> {
        return gallery?.map { getBuildingGalleryUrl(it.id) } ?: emptyList()
    }
}

data class BuildingImage(
    val id: Int,
    val buildingId: Int,
)

object BuildingType {
    const val APARTMENT = 0
    const val HOSPITAL = 1
    const val AIRPORT = 2
}