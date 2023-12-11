package com.mrknti.vaidyaseva.ui.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrknti.vaidyaseva.Graph
import com.mrknti.vaidyaseva.data.UserDocumentType
import com.mrknti.vaidyaseva.data.building.HostelRoom
import com.mrknti.vaidyaseva.data.eventBus.DocumentUploadEvent
import com.mrknti.vaidyaseva.data.eventBus.EventBus
import com.mrknti.vaidyaseva.data.eventBus.RoomBookedEvent
import com.mrknti.vaidyaseva.data.eventBus.RoomCheckedOutEvent
import com.mrknti.vaidyaseva.data.getDocumentUrl
import com.mrknti.vaidyaseva.data.network.handleError
import com.mrknti.vaidyaseva.data.user.User
import com.mrknti.vaidyaseva.data.user.UserInfo
import com.mrknti.vaidyaseva.ui.NavArgKeys
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.Date

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class UserSearchViewModel(saveState: SavedStateHandle) : ViewModel() {
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
    val searchType: SearchType = saveState[NavArgKeys.SEARCH_TYPE] ?: SearchType.MAIN

    init {
        viewModelScope.launch {
            searchQuery
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
                        _state.value = _state.value.copy(passportData = it.documentUri)
                    } else {
                        _state.value = _state.value.copy(visaData = it.documentUri)
                    }
                }
            }
        }
    }

    fun setSelectedUser(user: User) {
        if (user != _state.value.selectedUser) {
            _state.value = _state.value.copy(
                selectedUser = user,
                passportData = emptyList(),
            )
            viewModelScope.launch {
                userRepository.getUserInfo(user.id)
                    .handleError { _state.value = _state.value.copy(errorMessage = it.message ?: "") }
                    .collect { userInfo ->
                        val userDocuments = userInfo.documents
                        if (!userDocuments.isNullOrEmpty()) {
                            val passportData = mutableListOf<Any>()
                            val visaData = mutableListOf<Any>()
                            var passportExpiry: Date? = null
                            var visaExpiry: Date? = null

                            userDocuments.forEach {
                                if (it.documentType == UserDocumentType.PASSPORT) {
                                    passportData.add(getDocumentUrl(it.id))
                                    passportExpiry = it.expiryTime
                                } else {
                                    visaData.add(getDocumentUrl(it.id))
                                    visaExpiry = it.expiryTime
                                }
                            }
                            _state.value = _state.value.copy(
                                passportData = passportData,
                                passportExpiry = passportExpiry,
                                visaData = visaData,
                                visaExpiry = visaExpiry
                            )
                        }
                        _state.value = _state.value.copy(selectedUserInfo = userInfo)
                    }
            }
        }
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
        _state.value = _state.value.copy(selectedUser = null, selectedUserInfo = null)
    }

    fun bookRoom(room: HostelRoom, checkIn: Date, checkOut: Date, buildingId: Int) {
        viewModelScope.launch {
            buildingRepository.bookRoom(room.id, _state.value.selectedUser!!.id, checkIn, checkOut)
                .handleError { _state.value = _state.value.copy(errorMessage = it.message ?: "") }
                .collect {
                    _state.value = _state.value.copy(selectedUser = null)
                    _action.emit(UserSearchViewAction.RoomBooked)
                    EventBus.publish(RoomBookedEvent(it, buildingId))
                }
        }
    }

    fun checkOutOccupancy(occupancyId: Int, roomId: Int, buildingId: Int) {
        viewModelScope.launch {
            buildingRepository.checkOutOccupancy(occupancyId)
                .handleError { _state.value = _state.value.copy(errorMessage = it.message ?: "") }
                .collect {
                    _action.emit(UserSearchViewAction.OccupancyCheckedOut(occupancyId))
                    EventBus.publish(
                        RoomCheckedOutEvent(
                            occupancyId,
                            roomId,
                            buildingId,
                            it.status
                        )
                    )
                }
        }
    }

    fun clearData() {
        _state.value = UserSearchViewState()
        _action.value = null
        viewModelScope.launch {
            (searchQuery as MutableSharedFlow).emit("")
        }
    }

}

data class UserSearchViewState(
    val errorMessage: String = "",
    val selectedUser: User? = null,
    val selectedUserInfo: UserInfo? = null,
    val passportData: List<Any> = emptyList(),
    val passportExpiry: Date? = null,
    val visaData: List<Any> = emptyList(),
    val visaExpiry: Date? = null,
    val searchResults: List<User> = emptyList()
)

sealed class UserSearchViewAction {
    data object RoomBooked : UserSearchViewAction()
    data class OccupancyCheckedOut(val occupancyId: Int) : UserSearchViewAction()
}
