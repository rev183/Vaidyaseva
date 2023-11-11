package com.mrknti.vaidyaseva.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrknti.vaidyaseva.Graph
import com.mrknti.vaidyaseva.data.building.BuildingData
import com.mrknti.vaidyaseva.data.network.handleError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class HomeViewModel : ViewModel() {
    private val _state = MutableStateFlow(HomeViewState())
    val state = _state.asStateFlow()
    private val buildingRepository = Graph.buildingRepository
    val role = runBlocking { Graph.dataStoreManager.getUser().first() }?.roles

    fun getBuildingsData() {
        _state.value = _state.value.copy(isLoading = true)
        viewModelScope.launch {
            buildingRepository.getBuildings()
                .handleError {
                    _state.value = _state.value.copy(isLoading = false, errorMessage = it.message)
                }
                .collect {
                    _state.value = _state.value.copy(buildings = it, isLoading = false)
                }
        }
    }
}

data class HomeViewState(
    val buildings: List<BuildingData> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)