package com.mrknti.vaidyaseva.ui.services

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrknti.vaidyaseva.Graph
import com.mrknti.vaidyaseva.data.network.handleError
import com.mrknti.vaidyaseva.data.userService.Service
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ServicesViewModel : ViewModel() {
    private val servicesRepository = Graph.servicesRepository
    private val _state = MutableStateFlow(ServicesUIState())
    val state = _state.asStateFlow()
    private var page by mutableIntStateOf(1)
    var canPaginate by mutableStateOf(true)
    private var listState by mutableStateOf(ListState.IDLE)
    private val serviceList = mutableStateListOf<Service>()

    fun getServices() {
        if (page == 1 || (page != 1 && canPaginate) && listState == ListState.IDLE) {
            listState = if (page == 1) ListState.LOADING else ListState.PAGINATING
            _state.value = _state.value.copy(listState = listState)
            viewModelScope.launch {
                servicesRepository.getOpenServices(1)
                    .handleError { e ->
                        _state.value =
                            _state.value.copy(listState = ListState.ERROR, error = e.message ?: "")
                    }
                    .collect {
                        canPaginate = it.size >= 20
                        if (page == 1) {
                            serviceList.clear()
                        }
                        serviceList.addAll(it)
                        listState = if (canPaginate) {
                            page++
                            ListState.IDLE
                        } else {
                            ListState.PAGINATION_EXHAUST
                        }
                        _state.value =
                            _state.value.copy(listState = listState, services = serviceList)
                    }
            }
        }
    }

    override fun onCleared() {
        page = 1
        listState = ListState.IDLE
        canPaginate = false
        super.onCleared()
    }
}

data class ServicesUIState(
    val listState: ListState = ListState.IDLE,
    val services: List<Service> = emptyList(),
    val error: String = ""
)

enum class ListState {
    IDLE,
    LOADING,
    PAGINATING,
    ERROR,
    PAGINATION_EXHAUST,
}