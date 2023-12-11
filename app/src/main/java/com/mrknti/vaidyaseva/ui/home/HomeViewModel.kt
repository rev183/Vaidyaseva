package com.mrknti.vaidyaseva.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrknti.vaidyaseva.Graph
import com.mrknti.vaidyaseva.data.OccupancyStatus
import com.mrknti.vaidyaseva.data.building.BuildingData
import com.mrknti.vaidyaseva.data.building.BuildingType
import com.mrknti.vaidyaseva.data.eventBus.EventBus
import com.mrknti.vaidyaseva.data.eventBus.RoomBookedEvent
import com.mrknti.vaidyaseva.data.eventBus.RoomCheckedOutEvent
import com.mrknti.vaidyaseva.data.eventBus.RoomOccupancyChangedEvent
import com.mrknti.vaidyaseva.data.network.handleError
import com.mrknti.vaidyaseva.data.user.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val _state = MutableStateFlow(HomeViewState())
    val state = _state.asStateFlow()
    private val buildingRepository = Graph.buildingRepository
    private val dataStore = Graph.dataStoreManager

    init {
        viewModelScope.launch {
            dataStore.getUser().collect { user ->
                _state.value = _state.value.copy(selfUser = user, isLoading = true)
            }
        }

        viewModelScope.launch {
            EventBus.subscribe<RoomBookedEvent> {
                if (it.occupancy.status == OccupancyStatus.CHECK_IN.value) {
                    handleCheckInOut(it.occupancy.roomId, it.buildingId, true)
                }
            }
        }

        viewModelScope.launch {
            EventBus.subscribe<RoomCheckedOutEvent> {
                if (it.status == OccupancyStatus.CHECK_OUT.value) {
                    handleCheckInOut(it.roomId, it.buildingId, false)
                }
            }
        }

        viewModelScope.launch {
            EventBus.subscribe<RoomOccupancyChangedEvent> {
                val occupancy = it.occupancy
                when (occupancy.occupancyStatus) {
                    OccupancyStatus.CHECK_OUT.value -> {
                        handleCheckInOut(occupancy.roomId, occupancy.buildingId, false)
                    }
                    OccupancyStatus.CHECK_IN.value -> {
                        handleCheckInOut(occupancy.roomId, occupancy.buildingId, true)
                    }
                    else -> {

                    }
                }
            }
        }
    }

    private fun handleCheckInOut(roomId: Int, buildingId: Int, isCheckIn: Boolean) {
        val index = _state.value.buildings.indexOfFirst { it.id == buildingId }
        if (index != -1) {
            val building = _state.value.buildings[index]
            val roomIndex = building.rooms?.indexOfFirst { it.id == roomId }
            if (roomIndex != -1) {
                val buildings = _state.value.buildings.toMutableList()
                if (isCheckIn) {
                    buildings[index] = building.copy(
                        freeRooms = building.freeRooms?.minus(1),
                        numOccupiedRooms = building.numOccupiedRooms?.plus(1)
                    )
                } else {
                    buildings[index] = building.copy(
                        freeRooms = building.freeRooms?.plus(1),
                        numOccupiedRooms = building.numOccupiedRooms?.minus(1)
                    )
                }
                _state.value = _state.value.copy(buildings = buildings)
            }
        }
    }

    fun getBuildingsData() {
        _state.value = _state.value.copy(isLoading = true)
        viewModelScope.launch {
            buildingRepository.getBuildings()
                .handleError {
                    _state.value = _state.value.copy(isLoading = false, errorMessage = it.message)
                }
                .collect { buildings ->
                    dataStore.saveBuildingData(buildings)
                    _state.value = _state.value.copy(
                        buildings = buildings.filter { it.type == BuildingType.APARTMENT },
                        isLoading = false
                    )
                }
        }
    }
}

data class HomeViewState(
    val buildings: List<BuildingData> = emptyList(),
    val selfUser: User? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)