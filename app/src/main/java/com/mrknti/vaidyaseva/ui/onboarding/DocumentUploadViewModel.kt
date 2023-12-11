package com.mrknti.vaidyaseva.ui.onboarding

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrknti.vaidyaseva.Graph
import com.mrknti.vaidyaseva.data.UserDocumentType
import com.mrknti.vaidyaseva.data.eventBus.DocumentUploadEvent
import com.mrknti.vaidyaseva.data.eventBus.EventBus
import com.mrknti.vaidyaseva.data.getDocumentUrl
import com.mrknti.vaidyaseva.data.network.handleError
import com.mrknti.vaidyaseva.data.user.User
import com.mrknti.vaidyaseva.filehandling.MediaData
import com.mrknti.vaidyaseva.ui.NavArgKeys
import com.squareup.moshi.JsonAdapter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.Date

class DocumentUploadViewModel(saveState: SavedStateHandle) : ViewModel() {

    private val jsonAdapter: JsonAdapter<User> = Graph.moshi.adapter(User::class.java)
    private val userJson: String = requireNotNull(saveState[NavArgKeys.USER_DATA]) {
        "User data not found in saved state"
    }
    val user = jsonAdapter.fromJson(userJson)!!
    private val _state = MutableStateFlow(DocumentUploadUIState())
    private val userRepository = Graph.userRepository
    val state = _state.asStateFlow()
    val authToken: String = runBlocking { Graph.dataStoreManager.authToken.first()!! }
    private val maxAttachments = 4
    private val documentIdMap: MutableMap<String, Int> = mutableMapOf()

    init {
        getCurrentDocuments()
    }

    fun setImageUri(uris: List<Uri>, documentType: Int) {
        when (documentType) {
            UserDocumentType.PASSPORT -> {
                val passportData = addUrisToLimit(state.value.passportData.toMutableList(), uris)
                _state.value = _state.value.copy(passportData = passportData)
            }
            UserDocumentType.VISA -> {
                val visaData = addUrisToLimit(state.value.visaData.toMutableList(), uris)
                _state.value = _state.value.copy(visaData = visaData)
            }
        }
    }

    private fun addUrisToLimit(data: MutableList<Any>, uris: List<Uri>) : List<Any> {
        var existingSize = data.size
        var i = 0
        while (existingSize < maxAttachments && i < uris.size) {
            data.add(uris[i])
            existingSize++
            i++
        }
        return data
    }

    private fun getCurrentDocuments() {
        viewModelScope.launch {
            userRepository.getAllDocuments(user.id)
                .handleError { _state.value = _state.value.copy(error = it.message ?: "") }
                .collect { userDocuments ->
                    if (userDocuments.isNotEmpty()) {
                        val passportData = mutableListOf<Any>()
                        val visaData = mutableListOf<Any>()
                        var passportExpiry: Date? = null
                        var visaExpiry: Date? = null

                        userDocuments.forEach {
                            val url = getDocumentUrl(it.id)
                            if (it.documentType == UserDocumentType.PASSPORT) {
                                passportData.add(url)
                                documentIdMap[url] = it.id
                                passportExpiry = it.expiryTime
                            } else {
                                visaData.add(url)
                                documentIdMap[url] = it.id
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
                }
        }
    }

    fun uploadDocument(type: Int, expiry: Date, mediaData: List<MediaData>) {
        updateUploadStatus(type, UploadStatus.UPLOADING)
        viewModelScope.launch {
            userRepository.uploadDocument(user.id, type, expiry, mediaData)
                .handleError {
                    updateUploadStatus(type, UploadStatus.NOT_UPLOADED)
                    mediaData.forEach { it.fileDescriptor.close() }
                }
                .collect {
                    mediaData.forEach { it.fileDescriptor.close() }
                    updateUploadStatus(type, UploadStatus.UPLOADED)
                    EventBus.publish(
                        DocumentUploadEvent(
                            user.id,
                            type,
                            if (type == UserDocumentType.PASSPORT) state.value.passportData
                            else state.value.visaData
                        )
                    )
                }
        }
    }

    fun clearDocument(type: Int, position: Int) {
        val url = if (type == UserDocumentType.PASSPORT) {
            state.value.passportData[position]
        } else {
            state.value.visaData[position]
        }
        // is remote file url
        if (url is String && documentIdMap.containsKey(url)) {
            viewModelScope.launch {
                userRepository.deleteDocument(documentIdMap[url]!!)
                    .handleError { _state.value = _state.value.copy(error = it.message ?: "") }
                    .collect {
                        removeDocument(type, position)
                    }
            }
        } else {
            // directly remove local file
            removeDocument(type, position)
        }
    }

    private fun removeDocument(type: Int, position: Int) {
        if (type == UserDocumentType.PASSPORT) {
            val uris = state.value.passportData.toMutableList()
            val expiry = if (uris.size > 1) state.value.passportExpiry else null
            _state.value = _state.value.copy(
                passportData = uris.apply { removeAt(position) },
                passportExpiry = expiry,
                passportStatus = UploadStatus.NOT_UPLOADED
            )
        } else {
            val uris = state.value.visaData.toMutableList()
            val expiry = if (uris.size > 1) state.value.passportExpiry else null
            _state.value = _state.value.copy(
                visaData = uris.apply { removeAt(position) },
                visaExpiry = expiry,
                visaStatus = UploadStatus.NOT_UPLOADED
            )
        }
    }

    private fun updateUploadStatus(type: Int, status: UploadStatus) {
        if (type == UserDocumentType.PASSPORT) {
            _state.value = _state.value.copy(passportStatus = status)
        } else {
            _state.value = _state.value.copy(visaStatus = status)
        }
    }

}

data class DocumentUploadUIState(
    val isLoading: Boolean = false,
    val error: String = "",
    val passportData: List<Any> = emptyList(),
    val passportExpiry: Date? = null,
    val passportStatus: UploadStatus = UploadStatus.NOT_UPLOADED,
    val visaData: List<Any> = emptyList(),
    val visaExpiry: Date? = null,
    val visaStatus: UploadStatus = UploadStatus.NOT_UPLOADED,
)

enum class UploadStatus {
    NOT_UPLOADED,
    UPLOADING,
    UPLOADED
}