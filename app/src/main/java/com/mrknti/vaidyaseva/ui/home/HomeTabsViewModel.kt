package com.mrknti.vaidyaseva.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrknti.vaidyaseva.Graph
import com.mrknti.vaidyaseva.data.user.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class HomeTabsViewModel : ViewModel() {
    private val _state = MutableStateFlow(HomeViewState())
    val state = _state.asStateFlow()
    private val dataStoreManager = Graph.dataStoreManager
    private val authFlow = dataStoreManager.authToken.map {
        if (it == null) {
            LoginState.NotLoggedIn
        } else {
            LoginState.LoggedIn
        }
    }

    init {
        viewModelScope.launch {
            dataStoreManager.getUser().combine(authFlow) {
                    user, loginState -> user to loginState
            }.collect { (user, loginState) ->
                _state.value = _state.value.copy(selfUser = user, loginState = loginState)
            }
        }
    }

    fun performLogout() {
        viewModelScope.launch {
            dataStoreManager.clearOnLogout()
            _state.value = _state.value.copy(selfUser = null, loginState = LoginState.NotLoggedIn)
        }
    }

}

data class HomeViewState(
    val selfUser: User? = null,
    val loginState: LoginState = LoginState.Loading
)

sealed class LoginState {
    data object Loading : LoginState()
    data object NotLoggedIn : LoginState()
    data object LoggedIn : LoginState()
}