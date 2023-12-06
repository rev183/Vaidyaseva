package com.mrknti.vaidyaseva.ui.services

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrknti.vaidyaseva.Graph
import com.mrknti.vaidyaseva.data.ServiceStatus
import com.mrknti.vaidyaseva.data.ServiceType
import com.mrknti.vaidyaseva.data.eventBus.EventBus
import com.mrknti.vaidyaseva.data.eventBus.ServiceAcknowledgeEvent
import com.mrknti.vaidyaseva.data.eventBus.ServiceCompletedEvent
import com.mrknti.vaidyaseva.data.network.handleError
import com.mrknti.vaidyaseva.data.userService.Service
import com.mrknti.vaidyaseva.ui.NavArgKeys
import com.squareup.moshi.JsonAdapter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
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
    private val dataStoreManager = Graph.dataStoreManager
    private val service = jsonAdapter.fromJson(serviceJson)!!
    private val selfUser = runBlocking { dataStoreManager.getUser().first() }

    private val _state = MutableStateFlow(ServiceDetailUIState(service = service))
    val state = _state.asStateFlow()

    val canCompleteService = service.assignee?.id == selfUser?.id
    val canAcknowledgeService =
        !service.isAcknowledged && canAssignToUser(selfUser?.roles ?: emptyList())

    init {
        if (service.type == ServiceType.TRANSPORT.value && service.source != null && service.destination != null) {
            viewModelScope.launch {
                dataStoreManager.getTransportDetails(service.source, service.destination)
                    .collect { (source, destination) ->
                        _state.value = _state.value.copy(
                            source = source,
                            destination = destination
                        )
                    }
            }
        }
    }

    fun onAcknowledgeCompleteClick() {
        _state.value = _state.value.copy(isLoading = true)
        viewModelScope.launch {
            if (!_state.value.service.isAcknowledged) {
                val ackFlow = servicesRepository.acknowledgeService(service.id)
                    .handleError { e ->
                        _state.value = _state.value.copy(isLoading = false, error = e.message ?: "")
                    }
                ackFlow.combine(dataStoreManager.getUser()) { _, user ->
                    val updatedService = service.copy(isAcknowledged = true, assignee = user)
                    _state.value = _state.value.copy(isLoading = false, service = updatedService)
                    return@combine updatedService
                }.collect {
                    EventBus.publish(ServiceAcknowledgeEvent(it))
                }
            } else {
                servicesRepository.completeService(service.id)
                    .handleError { e ->
                        _state.value = _state.value.copy(isLoading = false, error = e.message ?: "")
                    }
                    .collect {
                        val updatedService =
                            service.copy(status = ServiceStatus.COMPLETED, completedAt = Date())
                        _state.value = _state.value.copy(isLoading = false, service = updatedService)
                        EventBus.publish(ServiceCompletedEvent(updatedService))
                    }
            }
        }
    }

    private fun canAssignToUser(userRoles: List<String>): Boolean {
        val serviceType = ServiceType.getByValue(service.type)
        userRoles.forEach {
            if (serviceType.canBeAssignedTo(it)) {
                return true
            }
        }
        return false
    }
}

data class ServiceDetailUIState(
    val isLoading: Boolean = false,
    val service: Service,
    val error: String = "",
    val source: String? = null,
    val destination: String? = null,
)