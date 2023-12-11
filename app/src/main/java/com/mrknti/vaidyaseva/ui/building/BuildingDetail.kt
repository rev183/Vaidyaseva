package com.mrknti.vaidyaseva.ui.building

import android.widget.Toast
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.mrknti.vaidyaseva.R
import com.mrknti.vaidyaseva.data.building.BuildingData
import com.mrknti.vaidyaseva.data.building.HostelRoom
import com.mrknti.vaidyaseva.data.user.User
import com.mrknti.vaidyaseva.ui.components.LoadingView
import com.mrknti.vaidyaseva.util.DateFormat
import com.mrknti.vaidyaseva.util.differenceInHours
import com.mrknti.vaidyaseva.util.formatDate
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuildingDetail(navigateToFullScreenImage: (String) -> Unit) {
    val viewModel: BuildingDetailViewModel = viewModel()
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    val localContext = LocalContext.current
    var showRoomSheet: HostelRoom? by remember { mutableStateOf(null) }
    val sheetState = rememberModalBottomSheetState()

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
                BuildingDetailHeader(viewState.buildingData!!, viewState.selfUser)
                Spacer(modifier = Modifier.size(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.size(16.dp))
                if (viewModel.isAdmin()) {
                    BuildingDetailContent(
                        rooms = viewState.rooms,
                        onRoomClick = {
                            showRoomSheet = it
                        })
                } else {
                    BuildingGallery(
                        picUrls = viewState.buildingData!!.getGalleryUrls(),
                        onImageClick = {
                            val urlE = URLEncoder.encode(it, "utf-8")
                            navigateToFullScreenImage(urlE)
                        })
                }
            }
        }
        if (showRoomSheet != null) {
            AssignRoom(
                room = showRoomSheet!!,
                buildingId = viewModel.buildingId,
                onDismissRequest = { showRoomSheet = null },
                sheetState = sheetState
            )
        }
        if (viewModel.isAdmin()) {
            Box(modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    COLOR_LEGEND.forEach { (color, text) ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(color, shape = MaterialTheme.shapes.small)
                            )
                            Spacer(modifier = Modifier.size(4.dp))
                            Text(text = text, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
     }
}

@Composable
fun BuildingDetailHeader(buildingData: BuildingData, selfUser: User?) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row {
                Icon(
                    painter = painterResource(id = R.drawable.apartment_24),
                    contentDescription = null
                )
                Spacer(modifier = Modifier.size(12.dp))
                Text(text = buildingData.name ?: "Building", style = MaterialTheme.typography.titleSmall)
            }
            if (selfUser != null && selfUser.canProxyBook()) {
                Spacer(modifier = Modifier.size(8.dp))
                Text(text = "Occupied: ${buildingData.numOccupiedRooms}", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.size(4.dp))
                Text(text = "Free: ${buildingData.freeRooms}", style = MaterialTheme.typography.labelMedium)
            }
            Spacer(modifier = Modifier.size(8.dp))
            val hasManager = buildingData.manager != null
            val managerText = if (hasManager) "Manager - ${buildingData.manager!!.displayName}"
            else "No manager assigned"
            Text(text = managerText, style = MaterialTheme.typography.bodySmall)
        }
        Spacer(modifier = Modifier.weight(1f))
        if (buildingData.getGalleryUrls().isNotEmpty()) {
            AsyncImage(
                model = buildingData.getGalleryUrls().firstOrNull(),
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .align(Alignment.CenterVertically),
                contentScale = ContentScale.Crop
            )
        }
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

@Composable
fun BuildingGallery(picUrls: List<String>, onImageClick: (String) -> Unit) {
    Column {
        Text(
            text = "Gallery",
            modifier = Modifier.padding(bottom = 16.dp),
            style = MaterialTheme.typography.titleSmall)

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(picUrls, key = { it }) { url ->
                val data = ImageRequest.Builder(LocalContext.current)
                    .data(url)
                    .build()
                AsyncImage(
                    model = data,
                    contentDescription = null,
                    modifier = Modifier
                        .clickable { onImageClick(url) }
                        .size(width = 110.dp, height = 150.dp)
                        .clip(MaterialTheme.shapes.medium),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

fun getRoomColor(room: HostelRoom): Color {
    return when {
        room.isOccupied -> {
            if ((room.occupancies.firstOrNull()?.checkoutTime?.differenceInHours() ?: 0) > 24) {
                Color.Red
            } else {
                Color.Red.copy(alpha = 0.5f)
            }
        }
        else -> {
            if (room.occupancies.isEmpty()) {
                Color.Green
            } else {
                Color.Blue
            }
        }
    }
}

val COLOR_LEGEND = mapOf(
    Color.Green to "Vacant",
    Color.Blue to "Has future bookings",
    Color.Red to "Checkout after 24 hours",
    Color.Red.copy(alpha = 0.5f) to "Checkout with in 24 hours"
)
