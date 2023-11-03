package com.mrknti.vaidyaseva.data.building

import kotlinx.coroutines.flow.Flow
import java.util.Date

interface BuildingRepository {
    suspend fun getBuildings(): Flow<List<BuildingData>>
    suspend fun getBuildingDetail(buildingId: Int): Flow<BuildingData>
    suspend fun bookRoom(
        roomId: Int,
        occupantId: Int,
        checkIn: Date,
        checkOut: Date
    ): Flow<RoomOccupancy>
}