package com.mrknti.vaidyaseva.data.user

import com.mrknti.vaidyaseva.data.ApiService
import kotlinx.coroutines.flow.Flow

class UserRepositoryImpl(private val apiService: ApiService) : UserRepository {
    override suspend fun searchUser(query: String): Flow<List<User>> = apiService.searchUser(query)
    override suspend fun getAllDocuments(userId: Int): Flow<List<UserDocument>> =
        apiService.getAllDocuments(userId)
}