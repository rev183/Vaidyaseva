@file:OptIn(ExperimentalMaterial3Api::class)

package com.mrknti.vaidyaseva.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mrknti.vaidyaseva.data.ServiceType
import com.mrknti.vaidyaseva.data.building.BuildingData
import com.mrknti.vaidyaseva.ui.building.BuildingDetailHeader

@Composable
fun Home(
    modifier: Modifier = Modifier,
    navigateToBooking: (ServiceRequest) -> Unit,
    navigateToBuildingDetail: (BuildingData) -> Unit
) {
    val viewModel: HomeViewModel = viewModel()
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        viewModel.getBuildingsData()
    }

    Column(modifier = modifier
        .fillMaxSize()
        .scrollable(scrollState, Orientation.Vertical)) {
        ServicesToBook(navigateToBooking = navigateToBooking)
        Spacer(modifier = Modifier.size(16.dp))
        BuildingInfo(
            buildings = viewState.buildings,
            navigateToBuildingDetail = navigateToBuildingDetail
        )
    }
}

@Composable
fun ServicesToBook(navigateToBooking: (ServiceRequest) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(text = "Book services", modifier = Modifier.padding(bottom = 16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(HOME_SERVICE.values.toList()) { service ->
                ServiceRequestItem(serviceRequest = service, navigateToBooking = navigateToBooking)
            }
        }
    }
}

@Composable
fun ServiceRequestItem(
    serviceRequest: ServiceRequest,
    modifier: Modifier = Modifier,
    navigateToBooking: (ServiceRequest) -> Unit
) {
    Box(
        modifier = modifier
            .shadow(4.dp, shape = MaterialTheme.shapes.medium)
            .zIndex(4.dp.value)
            .background(color = MaterialTheme.colorScheme.surfaceTint)
            .clickable { navigateToBooking(serviceRequest) },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(8.dp)
        ) {
            Image(
                imageVector = serviceRequest.icon,
                modifier = Modifier.size(40.dp),
                contentDescription = null,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimary)
            )
            Text(
                text = serviceRequest.title,
                modifier = Modifier.padding(top = 8.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun BuildingInfo(buildings: List<BuildingData>, navigateToBuildingDetail: (BuildingData) -> Unit) {
    val listState = rememberLazyListState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(text = "Building info", modifier = Modifier.padding(bottom = 16.dp))
        LazyColumn(state = listState, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(buildings, key = { it.id }) { building ->
                BuildingCard(buildingData = building, navigateToBuildingDetail = navigateToBuildingDetail)
            }
        }
    }
}

@Composable
fun BuildingCard(buildingData: BuildingData, navigateToBuildingDetail: (BuildingData) -> Unit) {
    Card(onClick = { navigateToBuildingDetail(buildingData) }, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            BuildingDetailHeader(buildingData)
        }
    }
}

data class ServiceRequest(
    val icon: ImageVector,
    val title: String,
    val type: String
)

val HOME_SERVICE = mapOf(
    ServiceType.CAB to ServiceRequest(Icons.Outlined.LocationOn, "Cab", ServiceType.CAB),
    ServiceType.CLEANING to ServiceRequest(Icons.Outlined.Person, "Cleaning", ServiceType.CLEANING),
    ServiceType.PLUMBING to ServiceRequest(Icons.Outlined.Build, "Plumbing", ServiceType.PLUMBING),
    ServiceType.MEDICINE to ServiceRequest(
        Icons.Outlined.ShoppingCart,
        "Medicine",
        ServiceType.MEDICINE
    ),
    ServiceType.ROOM_SERVICE to ServiceRequest(
        Icons.Outlined.Phone,
        "Room Service",
        ServiceType.ROOM_SERVICE
    ),
    ServiceType.NORMAL to ServiceRequest(Icons.Outlined.Add, "General", ServiceType.NORMAL),
)