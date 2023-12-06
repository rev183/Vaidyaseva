package com.mrknti.vaidyaseva.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrknti.vaidyaseva.Graph
import com.mrknti.vaidyaseva.data.eventBus.EventBus
import com.mrknti.vaidyaseva.data.eventBus.ReFetchFCMTokenEvent
import com.mrknti.vaidyaseva.data.eventBus.UnAuthorizedAccessEvent
import com.mrknti.vaidyaseva.data.network.handleError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val dataStoreManager = Graph.dataStoreManager
    private val authRepository = Graph.authRepository
    private val _actions = MutableStateFlow<MainViewModelActions?>(null)
    val actions = _actions.asStateFlow()

    init {
        viewModelScope.launch {
            EventBus.subscribe<UnAuthorizedAccessEvent> {
                showReLoginDialog()
            }
        }
        viewModelScope.launch {
            EventBus.subscribe<ReFetchFCMTokenEvent> {
                _actions.value = MainViewModelActions.ReFetchFCMToken
            }
        }
    }

    fun performLogout() {
        viewModelScope.launch {
            authRepository.logout(dataStoreManager.getRegisteredDevice().first())
                .handleError { _actions.value = null }
                .collect {
                    dataStoreManager.clearOnLogout()
                    _actions.value = null
                }
        }
    }

    private fun showReLoginDialog() {
        _actions.value = MainViewModelActions.ShowReLoginDialog
    }

    fun showNotifPermissionDialog() {
        _actions.value = MainViewModelActions.ShowNotifPromptDialog
    }

    fun clearAction() {
        _actions.value = null
    }
}



sealed class MainViewModelActions {
    data object ShowReLoginDialog : MainViewModelActions()
    data object ShowNotifPromptDialog : MainViewModelActions()
    data object ReFetchFCMToken : MainViewModelActions()
}