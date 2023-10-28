package com.mrknti.vaidyaseva.data.authentication

import com.mrknti.vaidyaseva.filehandling.MediaData
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(username: String, password: String): Flow<AuthData>

    suspend fun registerFCMToken(token: String): Flow<Unit>

    // user register apis
    suspend fun registerUser(
        firstName: String,
        lastName: String,
        username: String,
        password: String,
        role: String
    ): Flow<AuthData>

    suspend fun uploadDocument(
        clientId: Int,
        documentType: Int,
        document: MediaData,
    ): Flow<Unit>
}