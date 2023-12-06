package com.mrknti.vaidyaseva.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrknti.vaidyaseva.Graph
import com.mrknti.vaidyaseva.data.building.BuildingData
import com.mrknti.vaidyaseva.data.building.BuildingType
import com.mrknti.vaidyaseva.data.network.handleError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val _state = MutableStateFlow(HomeViewState())
    val state = _state.asStateFlow()
    private val buildingRepository = Graph.buildingRepository
    private val dataStore = Graph.dataStoreManager

    init {
        _state.value = _state.value.copy(isLoading = true)
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
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)