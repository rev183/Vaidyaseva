package com.mrknti.vaidyaseva.data.authentication

import com.mrknti.vaidyaseva.data.ApiService
import com.mrknti.vaidyaseva.data.user.User
import kotlinx.coroutines.flow.Flow

class AuthRepositoryImpl(private val apiService: ApiService) : AuthRepository {

    override suspend fun login(username: String, password: String): Flow<AuthData> =
        apiService.login(username, password)

    override suspend fun registerUser(
        firstName: String,
        lastName: String,
        username: String,
        password: String,
        role: String
    ): Flow<User> =
        apiService.registerUser(firstName, lastName, username, password, role)

    override suspend fun registerFCMToken(token: String, deviceId: Int?): Flow<RegisterDevice> =
        apiService.registerFCMToken(token, deviceId)

    override suspend fun logout(deviceId: Int?): Flow<Unit> =
        apiService.logout(deviceId)

}