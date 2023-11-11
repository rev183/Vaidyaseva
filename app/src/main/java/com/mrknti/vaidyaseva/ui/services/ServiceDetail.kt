package com.mrknti.vaidyaseva.ui.services

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mrknti.vaidyaseva.data.ServiceStatus
import com.mrknti.vaidyaseva.ui.chats.ChatDetail
import com.mrknti.vaidyaseva.util.DateFormat
import com.mrknti.vaidyaseva.util.formatDate

@Composable
fun ServiceDetail() {
    val viewModel: ServiceDetailViewModel = viewModel()
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    val service = viewState.service

    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "${service.type} service requested by ${service.requester.displayName}",
                style = MaterialTheme.typography.titleSmall
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
        }
        Spacer(modifier = Modifier.size(16.dp))
        Text(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp),
            text = "Service Chat",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.size(8.dp))
        Divider(thickness = 2.dp)
        ChatDetail(threadId = viewState.service.threadId)
        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.ime))
    }
}