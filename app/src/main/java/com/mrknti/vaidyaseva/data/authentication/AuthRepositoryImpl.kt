package com.mrknti.vaidyaseva.data.authentication

import com.mrknti.vaidyaseva.data.ApiService
import com.mrknti.vaidyaseva.data.extensions.createMultiPartData
import com.mrknti.vaidyaseva.filehandling.MediaData
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
    ): Flow<AuthData> =
        apiService.registerUser(firstName, lastName, username, password, role)

    override suspend fun registerFCMToken(token: String): Flow<Unit> =
        apiService.registerFCMToken(token)

    override suspend fun uploadDocument(
        clientId: Int,
        documentType: Int,
        document: MediaData
    ): Flow<Unit> {
        return apiService.uploadDocument(clientId, documentType, document.createMultiPartData("data"))
    }

}