package com.mrknti.vaidyaseva.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrknti.vaidyaseva.Graph
import com.mrknti.vaidyaseva.data.eventBus.EventBus
import com.mrknti.vaidyaseva.data.eventBus.UnAuthorizedAccessEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val dataStoreManager = Graph.dataStoreManager
    private val _actions = MutableStateFlow<MainViewModelActions?>(null)
    val actions = _actions.asStateFlow()

    init {
        viewModelScope.launch {
            EventBus.subscribe<UnAuthorizedAccessEvent> {
                showReLoginDialog()
            }
        }
    }

    fun performLogout() {
        viewModelScope.launch {
            dataStoreManager.clearOnLogout()
        }
        _actions.value = null
    }

    private fun showReLoginDialog() {
        _actions.value = MainViewModelActions.ShowReLoginDialog
    }
}



sealed class MainViewModelActions {
    data object ShowReLoginDialog : MainViewModelActions()
}