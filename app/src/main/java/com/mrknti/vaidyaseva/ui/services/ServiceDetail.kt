package com.mrknti.vaidyaseva.ui.services

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mrknti.vaidyaseva.data.ServiceStatus
import com.mrknti.vaidyaseva.data.ServiceType
import com.mrknti.vaidyaseva.ui.chats.ChatDetail
import com.mrknti.vaidyaseva.util.DateFormat
import com.mrknti.vaidyaseva.util.formatDate

@Composable
fun ServiceDetail(onBackPressed: () -> Unit) {
    val viewModel: ServiceDetailViewModel = viewModel()
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    val service = viewState.service

    Column(modifier = Modifier.fillMaxSize()) {
        VsTopAppBar(title = "Service Detail", onBackPressed = onBackPressed)

        Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 20.dp, top = 16.dp)) {
            Text(
                text = "${ServiceType.getByValue(service.type).uiString} service requested by ${service.requester.displayName}",
                style = MaterialTheme.typography.titleSmall
            )
            if (service.type == ServiceType.TRANSPORT.value) {
                val transportText = if (viewState.source != null && viewState.destination != null) {
                    "From ${viewState.source} to ${viewState.destination}"
                } else {
                    "Ride details missing."
                }
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = transportText,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = "Service Needed at ${service.serviceTime?.formatDate(DateFormat.HOUR_DAY_MONTH)}",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.size(16.dp))
            if (service.status == ServiceStatus.COMPLETED) {
                Row {
                    Text(
                        text = "Service Completed by ${service.assignee?.displayName}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "\u2022 ${service.completedAt?.formatDate(DateFormat.HOUR_DAY_MONTH)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            } else if (viewModel.canAcknowledgeService || viewModel.canCompleteService) {
                Button(
                    onClick = viewModel::onAcknowledgeCompleteClick,
                ) {
                    Text(text =
                    "${if (service.isAcknowledged) "Complete" else "Acknowledge"} Booking")
                }
            }
            if (service.assignee != null) {
                Spacer(modifier = Modifier.size(16.dp))
                Text(
                    text = "Assigned to: ${service.assignee.displayName}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.size(16.dp))
            Text(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp),
                text = "Service Chat",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.size(8.dp))
            HorizontalDivider(thickness = 2.dp)
            ChatDetail(threadId = viewState.service.threadId)
            Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.ime))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VsTopAppBar(title: String, onBackPressed: () -> Unit) {
    TopAppBar(
        title = {
            Row(
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = title,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackPressed) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Toggle Drawer",
                )
            }
        }
    )
    HorizontalDivider(thickness = 1.dp)
}