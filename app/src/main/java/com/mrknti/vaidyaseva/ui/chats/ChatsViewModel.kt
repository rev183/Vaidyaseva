package com.mrknti.vaidyaseva.ui.chats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrknti.vaidyaseva.Graph
import com.mrknti.vaidyaseva.data.chat.ChatThread
import com.mrknti.vaidyaseva.data.network.handleError
import com.mrknti.vaidyaseva.data.userService.Service
import com.mrknti.vaidyaseva.ui.services.ListState
import com.mrknti.vaidyaseva.ui.services.ServicesUIState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatsViewModel : ViewModel() {
    private val chatRepository = Graph.chatRepository
    private val _state = MutableStateFlow(ChatsUIState())
    val state = _state.asStateFlow()

    init {
        getChats()
    }

    private fun getChats() {
        _state.value = _state.value.copy(isLoading = true)
        viewModelScope.launch {
            chatRepository.getChats()
                .handleError { e ->
                    _state.value = _state.value.copy(isLoading = false, error = e.message ?: "")
                }
                .collect {
                    _state.value = _state.value.copy(chats = it, isLoading = false)
                }
        }
    }
}

data class ChatsUIState(
    val isLoading: Boolean = true,
    val chats: List<ChatThread> = emptyList(),
    val error: String = ""
)