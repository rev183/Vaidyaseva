package com.mrknti.vaidyaseva.ui.chats

import android.widget.Toast
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mrknti.vaidyaseva.data.chat.ChatThread
import com.mrknti.vaidyaseva.ui.components.LoadingView
import com.mrknti.vaidyaseva.ui.services.ListState
import com.mrknti.vaidyaseva.ui.services.ServicesViewModel

@Composable
fun Chats(modifier: Modifier = Modifier, onChatClick: (ChatThread) -> Unit) {
    val viewModel: ChatsViewModel = viewModel()
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    val lazyColumnListState = rememberLazyListState()

    if (viewState.isLoading) {
        LoadingView(alignment = Alignment.TopCenter)
    } else if (viewState.error.isNotEmpty()) {
        Toast.makeText(LocalContext.current, viewState.error, Toast.LENGTH_SHORT).show()
    }

    LazyColumn(state = lazyColumnListState, modifier = modifier) {
        item {
            Text(text = "Chats", style = MaterialTheme.typography.titleLarge)
        }
        items(viewState.chats, key = { it.id }) {
            ChatThreadItem(chat = it, onChatClick = onChatClick)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatThreadItem(chat: ChatThread, onChatClick: (ChatThread) -> Unit) {
    Card(onClick = { onChatClick(chat) }) {
        val message = chat.messages.firstOrNull()
        Text(text = "${message?.id}", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.size(8.dp))
        Text(text = "${message?.id}", style = MaterialTheme.typography.bodySmall)
    }
}