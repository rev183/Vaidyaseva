package com.mrknti.vaidyaseva.ui.chats

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mrknti.vaidyaseva.data.chat.ChatMessage
import com.mrknti.vaidyaseva.ui.services.ListState
import com.mrknti.vaidyaseva.ui.services.Loading
import com.mrknti.vaidyaseva.ui.services.PaginationLoading
import com.mrknti.vaidyaseva.util.DateFormat
import com.mrknti.vaidyaseva.util.formatDate
import kotlinx.coroutines.launch

@Composable
fun ChatDetail(modifier: Modifier = Modifier, threadId: Int? = null) {
    val viewModel: ChatDetailViewModel = viewModel()
    viewModel.setThreadId(threadId)
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    val actions = viewModel.actions.collectAsStateWithLifecycle()
    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val shouldStartPaginate = remember {
        derivedStateOf {
            viewModel.canPaginate && (lazyListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
                ?: -1) >= (lazyListState.layoutInfo.totalItemsCount - 3)
        }
    }

    LaunchedEffect(key1 = shouldStartPaginate.value) {
        if (shouldStartPaginate.value && viewState.listState == ListState.IDLE)
            viewModel.getChatDetail()
    }

    val context = LocalContext.current
    LaunchedEffect(key1 = viewState.error) {
        if (viewState.error.isNotEmpty()) {
            Log.d("Chats Detail", viewState.error)
            Toast.makeText(context, viewState.error, Toast.LENGTH_SHORT).show()
        }
    }
    LaunchedEffect(key1 = actions.value) {
        if (actions.value is ChatDetailActions.ScrollToTop) {
            coroutineScope.launch {
                lazyListState.scrollToItem(0)
            }
        }
    }

    Column(modifier.fillMaxSize()) {
        ChatMessages(
            messages = viewState.messages,
            listState = viewState.listState,
            scrollState = lazyListState,
            modifier = Modifier.weight(1f)
        )
        ChatInput(
            onSendMessage = {
                viewModel.sendMessage(it)
            }
        )
    }
}

@Composable
fun ChatMessages(
    messages: List<ChatMessage>,
    listState: ListState,
    scrollState: LazyListState,
    modifier: Modifier
) {
    LazyColumn(
        modifier = modifier,
        state = scrollState,
        reverseLayout = true,
        verticalArrangement = Arrangement.spacedBy(12.dp, alignment = Alignment.Bottom),
    ) {

        item {
            Spacer(modifier = Modifier.size(1.dp))
        }

        items(items = messages, key = { it.id }) {
            ChatMessageItem(it)
        }

        item(
            key = listState,
        ) {
            when (listState) {
                ListState.LOADING -> {
                    Loading()
                }

                ListState.PAGINATING -> {
                    PaginationLoading()
                }

                ListState.PAGINATION_EXHAUST -> {
                    Text(
                        text = "-----------\u2B24-----------",
                        modifier = Modifier
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }

                else -> {}
            }
        }
    }
}

@Composable
fun ChatInput(
    modifier: Modifier = Modifier,
    onSendMessage: (String) -> Unit,
    resetScroll: () -> Unit = {},
) {
    var textState by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue())
    }

    Box(
        modifier = modifier
            .zIndex(4.dp.value)
            .background(color = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.padding(4.dp)) {
            OutlinedTextField(
                value = textState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                onValueChange = { textState = it },
                placeholder = {
                    Text(
                        text = "Type your message here",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
            )
            Spacer(modifier = Modifier.size(8.dp))
            IconButton(
                onClick = {
                    onSendMessage(textState.text)
                    textState = TextFieldValue()
                    resetScroll()
                },
                enabled = textState.text.isNotEmpty()
            ) {
                Icon(
                    imageVector = Icons.Filled.Send,
                    contentDescription = "Send message",
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun ChatMessageItem(message: ChatMessage, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .padding(start = 16.dp, end = 16.dp)
            .shadow(4.dp, shape = MaterialTheme.shapes.medium)
            .background(color = MaterialTheme.colorScheme.surfaceVariant),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(8.dp)
        ) {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                Text(
                    text = message.senderName,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(text = message.createdAt.formatDate(DateFormat.HOUR_DAY_MONTH), style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.size(8.dp))
            Text(text = message.body, style = MaterialTheme.typography.bodySmall)
        }
    }
}