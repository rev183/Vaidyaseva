package com.mrknti.vaidyaseva.ui.chats

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrknti.vaidyaseva.Graph
import com.mrknti.vaidyaseva.data.chat.ChatMessage
import com.mrknti.vaidyaseva.data.eventBus.EventBus
import com.mrknti.vaidyaseva.data.eventBus.NewChatEvent
import com.mrknti.vaidyaseva.data.network.handleError
import com.mrknti.vaidyaseva.ui.NavArgKeys
import com.mrknti.vaidyaseva.ui.services.ListState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatDetailViewModel(saveState: SavedStateHandle) : ViewModel() {
    private val chatRepository = Graph.chatRepository
    private var threadId: Int? = saveState[NavArgKeys.CHAT_THREAD_ID]
    private var page by mutableIntStateOf(1)
    var canPaginate by mutableStateOf(true)
    private var listState by mutableStateOf(ListState.IDLE)
    private val messages = mutableListOf<ChatMessage>()
    // state and actions
    private val _state = MutableStateFlow(ChatDetailUIState())
    val state = _state.asStateFlow()
    private val _actions = MutableStateFlow<ChatDetailActions?>(null)
    val actions = _actions.asStateFlow()

    init {
        viewModelScope.launch {
            EventBus.subscribe<NewChatEvent> {
                if (it.chatMessage.threadId == threadId) {
                    onNewChatAdded(it.chatMessage)
                }
            }
        }
    }

    fun getChatDetail() {
        if (page == 1 || (page != 1 && canPaginate) && listState == ListState.IDLE) {
            listState = if (page == 1) ListState.LOADING else ListState.PAGINATING
            _state.value = _state.value.copy(listState = listState)
            viewModelScope.launch {
                chatRepository.getChatDetail(threadId!!, messages.lastOrNull()?.id)
                    .handleError {
                        _state.value =
                            _state.value.copy(listState = ListState.ERROR, error = it.message ?: "")
                    }
                    .collect {
                        canPaginate = it.messages.size >= 20
                        if (page == 1) {
                            messages.clear()
                        }
                        messages.addAll(it.messages)
                        listState = if (canPaginate) {
                            page++
                            ListState.IDLE
                        } else {
                            ListState.PAGINATION_EXHAUST
                        }
                        _state.value =
                            _state.value.copy(listState = listState, messages = messages)
                    }
            }
        }
    }

    fun sendMessage(message: String) {
        viewModelScope.launch {
            chatRepository.addChatMessage(threadId!!, message)
                .handleError {
                    _state.value =
                        _state.value.copy(listState = ListState.ERROR, error = it.message ?: "")
                }
                .collect {
                    onNewChatAdded(it)
                }
        }
    }

    private fun onNewChatAdded(chat: ChatMessage) {
        messages.add(0, chat)
        _state.value = _state.value.copy(messages = messages.toList())
        _actions.value = ChatDetailActions.ScrollToTop(chat.id)
    }

    fun setThreadId(threadId: Int?) {
        this.threadId = threadId
    }

    override fun onCleared() {
        page = 1
        listState = ListState.IDLE
        canPaginate = false
        super.onCleared()
    }
}

data class ChatDetailUIState(
    val listState: ListState = ListState.IDLE,
    val messages: List<ChatMessage> = emptyList(),
    val error: String = "",
)

sealed class ChatDetailActions {
    data class ScrollToTop(val id: Int) : ChatDetailActions()
}