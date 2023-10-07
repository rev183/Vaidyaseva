package com.mrknti.vaidyaseva.data.userService

import com.mrknti.vaidyaseva.data.ApiService
import com.mrknti.vaidyaseva.data.ServiceBooking
import com.mrknti.vaidyaseva.data.UserRoles
import com.mrknti.vaidyaseva.util.convertToISO8601
import kotlinx.coroutines.flow.Flow
import java.util.Date

class ServiceRepositoryImpl(private val apiService: ApiService) : ServicesRepository {
    override suspend fun getOpenServices(pagenum: Int): Flow<List<Service>> =
        apiService.getOpenServices(pagenum)

    override suspend fun getClosedServices(pagenum: Int): Flow<List<Service>> =
        apiService.getClosedServices(pagenum)

    override suspend fun bookServices(
        serviceType: String,
        serviceTime: Date,
        comment: String?
    ): Flow<ServiceBooking> =
        apiService.bookService(serviceType, convertToISO8601(serviceTime), comment)

    override suspend fun acknowledgeService(serviceId: Int): Flow<Unit> =
        apiService.acknowledgeService(serviceId)

    override suspend fun completeService(serviceId: Int): Flow<Unit> =
        apiService.completeService(serviceId)
}