package com.mrknti.vaidyaseva.data.user

import com.mrknti.vaidyaseva.data.ApiService
import com.mrknti.vaidyaseva.data.extensions.createMultiPartData
import com.mrknti.vaidyaseva.filehandling.MediaData
import com.mrknti.vaidyaseva.util.convertToISO8601
import kotlinx.coroutines.flow.Flow
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.Date

class UserRepositoryImpl(private val apiService: ApiService) : UserRepository {
    override suspend fun searchUser(query: String): Flow<List<User>> = apiService.searchUser(query)
    override suspend fun getAllDocuments(userId: Int): Flow<List<UserDocument>> =
        apiService.getAllDocuments(userId)
    override suspend fun uploadDocument(
        clientId: Int,
        documentType: Int,
        expiryTime: Date,
        documents: List<MediaData>
    ): Flow<Unit> {
        return apiService.uploadDocument(
            clientId,
            documentType,
            convertToISO8601(expiryTime).toRequestBody(),
            documents.map { it.createMultiPartData("data")  }
        )
    }
    override suspend fun getInbox(): Flow<List<InboxItem>> = apiService.getInbox()

    override suspend fun getUserInfo(userId: Int?): Flow<UserInfo> = apiService.getUserInfo(userId)

    override suspend fun deleteDocument(documentId: Int): Flow<Unit> =
        apiService.deleteDocument(documentId)
}