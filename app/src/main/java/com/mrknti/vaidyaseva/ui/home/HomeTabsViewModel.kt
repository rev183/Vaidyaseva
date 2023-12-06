package com.mrknti.vaidyaseva.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrknti.vaidyaseva.Graph
import com.mrknti.vaidyaseva.data.network.handleError
import com.mrknti.vaidyaseva.data.user.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class HomeTabsViewModel : ViewModel() {
    private val _state = MutableStateFlow(HomeTabsViewState())
    val state = _state.asStateFlow()
    private val userRepository = Graph.userRepository
    private val dataStoreManager = Graph.dataStoreManager
    private val authRepository = Graph.authRepository

    init {
        viewModelScope.launch {
            dataStoreManager.getUser().combine(dataStoreManager.authFlow) {
                    user, loginState -> user to loginState
            }.collect { (user, loginState) ->
                _state.value = _state.value.copy(selfUser = user, loginState = loginState)
            }
        }

        viewModelScope.launch {
            dataStoreManager.authFlow.collect { loginState ->
                if (loginState == LoginState.LoggedIn) {
                    fetchUserInfo()
                }
            }
        }
    }

    private fun fetchUserInfo() {
        viewModelScope.launch {
            userRepository.getUserInfo(null)
                .handleError { e ->
                    Log.e("HomeTabsViewModel", "Error fetching user info", e)
                }
                .collect {
                    dataStoreManager.saveUserInfo(it)
                }
        }
    }

    fun performLogout() {
        viewModelScope.launch {
            authRepository.logout(dataStoreManager.getRegisteredDevice().first())
                .handleError {  }
                .collect {
                    dataStoreManager.clearOnLogout()
                    _state.value = _state.value.copy(selfUser = null, loginState = LoginState.NotLoggedIn)
                }
        }
    }

}

data class HomeTabsViewState(
    val selfUser: User? = null,
    val loginState: LoginState = LoginState.Loading
)

sealed class LoginState {
    data object Loading : LoginState()
    data object NotLoggedIn : LoginState()
    data object LoggedIn : LoginState()
}