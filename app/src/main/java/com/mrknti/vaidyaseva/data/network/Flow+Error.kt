package com.mrknti.vaidyaseva.data.network

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch

fun <T> Flow<T>.handleError(handler: (Throwable) -> Unit): Flow<T> =
    catch { e -> handler.invoke(e) }
