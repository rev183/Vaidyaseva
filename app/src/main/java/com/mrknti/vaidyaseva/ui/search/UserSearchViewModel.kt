package com.mrknti.vaidyaseva.ui.search

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrknti.vaidyaseva.Graph
import com.mrknti.vaidyaseva.data.UserDocumentType
import com.mrknti.vaidyaseva.data.building.HostelRoom
import com.mrknti.vaidyaseva.data.eventBus.DocumentUploadEvent
import com.mrknti.vaidyaseva.data.eventBus.EventBus
import com.mrknti.vaidyaseva.data.eventBus.RoomBookedEvent
import com.mrknti.vaidyaseva.data.getDocumentUrl
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.Date

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class UserSearchViewModel : ViewModel() {
    private val userRepository = Graph.userRepository
    private val buildingRepository = Graph.buildingRepository
    val searchQuery: Flow<String> = MutableSharedFlow(1)
    private val _state = MutableStateFlow(UserSearchViewState())
    val state = _state.asStateFlow()
    private val _action = MutableStateFlow<UserSearchViewAction?>(null)
    val action = _action.asStateFlow()
    val authToken: String = runBlocking { Graph.dataStoreManager.authToken.first()!! }
    private val userJsonAdapter = Graph.moshi.adapter(User::class.java)
    val selectedUserJson: String
    get() = userJsonAdapter.toJson(state.value.selectedUser)

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

        viewModelScope.launch {
            EventBus.subscribe<DocumentUploadEvent> {
                if (it.userId == state.value.selectedUser?.id) {
                    if (it.documentType == UserDocumentType.PASSPORT) {
                        _state.value = _state.value.copy(passportUri = it.documentUri)
                    } else {
                        _state.value = _state.value.copy(visaUri = it.documentUri)
                    }
                }
            }
        }
    }

    fun setSelectedUser(user: User) {
        if (user != _state.value.selectedUser) {
            viewModelScope.launch {
                userRepository.getAllDocuments(user.id)
                    .handleError { _state.value = _state.value.copy(errorMessage = it.message ?: "") }
                    .collect { userDocuments ->
                        userDocuments.find { it.documentType == UserDocumentType.PASSPORT }?.id?.let { id ->
                            _state.value = _state.value.copy(passportUrl = getDocumentUrl(id))
                        }
                        userDocuments.find { it.documentType == UserDocumentType.VISA }?.id?.let { id ->
                            _state.value = _state.value.copy(visaUrl = getDocumentUrl(id))
                        }
                    }
            }
        }
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

    fun clearSelectedUser() {
        _state.value = _state.value.copy(selectedUser = null)
    }

    fun bookRoom(room: HostelRoom, checkIn: Date, checkOut: Date) {
        viewModelScope.launch {
            buildingRepository.bookRoom(room.id, _state.value.selectedUser!!.id, checkIn, checkOut)
                .handleError { _state.value = _state.value.copy(errorMessage = it.message ?: "") }
                .collect {
                    _state.value = _state.value.copy(selectedUser = null)
                    _action.emit(UserSearchViewAction.RoomBooked)
                    EventBus.publish(RoomBookedEvent(it))
                }
        }
    }

}

data class UserSearchViewState(
    val errorMessage: String = "",
    val selectedUser: User? = null,
    val passportUrl: String? = null,
    val passportUri: Uri? = null,
    val visaUrl: String? = null,
    val visaUri: Uri? = null,
    val searchResults: List<User> = emptyList()
)

sealed class UserSearchViewAction {
    data object RoomBooked : UserSearchViewAction()
}
