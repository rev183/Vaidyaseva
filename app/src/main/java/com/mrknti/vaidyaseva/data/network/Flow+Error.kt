package com.mrknti.vaidyaseva.data.network

import android.util.Log
import com.mrknti.vaidyaseva.data.HttpCodes
import com.mrknti.vaidyaseva.data.eventBus.EventBus
import com.mrknti.vaidyaseva.data.eventBus.UnAuthorizedAccessEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch

fun <T> Flow<T>.handleError(handler: (Throwable) -> Unit): Flow<T> =
    catch { e ->
        Log.e("Flow+Error", "Error in flow", e)
        if (e is VsNetworkException) {
            if (e.statusCode == HttpCodes.UNAUTHORIZED) {
                EventBus.publish(UnAuthorizedAccessEvent)
            }
        }

        handler.invoke(e)
    }

class VsNetworkException(errorBody: VsErrorBody) : RuntimeException(errorBody.message) {
    val statusCode = errorBody.statusCode
}

data class VsErrorBody(val statusCode: Int, val message: String)