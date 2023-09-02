@file:OptIn(ExperimentalMaterial3Api::class)

package com.mrknti.vaidyaseva.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.mrknti.vaidyaseva.data.ServiceType

@Composable
fun Home(modifier: Modifier = Modifier) {
    HomeContent(modifier = modifier)
}

@Composable
fun HomeContent(modifier: Modifier) {
    Surface(modifier = modifier.fillMaxSize()) {
        ServicesToBook()
    }
}

@Composable
fun ServicesToBook() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "Book services", modifier = Modifier.padding(bottom = 16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(HOME_SERVICES) { service ->
                ServiceItem(service = service)
            }
        }
    }
}

@Composable
fun ServiceItem(service: Service, modifier: Modifier = Modifier, onServiceClick: (Service) -> Unit = { }) {
    Box(
        modifier = modifier
            .shadow(4.dp, shape = MaterialTheme.shapes.medium)
            .zIndex(4.dp.value)
            .background(color = MaterialTheme.colorScheme.surfaceTint)
            .clickable { onServiceClick(service) },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(8.dp)
        ) {
            Image(
                imageVector = service.icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )
            Text(
                text = service.title,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

data class Service(
    val icon: ImageVector,
    val title: String,
    val type: String
)

val HOME_SERVICES = listOf(
    Service(Icons.Outlined.LocationOn, "Cab", ServiceType.CAB),
    Service(Icons.Outlined.Person, "Cleaning", ServiceType.CLEANING),
    Service(Icons.Outlined.Build, "Plumbing", ServiceType.PLUMBING),
    Service(Icons.Outlined.ShoppingCart, "Medicine", ServiceType.MEDICINE),
    Service(Icons.Outlined.Phone, "Room Service", ServiceType.ROOM_SERVICE),
    Service(Icons.Outlined.Add, "General", ServiceType.NORMAL),
)