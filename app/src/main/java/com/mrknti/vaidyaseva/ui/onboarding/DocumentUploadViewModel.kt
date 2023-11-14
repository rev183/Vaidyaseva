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

    init {
        getCurrentDocuments()
    }

    fun setImageUri(uri: Uri?, documentType: Int) {
        when (documentType) {
            UserDocumentType.PASSPORT -> _state.value = _state.value.copy(passportUri = uri)
            UserDocumentType.VISA -> _state.value = _state.value.copy(visaUri = uri)
        }
    }

    private fun getCurrentDocuments() {
        viewModelScope.launch {
            userRepository.getAllDocuments(user.id)
                .handleError { _state.value = _state.value.copy(error = it.message ?: "") }
                .collect { userDocuments ->
                    userDocuments.find { it.documentType == UserDocumentType.PASSPORT }?.let {
                        _state.value = _state.value.copy(
                            passportUrl = getDocumentUrl(it.id),
                            passportExpiry = it.expiryTime,
                            passportStatus = UploadStatus.UPLOADED,
                        )
                    }
                    userDocuments.find { it.documentType == UserDocumentType.VISA }?.let {
                        _state.value = _state.value.copy(
                            visaUrl = getDocumentUrl(it.id),
                            visaExpiry = it.expiryTime,
                            visaStatus = UploadStatus.UPLOADED,
                        )
                    }
                }
        }
    }

    fun uploadDocument(type: Int, expiry: Date, mediaData: MediaData) {
        updateUploadStatus(type, UploadStatus.UPLOADING)
        viewModelScope.launch {
            userRepository.uploadDocument(user.id, type, expiry, mediaData)
                .handleError {
                    updateUploadStatus(type, UploadStatus.NOT_UPLOADED)
                    mediaData.fileDescriptor.close()
                }
                .collect {
                    mediaData.fileDescriptor.close()
                    updateUploadStatus(type, UploadStatus.UPLOADED)
                    EventBus.publish(
                        DocumentUploadEvent(
                            user.id,
                            type,
                            if (type == UserDocumentType.PASSPORT) state.value.passportUri
                            else state.value.visaUri
                        )
                    )
                }
        }
    }

    fun clearDocument(type: Int) {
        if (type == UserDocumentType.PASSPORT) {
            _state.value = _state.value.copy(
                passportUri = null,
                passportUrl = null,
                passportExpiry = null,
                passportStatus = UploadStatus.NOT_UPLOADED
            )
        } else {
            _state.value = _state.value.copy(
                visaUri = null,
                visaUrl = null,
                visaExpiry = null,
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
    val passportUri: Uri? = null,
    val passportUrl: String? = null,
    val passportExpiry: Date? = null,
    val passportStatus: UploadStatus = UploadStatus.NOT_UPLOADED,
    val visaUri: Uri? = null,
    val visaUrl: String?= null,
    val visaExpiry: Date? = null,
    val visaStatus: UploadStatus = UploadStatus.NOT_UPLOADED,
)

enum class UploadStatus {
    NOT_UPLOADED,
    UPLOADING,
    UPLOADED
}