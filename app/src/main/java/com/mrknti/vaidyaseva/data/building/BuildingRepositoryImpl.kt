package com.mrknti.vaidyaseva.data.building

import com.mrknti.vaidyaseva.data.ApiService
import com.mrknti.vaidyaseva.util.convertToISO8601
import kotlinx.coroutines.flow.Flow
import java.util.Date

class BuildingRepositoryImpl(private val apiService: ApiService) : BuildingRepository {
    override suspend fun getBuildings(): Flow<List<BuildingData>> =
        apiService.getBuildings()

    override suspend fun getBuildingDetail(buildingId: Int): Flow<BuildingData> =
        apiService.getBuildingDetail(buildingId)

    override suspend fun bookRoom(
        roomId: Int,
        occupantId: Int,
        checkIn: Date,
        checkOut: Date
    ): Flow<RoomOccupancy> = apiService.bookRoom(
        roomId,
        occupantId,
        convertToISO8601(checkIn),
        convertToISO8601(checkOut)
    )

    override suspend fun checkOutOccupancy(occupancyId: Int): Flow<RoomOccupancy> =
        apiService.checkOutOccupancy(occupancyId)

}