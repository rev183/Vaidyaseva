package com.mrknti.vaidyaseva.data.userService

import kotlinx.coroutines.flow.Flow
import java.util.Date

interface ServicesRepository {
    suspend fun getOpenServices(lastServiceId: Int?): Flow<List<Service>>
    suspend fun getClosedServices(lastServiceId: Int?): Flow<List<Service>>
    suspend fun bookServices(
        serviceType: String,
        serviceTime: Date,
        comment: String?,
        requesterId: Int?,
        source: Int?,
        destination: Int?,
    ): Flow<Service>
    suspend fun acknowledgeService(serviceId: Int): Flow<Unit>
    suspend fun completeService(serviceId: Int): Flow<Unit>
}