package com.mrknti.vaidyaseva.ui.building

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrknti.vaidyaseva.Graph
import com.mrknti.vaidyaseva.data.building.BuildingData
import com.mrknti.vaidyaseva.data.network.handleError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BuildingDetailViewModel(saveState: SavedStateHandle) : ViewModel() {
    private val buildingId = saveState.get<Int>("building_id")!!
    private val _state = MutableStateFlow(BuildingDetailViewState())
    val state = _state.asStateFlow()
    private val buildingRepository = Graph.buildingRepository

    init {
        getBuildingData()
    }

    private fun getBuildingData() {
        _state.value = _state.value.copy(isLoading = true)
        viewModelScope.launch {
            buildingRepository.getBuildingDetail(buildingId)
                .handleError {
                    _state.value = _state.value.copy(isLoading = false, errorMessage = it.message)
                }
                .collect {
                    _state.value = _state.value.copy(buildingData = it, isLoading = false)
                }
        }
    }

}

data class BuildingDetailViewState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val buildingData: BuildingData? = null,
)
