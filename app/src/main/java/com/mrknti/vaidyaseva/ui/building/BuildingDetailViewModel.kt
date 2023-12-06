package com.mrknti.vaidyaseva.ui.building

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrknti.vaidyaseva.Graph
import com.mrknti.vaidyaseva.data.UserRole
import com.mrknti.vaidyaseva.data.building.BuildingData
import com.mrknti.vaidyaseva.data.building.HostelRoom
import com.mrknti.vaidyaseva.data.eventBus.EventBus
import com.mrknti.vaidyaseva.data.eventBus.RoomBookedEvent
import com.mrknti.vaidyaseva.data.eventBus.RoomCheckedOutEvent
import com.mrknti.vaidyaseva.data.network.handleError
import com.mrknti.vaidyaseva.data.user.User
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@OptIn(ExperimentalCoroutinesApi::class)
class BuildingDetailViewModel(saveState: SavedStateHandle) : ViewModel() {
    private val buildingId = saveState.get<Int>("building_id")!!
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
                handleCheckout(it.occupancyId, it.roomId)
            }
        }

        viewModelScope.launch {
            Graph.dataStoreManager.getUser().collect {
                _state.value = _state.value.copy(selfUser = it)
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
