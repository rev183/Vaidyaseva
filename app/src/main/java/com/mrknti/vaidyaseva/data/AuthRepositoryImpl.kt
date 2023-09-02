package com.mrknti.vaidyaseva.data

import com.mrknti.vaidyaseva.data.dataModel.AuthData
import kotlinx.coroutines.flow.Flow

class AuthRepositoryImpl(private val apiService: ApiService) : AuthRepository {

    override suspend fun login(username: String, password: String): Flow<AuthData> =
        apiService.login(username, password)


    override suspend fun signup(username: String, password: String): Flow<AuthData> =
        apiService.signup(username, password, UserRoles.CLIENT)

}