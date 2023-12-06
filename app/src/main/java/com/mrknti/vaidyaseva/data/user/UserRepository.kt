package com.mrknti.vaidyaseva.data.user

import com.mrknti.vaidyaseva.filehandling.MediaData
import kotlinx.coroutines.flow.Flow
import java.util.Date

interface UserRepository {
    suspend fun searchUser(query: String): Flow<List<User>>
    suspend fun getAllDocuments(userId: Int): Flow<List<UserDocument>>
    suspend fun uploadDocument(
        clientId: Int,
        documentType: Int,
        expiryTime: Date,
        document: MediaData,
    ): Flow<Unit>
    suspend fun getInbox(): Flow<List<InboxItem>>

    suspend fun getUserInfo(userId: Int?): Flow<UserInfo>
}