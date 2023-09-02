package com.mrknti.vaidyaseva.data

import com.mrknti.vaidyaseva.data.dataModel.AuthData
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(username: String, password: String): Flow<AuthData>
    suspend fun signup(username: String, password: String): Flow<AuthData>
}