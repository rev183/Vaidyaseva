package com.mrknti.vaidyaseva.ui.services

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrknti.vaidyaseva.Graph
import com.mrknti.vaidyaseva.data.ServiceStatus
import com.mrknti.vaidyaseva.data.eventBus.EventBus
import com.mrknti.vaidyaseva.data.eventBus.ServiceCompletedEvent
import com.mrknti.vaidyaseva.data.network.handleError
import com.mrknti.vaidyaseva.data.userService.Service
import com.mrknti.vaidyaseva.ui.NavArgKeys
import com.squareup.moshi.JsonAdapter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.Date

class ServiceDetailViewModel(saveState: SavedStateHandle) : ViewModel() {
    private val jsonAdapter: JsonAdapter<Service> = Graph.moshi.adapter(Service::class.java)
    private val serviceJson: String = requireNotNull(saveState[NavArgKeys.SERVICE_DATA]) {
        "Service data not found in saved state"
    }
    private val servicesRepository = Graph.servicesRepository
    private val service = jsonAdapter.fromJson(serviceJson)!!
    private val _state = MutableStateFlow(ServiceDetailUIState(service = service))
    val state = _state.asStateFlow()
    val userId = runBlocking { Graph.dataStoreManager.userId.first() }

    fun onAcknowledgeCompleteClick() {
        _state.value = _state.value.copy(isLoading = true)
        viewModelScope.launch {
            if (!service.isAcknowledged) {
                servicesRepository.acknowledgeService(service.id)
                    .handleError { e ->
                        _state.value = _state.value.copy(isLoading = false, error = e.message ?: "")
                    }
                    .collect {
                        val updatedService = service.copy(isAcknowledged = true)
                        _state.value = _state.value.copy(isLoading = false, service = updatedService)
                    }
            } else {
                servicesRepository.completeService(service.id)
                    .handleError { e ->
                        _state.value = _state.value.copy(isLoading = false, error = e.message ?: "")
                    }
                    .collect {
                        val updatedService =
                            service.copy(status = ServiceStatus.COMPLETED, completedAt = Date())
                        EventBus.publish(ServiceCompletedEvent(updatedService))
                        _state.value = _state.value.copy(isLoading = false, service = updatedService)
                    }
            }
        }
    }
}

data class ServiceDetailUIState(
    val isLoading: Boolean = false,
    val service: Service,
    val error: String = ""
)