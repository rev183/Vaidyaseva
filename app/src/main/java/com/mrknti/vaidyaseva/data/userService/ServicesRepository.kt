package com.mrknti.vaidyaseva.data.userService

import com.mrknti.vaidyaseva.data.ServiceBooking
import kotlinx.coroutines.flow.Flow
import java.util.Date

interface ServicesRepository {
    suspend fun getOpenServices(pagenum: Int): Flow<List<Service>>
    suspend fun getClosedServices(pagenum: Int): Flow<List<Service>>
    suspend fun bookServices(
        serviceType: String,
        serviceTime: Date,
        comment: String?
    ): Flow<ServiceBooking>
    suspend fun acknowledgeService(serviceId: Int): Flow<Unit>
    suspend fun completeService(serviceId: Int): Flow<Unit>
}