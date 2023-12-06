package com.mrknti.vaidyaseva.ui.updates

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mrknti.vaidyaseva.data.user.InboxItem
import com.mrknti.vaidyaseva.ui.components.EmptyView
import com.mrknti.vaidyaseva.ui.services.ListState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Inbox(modifier: Modifier = Modifier) {
    val viewModel: InboxViewModel = viewModel()
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    val lazyListState = rememberLazyListState()
    val localContext = LocalContext.current
    val shouldStartPaginate = remember {
        derivedStateOf {
            viewModel.canPaginate && (lazyListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
                ?: -1) >= (lazyListState.layoutInfo.totalItemsCount - 3)
        }
    }
    val pullState = rememberPullToRefreshState()
    LaunchedEffect(key1 = pullState.isRefreshing) {
        if (pullState.isRefreshing) {
            viewModel.loadInbox(reload = true)
        }
    }

    LaunchedEffect(key1 = shouldStartPaginate.value) {
        if (shouldStartPaginate.value && viewState.listState == ListState.IDLE)
            viewModel.loadInbox()
    }

    LaunchedEffect(key1 = viewState.error) {
        if (viewState.error.isNotEmpty()) {
            Toast.makeText(localContext, viewState.error, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(key1 = viewState.listState) {
        when (viewState.listState) {
            ListState.IDLE -> {
                pullState.endRefresh()
            }
            ListState.PAGINATION_EXHAUST -> {
                pullState.endRefresh()
            }
            ListState.LOADING -> {
                pullState.startRefresh()
            }
            else -> {

            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RectangleShape)
            .nestedScroll(pullState.nestedScrollConnection)
            .padding(start = 16.dp, end = 16.dp)
    ) {
        LazyColumn(modifier = modifier
            .fillMaxSize(),
            state = lazyListState,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Spacer(modifier = Modifier.size(1.dp))
            }

            items(viewState.inboxItems, key = { it.id }) { inboxItem ->
                InboxItem(
                    inboxItem = inboxItem,
                    modifier = Modifier.fillMaxWidth(),
                    onInboxItemClick = {

                    }
                )
            }
            if (viewState.inboxItems.isEmpty()) {
                item {
                    EmptyView(
                        title = "No notifications",
                        modifier = Modifier.fillParentMaxSize(),
                        subtitle = "You will see important updates here"
                    )
                }
            }
        }
        PullToRefreshContainer(
            modifier = Modifier.align(Alignment.TopCenter),
            state = pullState,
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    }
}

@Composable
fun InboxItem(
    inboxItem: InboxItem,
    modifier: Modifier = Modifier,
    onInboxItemClick: (InboxItem) -> Unit
) {
    Box(
        modifier = modifier
            .shadow(4.dp, shape = MaterialTheme.shapes.medium)
            .zIndex(4.dp.value)
            .background(color = MaterialTheme.colorScheme.secondary)
            .clickable { onInboxItemClick(inboxItem) },
        contentAlignment = Alignment.Center
    ) {
        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.Top) {
            Box(modifier = Modifier
                .padding(4.dp)
                .size(28.dp)
                .background(color = MaterialTheme.colorScheme.onSecondary, shape = CircleShape),
            ) {
                Image(
                    painter = painterResource(id = inboxItem.imageRes!!),
                    modifier = Modifier
                        .size(20.dp)
                        .align(Alignment.Center),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(Color.White)
                )
            }
            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Top,
                modifier = Modifier
                    .padding(start = 8.dp, end = 8.dp)
                    .weight(1f)
            ) {
                Text(
                    text = inboxItem.title,
                    color = MaterialTheme.colorScheme.onSecondary,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Start,
                )
                if (inboxItem.body != null) {
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = inboxItem.body,
                        color = MaterialTheme.colorScheme.onSecondary,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}
