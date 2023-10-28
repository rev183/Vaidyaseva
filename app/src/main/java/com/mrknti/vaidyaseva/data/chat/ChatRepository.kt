package com.mrknti.vaidyaseva.data.chat

import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    suspend fun getChats() : Flow<List<ChatThread>>
    suspend fun getChatDetail(threadId: Int, lastMessageId: Int?) : Flow<ChatThread>
    suspend fun addChatMessage(threadId: Int, message: String) : Flow<ChatMessage>
}