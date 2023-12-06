@file:OptIn(ExperimentalMaterial3Api::class)

package com.mrknti.vaidyaseva.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mrknti.vaidyaseva.data.ServiceType
import com.mrknti.vaidyaseva.data.building.BuildingData
import com.mrknti.vaidyaseva.data.user.User
import com.mrknti.vaidyaseva.ui.building.BuildingDetailHeader
import kotlin.math.ceil

@Composable
fun Home(
    modifier: Modifier = Modifier,
    navigateToBooking: (ServiceType) -> Unit,
    navigateToBuildingDetail: (BuildingData) -> Unit
) {
    val viewModel: HomeViewModel = viewModel()
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val pullState = rememberPullToRefreshState()

    LaunchedEffect(key1 = pullState.isRefreshing) {
        if (pullState.isRefreshing) {
            viewModel.getBuildingsData()
        }
    }

    LaunchedEffect(key1 = viewState.isLoading) {
        if (viewState.isLoading) {
            pullState.startRefresh()
        } else {
            pullState.endRefresh()
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
            state = listState,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(text = "Book services", modifier = Modifier.padding(bottom = 16.dp))
                LazyVerticalGrid(
                    modifier = Modifier.heightIn(max = getGridHeight(HOME_SERVICE.size)),
                    columns = GridCells.Fixed(3),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(HOME_SERVICE) { serviceType ->
                        ServiceRequestItem(serviceType = serviceType, navigateToBooking = navigateToBooking)
                    }
                }
            }

            item {
                Text(text = "Building info", modifier = Modifier.padding(bottom = 4.dp, top = 8.dp))
            }
            items(viewState.buildings, key = { it.id }) { building ->
                BuildingCard(buildingData = building, viewState.selfUser, navigateToBuildingDetail = navigateToBuildingDetail)
            }
        }
        PullToRefreshContainer(
            modifier = Modifier.align(Alignment.TopCenter),
            state = pullState,
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    }

}

private fun getGridHeight(numItems: Int): Dp {
    val itemsPerRow = 3
    val numRows: Int = ceil(numItems / itemsPerRow.toDouble()).toInt()
    // image height + padding + image-text spacing + text height
    val itemHeight = 32 + (12 * 2) + 4 + (18 * 2)
    val spacing = 16
    return ((itemHeight * numRows) + (spacing * (numRows - 1))).dp
}

@Composable
fun ServiceRequestItem(
    serviceType: ServiceType,
    modifier: Modifier = Modifier,
    navigateToBooking: (ServiceType) -> Unit
) {
    Box(
        modifier = modifier
            .shadow(4.dp, shape = MaterialTheme.shapes.medium)
            .zIndex(4.dp.value)
            .background(color = MaterialTheme.colorScheme.tertiaryContainer)
            .clickable { navigateToBooking(serviceType) },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(8.dp)
        ) {
            Image(
                painter = painterResource(id = serviceType.iconRes),
                modifier = Modifier.size(32.dp),
                contentDescription = null,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onTertiaryContainer)
            )
            Text(
                text = serviceType.uiString,
                modifier = Modifier.padding(top = 4.dp),
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun BuildingCard(buildingData: BuildingData, selfUser: User?, navigateToBuildingDetail: (BuildingData) -> Unit) {
    Card(onClick = { navigateToBuildingDetail(buildingData) }, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            BuildingDetailHeader(buildingData, selfUser)
        }
    }
}

val HOME_SERVICE = listOf(
    ServiceType.TRANSPORT,
    ServiceType.HOUSE_KEEPING,
    ServiceType.VISA_RENEWAL,
    ServiceType.NORMAL,
)