package com.mrknti.vaidyaseva.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrknti.vaidyaseva.Graph
import com.mrknti.vaidyaseva.data.network.handleError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class AuthViewModel() : ViewModel() {

    private val _state = MutableStateFlow(AuthUIState())
    val state = _state.asStateFlow()

    private val _actions = MutableStateFlow<AuthActions?>(null)
    val actions = _actions.asStateFlow()

    private val dataManager = Graph.dataStoreManager
    private val authRepository = Graph.authRepository

    fun loginOrSignup() {
        _state.value = _state.value.copy(isLoading = true)
        viewModelScope.launch {
            val authFlow = if (_state.value.authMode == AuthMode.LOGIN) {
                authRepository.login(_state.value.username, _state.value.password)
                    .handleError { e ->
                        _state.value = _state.value.copy(isLoading = false, error = e.message ?: "") }
            } else {
                authRepository.signup(_state.value.username, _state.value.password)
            }
            authFlow
                .map {
                    dataManager.saveAuthToken(it.authToken)
                    dataManager.saveUser(it.userId, it.roles)
                    _state.value = _state.value.copy(isLoading = false)
                    if (dataManager.isFCMRegistrationPending) {
                        authRepository.registerFCMToken(dataManager.fcmToken.first())
                            .handleError { e ->
                                _state.value = _state.value.copy(error = e.message ?: "")
                            }.collect {
                                dataManager.isFCMRegistrationPending = false
                                _actions.value = AuthActions.Login
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

    fun setAuthMode(authMode: AuthMode) {
        _state.value = _state.value.copy(authMode = authMode)
    }
}

data class AuthUIState(
    var username: String = "",
    var password: String = "",
    var authMode: AuthMode = AuthMode.LOGIN,
    val isLoginButtonEnabled: Boolean = false,
    val isLoading: Boolean = false,
    val error: String = ""
)

sealed class AuthActions {
    data object Login : AuthActions()
}

enum class AuthMode {
    LOGIN, SIGNUP
}