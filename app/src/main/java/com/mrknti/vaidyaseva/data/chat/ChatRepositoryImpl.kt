package com.mrknti.vaidyaseva.data.chat

import com.mrknti.vaidyaseva.data.ApiService
import kotlinx.coroutines.flow.Flow

class ChatRepositoryImpl(private val apiService: ApiService) : ChatRepository {

    override suspend fun getChats(): Flow<List<ChatThread>> = apiService.getChats()
    override suspend fun getChatDetail(threadId: Int, pagenum: Int): Flow<ChatThread> =
        apiService.getChatDetail(threadId, pagenum)
    override suspend fun addChatMessage(threadId: Int, message: String) : Flow<ChatMessage> =
        apiService.addChatMessage(threadId, message)
}