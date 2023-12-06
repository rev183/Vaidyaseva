package com.mrknti.vaidyaseva.data.userService

import com.mrknti.vaidyaseva.data.ApiService
import com.mrknti.vaidyaseva.util.convertToISO8601
import kotlinx.coroutines.flow.Flow
import java.util.Date

class ServiceRepositoryImpl(private val apiService: ApiService) : ServicesRepository {
    override suspend fun getOpenServices(lastServiceId: Int?): Flow<List<Service>> =
        apiService.getOpenServices(lastServiceId)

    override suspend fun getClosedServices(lastServiceId: Int?): Flow<List<Service>> =
        apiService.getClosedServices(lastServiceId)

    override suspend fun bookServices(
        serviceType: String,
        serviceTime: Date,
        comment: String?,
        requesterId: Int?,
        source: Int?,
        destination: Int?,
    ): Flow<Service> =
        apiService.bookService(
            serviceType,
            convertToISO8601(serviceTime),
            comment,
            requesterId,
            source,
            destination
        )

    override suspend fun acknowledgeService(serviceId: Int): Flow<Unit> =
        apiService.acknowledgeService(serviceId)

    override suspend fun completeService(serviceId: Int): Flow<Unit> =
        apiService.completeService(serviceId)
}