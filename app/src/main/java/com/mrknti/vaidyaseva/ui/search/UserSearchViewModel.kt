package com.mrknti.vaidyaseva.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrknti.vaidyaseva.Graph
import com.mrknti.vaidyaseva.data.network.handleError
import com.mrknti.vaidyaseva.data.user.User
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class UserSearchViewModel : ViewModel() {
    private val userRepository = Graph.userRepository
    private val _state = MutableStateFlow(UserSearchViewState())
    val state = _state.asStateFlow()
    val searchQuery: Flow<String> = MutableSharedFlow(1)

    init {
        viewModelScope.launch {
            searchQuery
                .distinctUntilChanged()
                .filter { it.length > 2 }
                .debounce(300)
                .flatMapLatest { userRepository.searchUser(it) }
                .handleError { _state.value = _state.value.copy(errorMessage = it.message ?: "") }
                .collect {
                    _state.value = _state.value.copy(searchResults = it)
                }
        }
    }

    fun setSelectedUser(user: User) {
        _state.value = _state.value.copy(selectedUser = user)
    }

    fun onSearchQueryChange(query: String) {
        viewModelScope.launch {
            (searchQuery as MutableSharedFlow).emit(query)
        }
    }

    fun onClearSearch() {
        viewModelScope.launch {
            (searchQuery as MutableSharedFlow).emit("")
            _state.value = _state.value.copy(searchResults = emptyList())
        }
    }

}

data class UserSearchViewState(
    val errorMessage: String = "",
    val selectedUser: User? = null,
    val searchResults: List<User> = emptyList()
)
