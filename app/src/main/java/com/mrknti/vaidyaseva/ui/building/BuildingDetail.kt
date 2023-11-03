package com.mrknti.vaidyaseva.ui.building

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mrknti.vaidyaseva.R
import com.mrknti.vaidyaseva.data.building.BuildingData
import com.mrknti.vaidyaseva.data.building.HostelRoom
import com.mrknti.vaidyaseva.ui.components.LoadingView
import com.mrknti.vaidyaseva.util.DateFormat
import com.mrknti.vaidyaseva.util.differenceInHours
import com.mrknti.vaidyaseva.util.formatDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuildingDetail() {
    val viewModel: BuildingDetailViewModel = viewModel()
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    val localContext = LocalContext.current
    var showRoomSheet: HostelRoom? by remember { mutableStateOf(null) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
        ) {
            if (viewState.isLoading) {
                LoadingView(alignment = Alignment.TopCenter)
            } else if (viewState.errorMessage != null) {
                LaunchedEffect(key1 = viewState.errorMessage) {
                    Toast.makeText(localContext, viewState.errorMessage, Toast.LENGTH_SHORT).show()
                }
            } else if (viewState.buildingData != null) {
                BuildingDetailHeader(viewState.buildingData!!)
                Spacer(modifier = Modifier.size(16.dp))
                BuildingDetailContent(
                    rooms = viewState.buildingData?.rooms ?: emptyList(),
                    onRoomClick = {
                        showRoomSheet = it
                    })
            }
            if (showRoomSheet != null) {
                AssignRoom(
                    room = showRoomSheet!!,
                    onDismissRequest = { showRoomSheet = null },
                    sheetState = sheetState
                )
            }
        }
    }
}

@Composable
fun BuildingDetailHeader(buildingData: BuildingData) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row {
            Icon(
                painter = painterResource(id = R.drawable.apartment_24),
                contentDescription = null
            )
            Spacer(modifier = Modifier.size(12.dp))
            Text(text = buildingData.name ?: "Building", style = MaterialTheme.typography.titleSmall)
        }
        Spacer(modifier = Modifier.size(8.dp))
        val hasManager = buildingData.managerName != null
        val managerText =
            if (hasManager) "Managed by ${buildingData.managerName}" else "No manager assigned"
        Text(text = managerText)
        Spacer(modifier = Modifier.size(8.dp))
        Text(text = "Occupied rooms: ${buildingData.numOccupiedRooms}")
        Spacer(modifier = Modifier.size(4.dp))
        Text(text = "Unoccupied rooms: ${buildingData.freeRooms}")
    }
}

@Composable
fun BuildingDetailContent(rooms: List<HostelRoom>, onRoomClick: (HostelRoom) -> Unit) {
    Column {
        Text(
            text = "Rooms info",
            modifier = Modifier.padding(bottom = 16.dp),
            style = MaterialTheme.typography.titleSmall)

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(rooms, key = { it.id }) { room ->
                RoomItem(room = room, onRoomClick = onRoomClick)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomItem(room: HostelRoom, onRoomClick: (HostelRoom) -> Unit) {
    Card(
        onClick = { onRoomClick(room) },
        colors = CardDefaults.cardColors(containerColor = getRoomColor(room))
    ) {
        Column(
            modifier = Modifier.padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = room.name,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium
                    .copy(color = MaterialTheme.colorScheme.onPrimary)
            )
            Spacer(modifier = Modifier.size(4.dp))
            val occupancyMeta = if (room.isOccupied) {
                "checkout: ${room.occupancies.first().checkoutTime?.formatDate(DateFormat.DAY_MONTH)}"
            } else {
                if (room.occupancies.isEmpty()) {
                    "Free"
                } else {
                    "check in: ${room.occupancies.first().checkInTime?.formatDate(DateFormat.DAY_MONTH)}"
                }
            }
            Text(
                text = occupancyMeta,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelSmall
                    .copy(color = MaterialTheme.colorScheme.onPrimary)
            )
        }
    }
}

fun getRoomColor(room: HostelRoom): Color {
    return when {
        room.isOccupied -> {
            if ((room.occupancies.first().checkoutTime?.differenceInHours() ?: 0) > 24) {
                Color.Red
            } else {
                Color.Red.copy(alpha = 0.5f)
            }
        }
        else -> {
            if (room.occupancies.isEmpty()) {
                Color.Green
            } else {
                Color.Yellow
            }
        }
    }
}
