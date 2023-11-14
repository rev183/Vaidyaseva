package com.mrknti.vaidyaseva.data.eventBus

import android.net.Uri
import android.util.Log
import com.mrknti.vaidyaseva.data.building.RoomOccupancy
import com.mrknti.vaidyaseva.data.chat.ChatMessage
import com.mrknti.vaidyaseva.data.userService.Service
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterIsInstance
import kotlin.coroutines.coroutineContext

object EventBus {
    private const val TAG = "EventBus"
    private val _events = MutableSharedFlow<Any>()
    val events = _events.asSharedFlow()

    suspend fun publish(event: Any) {
        Log.d(TAG, "Emitting event = $event")
        _events.emit(event)
    }

    suspend inline fun <reified T> subscribe(crossinline onEvent: (T) -> Unit) {
        events.filterIsInstance<T>()
            .collectLatest { event ->
                coroutineContext.ensureActive()
                onEvent(event)
            }
    }
}

// New chat event
data class NewChatEvent(val chatMessage: ChatMessage)

// Service events
data class ServiceAcknowledgeEvent(val service: Service)
data class ServiceCompletedEvent(val service: Service)
data class ServiceRaisedEvent(val service: Service)

// Unauthorized access event
object UnAuthorizedAccessEvent

// document upload event
data class DocumentUploadEvent(val userId:Int, val documentType: Int, val documentUri: Uri?)

// building events
data class RoomBookedEvent(val occupancy: RoomOccupancy)
data class RoomCheckedOutEvent(val occupancyId: Int, val roomId: Int)