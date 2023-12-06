package com.mrknti.vaidyaseva.ui.services

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrknti.vaidyaseva.Graph
import com.mrknti.vaidyaseva.data.APOLLO_HOSPITAL_ID
import com.mrknti.vaidyaseva.data.ServiceType
import com.mrknti.vaidyaseva.data.building.BuildingData
import com.mrknti.vaidyaseva.data.eventBus.EventBus
import com.mrknti.vaidyaseva.data.eventBus.ServiceRaisedEvent
import com.mrknti.vaidyaseva.data.network.handleError
import com.mrknti.vaidyaseva.data.user.User
import com.mrknti.vaidyaseva.data.user.UserInfo
import com.mrknti.vaidyaseva.ui.NavArgKeys
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import java.util.Date

class BookServiceViewModel(saveState: SavedStateHandle) : ViewModel() {
    private val servicesRepository = Graph.servicesRepository
    private  val userRepository = Graph.userRepository
    private val dataStoreManager = Graph.dataStoreManager
    private val _state = MutableStateFlow(BookServiceUIState(serviceType = ServiceType.getByValue(requireNotNull(saveState[NavArgKeys.SERVICE_TYPE]) {
        "No service type provided in save state"
    })))
    val state = _state.asStateFlow()
    private val _action = MutableStateFlow<ServiceBookingAction?>(null)
    val action = _action.asStateFlow()

    init {
        viewModelScope.launch {
            dataStoreManager.getSelfBuilding()
                .collect {
                    _state.value = _state.value.copy(requesterBuilding = it)
                }
        }

        viewModelScope.launch {
            dataStoreManager.getUserInfo()
                .collect {
                    _state.value = _state.value.copy(
                        requestUserInfo = it,
                        requestUser = it?.user,
                        selfUser = it?.user
                    )
                }
        }
    }

    fun bookService() {
        val message = _state.value.message
        _state.value = _state.value.copy(isLoading = true)
        viewModelScope.launch {
            servicesRepository.bookServices(
                serviceType = _state.value.serviceType.value,
                serviceTime =  _state.value.serviceDate,
                comment = message,
                requesterId =  _state.value.requestUser?.id,
                source = getSourceDestination().first,
                destination = getSourceDestination().second
            ).handleError { e ->
                _state.value = _state.value.copy(error = e.message ?: "", isLoading = false)
            }.collect {
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

    fun setDestinationType(destinationType: DestinationType) {
        _state.value = _state.value.copy(destinationType = destinationType)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun setRequestUser(user: User?) {
        val currentUser = _state.value.requestUser
        if (currentUser != null && currentUser.id == user?.id) return
        if (user == null) {
            _state.value = _state.value.copy(requestUser = null)
        } else {
            viewModelScope.launch {
                userRepository.getUserInfo(user.id)
                    .flatMapLatest {
                        _state.value = _state.value.copy(requestUser = user, requestUserInfo = it)
                        dataStoreManager.getBuildingById(it.buildingId ?: -1)
                    }
                    .handleError { e ->
                        _state.value = _state.value.copy(
                            error = e.message ?: "",
                            requestUser = null,
                        )
                     }
                    .collect {
                        _state.value = _state.value.copy(requesterBuilding = it)
                    }
            }
        }
    }

    private fun getSourceDestination(): Pair<Int?, Int?> {
        val destinationType = _state.value.destinationType
        val buildingInfo = _state.value.requesterBuilding
        return when (destinationType) {
            DestinationType.ROOM -> {
                val room = buildingInfo?.id
                val hospital = APOLLO_HOSPITAL_ID
                hospital to room
            }
            DestinationType.HOSPITAL -> {
                val hospital = APOLLO_HOSPITAL_ID
                val room = buildingInfo?.id
                room to hospital
            }
            else -> null to null
        }
    }

    fun serviceMissingRoom(): Boolean {
        return _state.value.serviceType.needsRoom && _state.value.requesterBuilding == null
    }
}

data class BookServiceUIState(
    val serviceType: ServiceType,
    val error: String = "",
    val message: String = "",
    val serviceDate: Date = Date(),
    val isLoading: Boolean = false,
    val destinationType: DestinationType? = null,
    val requestUser: User? = null,
    val selfUser: User? = null,
    val requestUserInfo: UserInfo? = null,
    val requesterBuilding: BuildingData? = null,
)

enum class DestinationType {
    ROOM,
    HOSPITAL
}


sealed class ServiceBookingAction {
    data object BookingComplete : ServiceBookingAction()
}