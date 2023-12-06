package com.mrknti.vaidyaseva.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrknti.vaidyaseva.Graph
import com.mrknti.vaidyaseva.data.eventBus.EventBus
import com.mrknti.vaidyaseva.data.eventBus.ReFetchFCMTokenEvent
import com.mrknti.vaidyaseva.data.network.handleError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val _state = MutableStateFlow(AuthUIState())
    val state = _state.asStateFlow()

    private val _actions = MutableStateFlow<AuthActions?>(null)
    val actions = _actions.asStateFlow()

    private val dataManager = Graph.dataStoreManager
    private val authRepository = Graph.authRepository

    fun performLogin() {
        _state.value = _state.value.copy(isLoading = true)
        viewModelScope.launch {
            authRepository.login(_state.value.username, _state.value.password)
                .handleError { e ->
                    _state.value = _state.value.copy(isLoading = false, error = e.message ?: "") }
                .map { authData ->
                    dataManager.saveAuthToken(authData.authToken)
                    dataManager.saveUser(authData.userId, authData.displayName, authData.roles)
                    _state.value = _state.value.copy(isLoading = false)
                    if (!dataManager.isFCMRegistrationCompleted) {
                        val token = dataManager.fcmToken.first()
                        if (token.isEmpty()) {
                            _actions.value = AuthActions.Login
                            EventBus.publish(ReFetchFCMTokenEvent)
                        } else {
                            authRepository.registerFCMToken(token, dataManager.getRegisteredDevice().first())
                                .handleError { e ->
                                    _state.value = _state.value.copy(error = e.message ?: "")
                                }.collect {
                                    dataManager.isFCMRegistrationCompleted = true
                                    dataManager.saveRegisteredDevice(it.deviceId)
                                    _actions.value = AuthActions.Login
                                }
                        }
                    } else {
                        _actions.value = AuthActions.Login
                    }
                }
                .stateIn(viewModelScope)
        }
    }

    fun setUsername(username: String) {
        _state.value = _state.value.copy(username = username)
    }

    fun setPassword(password: String) {
        _state.value = _state.value.copy(password = password)
    }
}

data class AuthUIState(
    var username: String = "",
    var password: String = "",
    var authMode: AuthMode = AuthMode.LOGIN,
    val isLoading: Boolean = false,
    val error: String = "",
)

sealed class AuthActions {
    data object Login : AuthActions()
}

enum class AuthMode {
    LOGIN, SIGNUP
}