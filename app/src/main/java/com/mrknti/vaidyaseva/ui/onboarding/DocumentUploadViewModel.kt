package com.mrknti.vaidyaseva.ui.onboarding

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrknti.vaidyaseva.Graph
import com.mrknti.vaidyaseva.data.UserDocumentType
import com.mrknti.vaidyaseva.data.network.handleError
import com.mrknti.vaidyaseva.data.user.User
import com.mrknti.vaidyaseva.filehandling.MediaData
import com.mrknti.vaidyaseva.ui.NavArgKeys
import com.squareup.moshi.JsonAdapter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DocumentUploadViewModel(saveState: SavedStateHandle) : ViewModel() {

    private val jsonAdapter: JsonAdapter<User> = Graph.moshi.adapter(User::class.java)
    private val userJson: String = requireNotNull(saveState[NavArgKeys.USER_DATA]) {
        "User data not found in saved state"
    }
    val user = jsonAdapter.fromJson(userJson)!!
    private val _state = MutableStateFlow(DocumentUploadUIState())
    private val authRepository = Graph.authRepository

    val state = _state.asStateFlow()

    fun setImageUri(uri: Uri?, documentType: Int) {
        when (documentType) {
            UserDocumentType.PASSPORT -> _state.value = _state.value.copy(passportUri = uri)
            UserDocumentType.VISA -> _state.value = _state.value.copy(visaUri = uri)
        }
    }

    fun uploadDocument(type: Int, mediaData: MediaData) {
        viewModelScope.launch {
            authRepository.uploadDocument(user.id, type, mediaData)
                .handleError {
                    mediaData.fileDescriptor.close()
                }
                .collect {
                    mediaData.fileDescriptor.close()
                    if (type == UserDocumentType.PASSPORT) {
                        _state.value = _state.value.copy(isPassportUploaded = true,)
                    } else {
                        _state.value = _state.value.copy(isVisaUploaded = true,)
                    }
                }
        }
    }

}

data class DocumentUploadUIState(
    val isLoading: Boolean = false,
    val error: String = "",
    val passportUri: Uri? = null,
    val isPassportUploaded: Boolean = false,
    val visaUri: Uri? = null,
    val isVisaUploaded: Boolean = false
)