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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mrknti.vaidyaseva.data.ServiceStatus
import com.mrknti.vaidyaseva.data.ServiceType
import com.mrknti.vaidyaseva.data.userService.Service
import com.mrknti.vaidyaseva.ui.components.LoadingView

@Composable
fun Services(
    modifier: Modifier = Modifier,
    onServiceClick: (Service) -> Unit,
    serviceType: String
) {
    val viewModel: ServicesViewModel =
        viewModel(key = serviceType, factory = ServicesViewModelFactory(serviceType))
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    val lazyListState = rememberLazyListState()
    val localContext = LocalContext.current
    val shouldStartPaginate = remember {
        derivedStateOf {
            viewModel.canPaginate && (lazyListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
                ?: -1) >= (lazyListState.layoutInfo.totalItemsCount - 3)
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

    Surface(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 16.dp, start = 16.dp, end = 16.dp)
    ) {
        LazyColumn(state = lazyListState, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(items = viewState.services, key = { it.id }) {
                ServiceRequestItem(
                    modifier = Modifier.fillMaxWidth(),
                    service = it,
                    onServiceClick = onServiceClick)
            }

            item(
                key = viewState.listState,
            ) {
                when (viewState.listState) {
                    ListState.LOADING -> {
                        Loading()
                    }

                    ListState.PAGINATING -> {
                        PaginationLoading()
                    }

                    ListState.PAGINATION_EXHAUST -> {
                        Text(
                            text = "-------\u2B24-------",
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
                imageVector = getIconForServiceType(service.type),
                modifier = Modifier.size(24.dp),
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
                    text = "${service.type} service requested by ${service.requester.displayName}",
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
                Text(
                    text = "Raised by: ${service.requester.displayName}",
                    color = MaterialTheme.colorScheme.onSecondary,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

private fun getIconForServiceType(type: String): ImageVector {
    return when (type) {
        ServiceType.CAB -> Icons.Outlined.LocationOn
        ServiceType.CLEANING -> Icons.Outlined.Person
        ServiceType.PLUMBING -> Icons.Outlined.Build
        ServiceType.MEDICINE -> Icons.Outlined.ShoppingCart
        ServiceType.ROOM_SERVICE -> Icons.Outlined.Phone
        ServiceType.NORMAL -> Icons.Outlined.Add
        else -> Icons.Outlined.Warning
    }
}