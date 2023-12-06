package com.mrknti.vaidyaseva.ui.building

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mrknti.vaidyaseva.R
import com.mrknti.vaidyaseva.data.OccupancyStatus
import com.mrknti.vaidyaseva.data.building.HostelRoom
import com.mrknti.vaidyaseva.data.building.RoomOccupancy
import com.mrknti.vaidyaseva.ui.components.ButtonSmall
import com.mrknti.vaidyaseva.ui.search.UserSearchBar
import com.mrknti.vaidyaseva.ui.search.UserSearchViewAction
import com.mrknti.vaidyaseva.ui.search.UserSearchViewModel
import com.mrknti.vaidyaseva.util.DateFormat
import com.mrknti.vaidyaseva.util.TODAY_START
import com.mrknti.vaidyaseva.util.formatDate
import java.util.Calendar
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignRoom(room: HostelRoom, onDismissRequest: () -> Unit, sheetState: SheetState) {
    var roomState by remember { mutableStateOf(room) }
    var checkIn: Date? by remember { mutableStateOf(null) }
    var checkOut: Date? by remember { mutableStateOf(null) }
    val searchViewModel: UserSearchViewModel = viewModel()
    val searchQuery by searchViewModel.searchQuery.collectAsStateWithLifecycle(initialValue = "")
    val searchViewState by searchViewModel.state.collectAsStateWithLifecycle()
    val viewActions by searchViewModel.action.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(key1 = viewActions) {
        if (viewActions == UserSearchViewAction.RoomBooked) {
            Toast.makeText(context, "Room booked successfully", Toast.LENGTH_LONG)
                .show()
            searchViewModel.clearData()
            onDismissRequest()
        } else if (viewActions is UserSearchViewAction.OccupancyCheckedOut) {
            Toast.makeText(context, "Checked out successful", Toast.LENGTH_LONG)
                .show()
            val occupancies = roomState.occupancies.toMutableList()
                .filter { it.id != (viewActions as UserSearchViewAction.OccupancyCheckedOut).occupancyId  }
            roomState = roomState.copy(occupancies = occupancies)
        }
    }

    LaunchedEffect(key1 = searchViewState.errorMessage) {
        if (searchViewState.errorMessage.isNotEmpty()) {
            Toast.makeText(context, searchViewState.errorMessage, Toast.LENGTH_LONG).show()
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            searchViewModel.clearData()
            onDismissRequest()
        },
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .padding(start = 20.dp, end = 20.dp, bottom = 45.dp)
        ) {
            Text(
                text = "Room - ${roomState.name}",
                modifier = Modifier
                    .fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.size(8.dp))
            val latestOccupancy = roomState.occupancies.firstOrNull()
            val occupancyText = if (latestOccupancy == null) {
                "Current occupancy - Vacant"
            } else {
                "Current occupancy"
            }
            Text(text = occupancyText, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.size(12.dp))
            Column {
                for (occupancy in roomState.occupancies) {
                    UserOccupancy(occupancy) {
                        searchViewModel.checkOutOccupancy(it, roomState.id)
                    }
                }
            }
            Spacer(modifier = Modifier.size(16.dp))
            HorizontalDivider()
            if (latestOccupancy == null || roomState.occupancies.size < 2) {
                if (searchViewState.selectedUser == null) {
                    UserSearchBar(
                        searchQuery,
                        searchViewModel::onSearchQueryChange,
                        searchViewModel::onClearSearch,
                        searchViewModel::setSelectedUser,
                        searchViewState.searchResults,
                        modifier = Modifier.fillMaxWidth(),
                        windowInsets = WindowInsets(top = 8.dp)
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Book for - ${searchViewState.selectedUser?.displayName}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.size(4.dp))
                        IconButton(onClick = searchViewModel::clearSelectedUser) {
                            Icon(
                                painter = painterResource(id = R.drawable.close_24),
                                contentDescription = "Clear icon"
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.size(12.dp))
                BookingTime(onDateChange = { date, bookingDateType ->
                    if (bookingDateType == BookingDateType.CHECK_IN) {
                        checkIn = date
                    } else {
                        checkOut = date
                    }
                })
                Spacer(modifier = Modifier.size(12.dp))
                Button(
                    onClick = {
                        if (checkIn != null && checkOut != null) {
                            searchViewModel.bookRoom(
                                roomState,
                                checkIn!!,
                                checkOut!!
                            )
                        }
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    enabled = checkIn != null && checkOut != null
                            && searchViewState.selectedUser != null
                ) {
                    Text(text = "Book Room")
                }
            } else {
                Spacer(modifier = Modifier.size(4.dp))
                val timingText = if (latestOccupancy.checkoutTime != null) "Check-out time - ${
                    latestOccupancy.checkoutTime.formatDate(DateFormat.DAY_MONTH)
                }"
                else "Check-in time - ${latestOccupancy.checkInTime?.formatDate(DateFormat.DAY_MONTH)}"
                Text(text = timingText, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingTime(onDateChange: (Date, BookingDateType) -> Unit) {
    var showDatePicker by remember { mutableStateOf(BookingDateType.NONE) }
    val datePickerState = rememberDatePickerState(selectableDates = object : SelectableDates {
        override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis >= TODAY_START
    })
    val calendar = Calendar.getInstance()
    var checkInText by remember { mutableStateOf("Check In") }
    var checkOutText by remember { mutableStateOf("Check Out") }

    if (showDatePicker != BookingDateType.NONE) {
        val confirmEnabled =
            remember { derivedStateOf { datePickerState.selectedDateMillis != null } }
        DatePickerDialog(
            onDismissRequest = {
                showDatePicker = BookingDateType.NONE
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        calendar.timeInMillis = datePickerState.selectedDateMillis!!
                        calendar.isLenient = false
                        onDateChange(calendar.time, showDatePicker)
                        if (showDatePicker == BookingDateType.CHECK_IN) {
                            checkInText = calendar.time.formatDate(DateFormat.DAY_MONTH_YEAR)
                        } else {
                            checkOutText = calendar.time.formatDate(DateFormat.DAY_MONTH_YEAR)
                        }
                        showDatePicker = BookingDateType.NONE
                    },
                    enabled = confirmEnabled.value
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDatePicker = BookingDateType.NONE
                    }
                ) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ButtonSmall(onClick = { showDatePicker = BookingDateType.CHECK_IN }) {
            Image(
                imageVector = Icons.Rounded.DateRange,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimary)
            )
            Spacer(modifier = Modifier.size(4.dp))
            Text(
                text = checkInText,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
            )
        }
        Spacer(modifier = Modifier.size(12.dp))
        Text(text = "to", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.size(12.dp))
        ButtonSmall(onClick = { showDatePicker = BookingDateType.CHECK_OUT }) {
            Image(
                imageVector = Icons.Rounded.DateRange,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimary)
            )
            Spacer(modifier = Modifier.size(4.dp))
            Text(
                text = checkOutText,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
fun UserOccupancy(occupancy: RoomOccupancy, onCheckOutClick: (Int) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp, bottom = 6.dp)
            .background(
                color = MaterialTheme.colorScheme.tertiaryContainer,
                shape = MaterialTheme.shapes.medium
            )
    ) {
        Column(
            modifier = Modifier
                .padding(top = 8.dp, start = 12.dp, end = 12.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = occupancy.occupant?.displayName ?: "Unknown",
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Spacer(modifier = Modifier.size(4.dp))
            Row {
                Text(
                    text = "Check-in - ${occupancy.checkInTime?.formatDate(DateFormat.DAY_MONTH)}",
                    style = MaterialTheme.typography.labelSmall
                        .copy(color = MaterialTheme.colorScheme.onTertiaryContainer)
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "Check-out - ${occupancy.checkoutTime?.formatDate(DateFormat.DAY_MONTH)}",
                    style = MaterialTheme.typography.labelSmall
                        .copy(color = MaterialTheme.colorScheme.onTertiaryContainer)
                )
            }
            val status = OccupancyStatus.getByValue(occupancy.status)
            TextButton(
                onClick = { onCheckOutClick(occupancy.id) },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(
                    text = if (status == OccupancyStatus.CHECK_IN) "Check-out" else "Cancel",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

enum class BookingDateType {
    CHECK_IN, CHECK_OUT, NONE
}