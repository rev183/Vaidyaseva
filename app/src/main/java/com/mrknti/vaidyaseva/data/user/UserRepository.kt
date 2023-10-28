package com.mrknti.vaidyaseva.data.user

import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun searchUser(query: String): Flow<List<User>>
}