package com.mrknti.vaidyaseva.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrknti.vaidyaseva.Graph
import com.mrknti.vaidyaseva.data.UserRoles
import com.mrknti.vaidyaseva.data.network.handleError
import com.mrknti.vaidyaseva.data.user.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OnboardingViewModel() : ViewModel() {
    private val _state = MutableStateFlow(OnboardingUIState())
    private val authRepository = Graph.authRepository
    private val userJsonAdapter = Graph.moshi.adapter(User::class.java)

    private val _actions = MutableStateFlow<OnboardActions?>(null)
    val actions = _actions.asStateFlow()

    val state = _state.asStateFlow()

    fun performSignup() {
        _state.value = _state.value.copy(isLoading = true)
        viewModelScope.launch {
            authRepository.registerUser(
                _state.value.firstName,
                state.value.lastName,
                _state.value.username,
                _state.value.password,
                _state.value.role
            )
            .handleError {
                    _state.value = _state.value.copy(isLoading = false, error = it.message ?: "")
            }
            .collect {
                _state.value =
                    _state.value.copy(isLoading = false, userJson = userJsonAdapter.toJson(it))
                _actions.value = OnboardActions.Signup
            }
        }
    }

    fun setUsername(value: String) {
        _state.value = _state.value.copy(username = value)
    }

    fun setPassword(value: String) {
        _state.value = _state.value.copy(password = value)
    }

    fun setFirstName(value: String) {
        _state.value = _state.value.copy(firstName = value)
    }

    fun setLastName(value: String) {
        _state.value = _state.value.copy(lastName = value)
    }

    fun setRole(value: String) {
        _state.value = _state.value.copy(role = value)
    }
}

data class OnboardingUIState(
    var username: String = "",
    var password: String = "",
    var firstName: String = "",
    var lastName: String = "",
    var role: String = UserRoles.CLIENT,
    val isLoading: Boolean = false,
    val error: String = "",
    val userJson: String? = null
)

sealed class OnboardActions {
    data object Signup : OnboardActions()
}