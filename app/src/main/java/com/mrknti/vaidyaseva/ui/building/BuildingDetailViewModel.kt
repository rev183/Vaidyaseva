package com.mrknti.vaidyaseva.ui.building

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrknti.vaidyaseva.Graph
import com.mrknti.vaidyaseva.data.OccupancyStatus
import com.mrknti.vaidyaseva.data.UserRole
import com.mrknti.vaidyaseva.data.building.BuildingData
import com.mrknti.vaidyaseva.data.building.HostelRoom
import com.mrknti.vaidyaseva.data.eventBus.EventBus
import com.mrknti.vaidyaseva.data.eventBus.RoomBookedEvent
import com.mrknti.vaidyaseva.data.eventBus.RoomCheckedOutEvent
import com.mrknti.vaidyaseva.data.eventBus.RoomOccupancyChangedEvent
import com.mrknti.vaidyaseva.data.network.handleError
import com.mrknti.vaidyaseva.data.user.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class BuildingDetailViewModel(saveState: SavedStateHandle) : ViewModel() {
    val buildingId = saveState.get<Int>("building_id")!!
    private val _state = MutableStateFlow(BuildingDetailViewState())
    val state = _state.asStateFlow()
    private val buildingRepository = Graph.buildingRepository
    private val role = runBlocking { Graph.dataStoreManager.getUser().first()!! }.roles

    init {
        getBuildingData()

        viewModelScope.launch {
            EventBus.subscribe<RoomBookedEvent> {
                getBuildingData()
            }
        }

        viewModelScope.launch {
            EventBus.subscribe<RoomCheckedOutEvent> {
                if (it.buildingId == buildingId) {
                    handleCheckout(it.occupancyId, it.roomId)
                }
            }
        }

        viewModelScope.launch {
            Graph.dataStoreManager.getUser().collect {
                _state.value = _state.value.copy(selfUser = it)
            }
        }

        viewModelScope.launch {
            EventBus.subscribe<RoomOccupancyChangedEvent> {
                val occupancy = it.occupancy
                if (occupancy.buildingId == buildingId) {
                    when (occupancy.occupancyStatus) {
                        OccupancyStatus.CHECK_OUT.value -> {
                            handleCheckout(occupancy.id, occupancy.roomId)
                        }
                        OccupancyStatus.CANCELLED.value -> {
                            handleCheckout(occupancy.id, occupancy.roomId)
                        }
                        else -> {
                            getBuildingData()
                        }
                    }
                }
            }
        }
    }

    private fun getBuildingData() {
        _state.value = _state.value.copy(isLoading = true)
        viewModelScope.launch {
            buildingRepository.getBuildingDetail(buildingId)
                .handleError {
                    _state.value = _state.value.copy(isLoading = false, errorMessage = it.message)
                }
                .collect {
                    _state.value = _state.value.copy(
                        buildingData = it,
                        isLoading = false,
                        rooms = it.rooms ?: emptyList()
                    )
                }
        }
    }

    private fun handleCheckout(occupancyId: Int, roomId: Int) {
        val rooms = state.value.rooms.toMutableList()
        val roomIndex = rooms.indexOfFirst { it.id == roomId }
        if (roomIndex != -1) {
            val room = rooms[roomIndex]
            val occupancies = room.occupancies.toMutableList()
            val occupancyIndex = occupancies.indexOfFirst { it.id == occupancyId }
            if (occupancyIndex != -1) {
                occupancies.removeAt(occupancyIndex)
                rooms[roomIndex] = room.copy(occupancies = occupancies, isOccupied = false)
                _state.value = _state.value.copy(rooms = rooms)
                val buildingData = state.value.buildingData!!
                _state.value = _state.value.copy(
                    buildingData = buildingData.copy(
                        numOccupiedRooms = (buildingData.numOccupiedRooms ?: 0) - 1,
                        freeRooms = (buildingData.freeRooms ?: 0) + 1
                    ),
                )
            }
        }
    }

    fun isAdmin(): Boolean {
        return role.contains(UserRole.ADMIN.value) || role.contains(UserRole.MANAGER.value)
    }

}

data class BuildingDetailViewState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val buildingData: BuildingData? = null,
    val selfUser: User? = null,
    val rooms: List<HostelRoom> = emptyList()
)
