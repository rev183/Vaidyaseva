package com.mrknti.vaidyaseva.ui.services

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrknti.vaidyaseva.Graph
import com.mrknti.vaidyaseva.data.eventBus.EventBus
import com.mrknti.vaidyaseva.data.eventBus.ServiceRaisedEvent
import com.mrknti.vaidyaseva.data.network.handleError
import com.mrknti.vaidyaseva.ui.NavArgKeys
import com.mrknti.vaidyaseva.ui.home.HOME_SERVICE
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date

class BookServiceViewModel(saveState: SavedStateHandle) : ViewModel() {
    private val servicesRepository = Graph.servicesRepository
    private val _state = MutableStateFlow(BookServiceUIState(serviceType = checkNotNull(saveState[NavArgKeys.SERVICE_TYPE]) {
        "No service type provided in save state"
    }))
    val state = _state.asStateFlow()
    val serviceBooking = HOME_SERVICE[state.value.serviceType]!!
    private val _action = MutableStateFlow<ServiceBookingAction?>(null)
    val action = _action.asStateFlow()

    fun bookService() {
        val message = _state.value.message
        _state.value = _state.value.copy(isLoading = true)
        viewModelScope.launch {
            servicesRepository.bookServices(_state.value.serviceType, _state.value.serviceDate!!, message)
                .handleError { e ->
                    _state.value = _state.value.copy(error = e.message ?: "", isLoading = false)
                }
                .collect {
                    _state.value = _state.value.copy(isLoading = false)
                    _action.value = ServiceBookingAction.BookingComplete
                    EventBus.publish(ServiceRaisedEvent(it))
                }
        }
    }

    fun onDateChange(date: Date) {
        _state.value = _state.value.copy(serviceDate = date)
    }

    fun onMessageChange(message: String) {
        _state.value = _state.value.copy(message = message)
    }
}

data class BookServiceUIState(
    val serviceType: String,
    val error: String = "",
    val message: String = "",
    val serviceDate: Date? = null,
    val isLoading: Boolean = false
)


sealed class ServiceBookingAction {
    data object BookingComplete : ServiceBookingAction()
}