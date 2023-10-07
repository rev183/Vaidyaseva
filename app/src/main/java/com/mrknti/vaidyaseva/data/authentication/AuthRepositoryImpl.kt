package com.mrknti.vaidyaseva.data.authentication

import com.mrknti.vaidyaseva.data.ApiService
import com.mrknti.vaidyaseva.data.UserRoles
import kotlinx.coroutines.flow.Flow

class AuthRepositoryImpl(private val apiService: ApiService) : AuthRepository {

    override suspend fun login(username: String, password: String): Flow<AuthData> =
        apiService.login(username, password)


    override suspend fun signup(username: String, password: String): Flow<AuthData> =
        apiService.signup(username, password, UserRoles.CLIENT)

    override suspend fun registerFCMToken(token: String): Flow<Unit> =
        apiService.registerFCMToken(token)

}