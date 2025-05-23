package com.mrknti.vaidyaseva.ui.services

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
import com.mrknti.vaidyaseva.data.ServiceStatus
import com.mrknti.vaidyaseva.data.ServiceType
import com.mrknti.vaidyaseva.data.userService.Service
import com.mrknti.vaidyaseva.ui.components.EmptyView
import com.mrknti.vaidyaseva.ui.components.LoadingView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Services(
    modifier: Modifier = Modifier,
    onServiceClick: (Service) -> Unit,
    serviceStatus: String
) {
    val viewModel: ServicesViewModel =
        viewModel(key = serviceStatus, factory = ServicesViewModelFactory(serviceStatus))
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    val lazyListState = rememberLazyListState()
    val localContext = LocalContext.current
    val shouldStartPaginate = remember {
        derivedStateOf {
            viewModel.canPaginate && (lazyListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
                ?: -1) >= (lazyListState.layoutInfo.totalItemsCount - 3)
        }
    }
    val emptyMessage = remember {
        if (serviceStatus == ServiceStatus.RAISED) {
            "Your booked services will be here"
        } else {
            "Your fulfilled services will be here"
        }
    }
    val pullState = rememberPullToRefreshState()
    LaunchedEffect(key1 = pullState.isRefreshing) {
        if (pullState.isRefreshing) {
            viewModel.getServices(true)
        }
    }

    LaunchedEffect(key1 = shouldStartPaginate.value) {
        if (shouldStartPaginate.value && viewState.listState == ListState.IDLE)
            viewModel.getServices()
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
        LazyColumn(
            state = lazyListState,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.size(1.dp))
            }

            items(items = viewState.services, key = { it.id }) {
                ServiceRequestItem(
                    modifier = Modifier.fillMaxWidth(),
                    service = it,
                    onServiceClick = onServiceClick)
            }

            if (viewState.services.isEmpty() &&
                viewState.listState == ListState.PAGINATION_EXHAUST) {
                item(key = "empty") {
                    EmptyView(
                        title = "No services",
                        modifier = Modifier.fillParentMaxSize(),
                        subtitle = emptyMessage
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.size(1.dp))
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
fun Loading() {
    LoadingView()
}

@Composable
fun PaginationLoading() {
    LoadingView(
        modifier = Modifier
            .fillMaxWidth()
            .padding(15.dp)
    )
}

@Composable
fun ServiceRequestItem(
    modifier: Modifier = Modifier,
    service: Service,
    onServiceClick: (Service) -> Unit
) {
    val serviceType = remember { ServiceType.getByValue(service.type) }

    Box(
        modifier = modifier
            .shadow(4.dp, shape = MaterialTheme.shapes.medium)
            .zIndex(4.dp.value)
            .background(color = MaterialTheme.colorScheme.secondary)
            .clickable { onServiceClick(service) },
        contentAlignment = Alignment.Center
    ) {
        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.Top) {
            Image(
                painter = painterResource(id = serviceType.iconRes),
                modifier = Modifier.size(20.dp),
                contentDescription = null,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSecondary)
            )
            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Top,
                modifier = Modifier
                    .padding(start = 8.dp, end = 8.dp)
                    .weight(1f)
            ) {
                Text(
                    text = "${serviceType.uiString} service requested by ${service.requester.displayName}",
                    color = MaterialTheme.colorScheme.onSecondary,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Start,
                )
                Spacer(modifier = Modifier.size(8.dp))
                if (service.assignee?.displayName != null && service.isAcknowledged) {
                    Text(
                        text = "${if (service.status == ServiceStatus.COMPLETED) 
                            "Completed by" else "Assigned to:"} " + service.assignee.displayName,
                        color = MaterialTheme.colorScheme.onSecondary,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                }
                if (service.type == ServiceType.TRANSPORT.value) {
                    val transportText = if (service.sourceName != null && service.destinationName != null) {
                        "From ${service.sourceName} to ${service.destinationName}"
                    } else {
                        "Ride details missing."
                    }
                    Text(
                        text = transportText,
                        color = MaterialTheme.colorScheme.onSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                }
                Text(
                    text = "Raised by: ${service.requester.displayName}",
                    color = MaterialTheme.colorScheme.onSecondary,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}