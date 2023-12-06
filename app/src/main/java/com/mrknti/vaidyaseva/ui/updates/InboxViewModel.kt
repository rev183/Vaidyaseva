package com.mrknti.vaidyaseva.ui.updates

import androidx.annotation.DrawableRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrknti.vaidyaseva.Graph
import com.mrknti.vaidyaseva.R
import com.mrknti.vaidyaseva.data.ServiceType
import com.mrknti.vaidyaseva.data.network.handleError
import com.mrknti.vaidyaseva.data.user.InboxItem
import com.mrknti.vaidyaseva.data.userService.Service
import com.mrknti.vaidyaseva.notifications.NotificationType
import com.mrknti.vaidyaseva.ui.services.ListState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class InboxViewModel: ViewModel() {
    private val _state = MutableStateFlow(InboxUIState())
    val state = _state.asStateFlow()
    private val userRepository = Graph.userRepository
    private var page by mutableIntStateOf(1)
    var canPaginate by mutableStateOf(true)
    private var listState by mutableStateOf(ListState.IDLE)
    private val inboxList = mutableListOf<InboxItem>()

    init {
        loadInbox()
    }

    fun loadInbox(reload: Boolean = false) {
        if (reload) {
            page = 1
            canPaginate = true
        }
        if (page == 1 || (page != 1 && canPaginate) && listState == ListState.IDLE) {
            listState = if (page == 1) ListState.LOADING else ListState.PAGINATING
            _state.value = _state.value.copy(listState = listState)
            viewModelScope.launch {
                userRepository.getInbox()
                    .handleError {
                        _state.value =
                            _state.value.copy(listState = ListState.ERROR, error = it.message ?: "")
                    }
                    .collect { inboxItems ->
                        canPaginate = inboxItems.size >= 20
                        if (page == 1) {
                            inboxList.clear()
                        }
                        val updatedItems = inboxItems.map { inboxItem ->
                            inboxItem.copy(
                                imageRes = getInboxImage(inboxItem),
                            )
                        }
                        inboxList.addAll(updatedItems)
                        listState = if (canPaginate) {
                            page++
                            ListState.IDLE
                        } else {
                            ListState.PAGINATION_EXHAUST
                        }
                        _state.value =
                            _state.value.copy(listState = listState, inboxItems = updatedItems)
                    }
            }
        }
    }

    private val serviceTypes = listOf(
        NotificationType.REQUEST_RAISED,
        NotificationType.REQUEST_ACKNOWLEDGED,
        NotificationType.REQUEST_COMPLETE
    )

    @DrawableRes
    private fun getInboxImage(inboxItem: InboxItem): Int {
        if (serviceTypes.contains(inboxItem.messageType)) {
            val service = Graph.moshi.adapter(Service::class.java).fromJson(inboxItem.payload)
            if (service != null) {
                val serviceType = ServiceType.getByValue(service.type)
                return serviceType.iconRes
            }
        }
        return R.drawable.inbox_24
    }
}

data class InboxUIState(
    val listState: ListState = ListState.IDLE,
    val inboxItems: List<InboxItem> = emptyList(),
    val error: String = ""
)