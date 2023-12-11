package com.mrknti.vaidyaseva.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.mrknti.vaidyaseva.Graph
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

@Composable
fun FullScreenImage(url: String) {
    val token = remember {
        runBlocking { Graph.dataStoreManager.authToken.first()!! }
    }
    val context = LocalContext.current
    @Suppress("IMPLICIT_CAST_TO_ANY")
    val imageModel = if (url.startsWith("http")) {
        ImageRequest.Builder(context)
            .data(url)
            .addHeader("token", token)
            .build()
    } else {
        url
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AsyncImage(
            model = imageModel,
            contentDescription = "Full Image",
            modifier = Modifier.align(Alignment.Center)
        )
    }
}