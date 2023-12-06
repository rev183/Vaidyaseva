package com.mrknti.vaidyaseva.ui.services

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mrknti.vaidyaseva.R
import com.mrknti.vaidyaseva.data.LOCALE_IN
import com.mrknti.vaidyaseva.data.ServiceType
import com.mrknti.vaidyaseva.data.building.BuildingData
import com.mrknti.vaidyaseva.data.user.User
import com.mrknti.vaidyaseva.ui.components.ButtonSmall
import com.mrknti.vaidyaseva.ui.components.LoadingView
import com.mrknti.vaidyaseva.ui.components.TimePickerDialog
import com.mrknti.vaidyaseva.util.TODAY_START
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

@Composable
fun BookService(
    modifier: Modifier = Modifier,
    onFinishClick: () -> Unit,
    onGotoSearch: () -> Unit,
    requesterFlow: StateFlow<User?>?
) {
    val viewModel: BookServiceViewModel = viewModel()
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    val action by viewModel.action.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(key1 = Unit) {
        scope.launch {
            requesterFlow?.distinctUntilChanged { old, new ->
                old?.id == new?.id
            }?.collect {
                viewModel.setRequestUser(it)
            }
        }
    }

    if (viewState.isLoading) {
        LoadingView(alignment = Alignment.TopCenter)
    } else if (viewState.error.isNotEmpty()) {
        LaunchedEffect(key1 = viewState.error) {
            Toast.makeText(context, viewState.error, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(key1 = action) {
        if (action == ServiceBookingAction.BookingComplete) {
            Toast.makeText(
                context,
                "Your service has been booked. Our personnel will contact you",
                Toast.LENGTH_SHORT
            ).show()
            onFinishClick()
        }
    }

    Surface(modifier = modifier
        .fillMaxSize()
        .padding(top = 16.dp, bottom = 0.dp, start = 20.dp, end = 20.dp) ) {
        Column(modifier = modifier.fillMaxSize()) {
            Text(
                text = "Book ${viewState.serviceType.uiString} Service",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
            )
            Spacer(modifier = Modifier.size(8.dp))
            Box(modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = MaterialTheme.shapes.medium
                )) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (viewState.selfUser?.canProxyBook() == true) {
                        AddRequester(viewState.requestUser, onGotoSearch)
                        Spacer(modifier = Modifier.size(8.dp))
                    }
                    when (viewState.serviceType) {
                        ServiceType.TRANSPORT -> {
                            TransportMeta(viewState.requesterBuilding, viewModel::setDestinationType)
                        }
                        else -> {}
                    }
                }
            }
            if (viewModel.serviceMissingRoom()) {
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = "Need an room booked to request this service",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Red)
                )
            }
            Spacer(modifier = Modifier.size(8.dp))
            BookingTime(viewModel::onDateChange)
            Spacer(modifier = Modifier.size(8.dp))
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = viewState.message,
                onValueChange = viewModel::onMessageChange,
                placeholder = {
                    Text(
                        text = "Add custom instructions",
                        color = MaterialTheme.colorScheme.secondary
                    )
                },
                minLines = 3
            )
            Spacer(modifier = Modifier.size(16.dp))
            val needDestination =
                viewState.serviceType != ServiceType.TRANSPORT || viewState.destinationType != null
            Button(
                onClick = viewModel::bookService,
                modifier = Modifier.fillMaxWidth(),
                enabled = !viewState.isLoading && needDestination && !viewModel.serviceMissingRoom()
            ) {
                Text(text = "Confirm Booking")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingTime(onDateChange: (Date) -> Unit) {
    var showTimePicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState()
    val datePickerState = rememberDatePickerState(selectableDates = object : SelectableDates {
        override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis >= TODAY_START
    })
    val calendar: Calendar by remember { mutableStateOf(Calendar.getInstance(LOCALE_IN)) }
    val timeFormatter = remember { SimpleDateFormat("hh:mm a", LOCALE_IN) }
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yy", LOCALE_IN) }
    val timeText by remember(calendar.time) { mutableStateOf(timeFormatter.format(calendar.time)) }
    val dateText by remember(calendar.time) { mutableStateOf(dateFormatter.format(calendar.time)) }

    if (showTimePicker) {
        TimePickerDialog(
            onCancel = { showTimePicker = false },
            onConfirm = {
                calendar.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                calendar.set(Calendar.MINUTE, timePickerState.minute)
                calendar.isLenient = false
                showTimePicker = false
                onDateChange(calendar.time)
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }

    if (showDatePicker) {
        val confirmEnabled = remember { derivedStateOf { datePickerState.selectedDateMillis != null } }
        DatePickerDialog(
            onDismissRequest = {
                showDatePicker = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val dateCal = Calendar.getInstance(LOCALE_IN)
                        dateCal.timeInMillis = datePickerState.selectedDateMillis!!
                        calendar.set(Calendar.MONTH, dateCal.get(Calendar.MONTH))
                        calendar.set(Calendar.DAY_OF_MONTH, dateCal.get(Calendar.DAY_OF_MONTH))
                        calendar.set(Calendar.YEAR, dateCal.get(Calendar.YEAR))
                        calendar.isLenient = false
                        onDateChange(calendar.time)
                        showDatePicker = false
                    },
                    enabled = confirmEnabled.value
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDatePicker = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Row {
        ButtonSmall(onClick = { showDatePicker = true }) {
            Image(
                imageVector = Icons.Rounded.DateRange,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimary)
            )
            Spacer(modifier = Modifier.size(4.dp))
            Text(
                text = dateText,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
            )
        }
        Spacer(modifier = Modifier.size(8.dp))
        ButtonSmall(onClick = { showTimePicker = true }) {
            Image(
                painter = painterResource(id = R.drawable.timer_24),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimary)
            )
            Spacer(modifier = Modifier.size(4.dp))
            Text(
                text = timeText,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
fun TransportMeta(
    selfBuilding: BuildingData?,
    selectDestination: (destination: DestinationType) -> Unit
) {
    var destinationType: DestinationType? by remember { mutableStateOf(null) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Destination", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.size(12.dp))
        Row {
            OutlinedButton(
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (destinationType == DestinationType.ROOM)
                        MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onTertiaryContainer),
                onClick = {
                    destinationType = DestinationType.ROOM
                    selectDestination(DestinationType.ROOM)
                }
            ) {
                Column {
                    Text(text = "Room")
                    if (selfBuilding?.name != null) {
                        Text(text = selfBuilding.name, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            Spacer(modifier = Modifier.size(8.dp))
            OutlinedButton(
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (destinationType == DestinationType.HOSPITAL)
                        MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onTertiaryContainer),
                onClick = {
                    destinationType = DestinationType.HOSPITAL
                    selectDestination(DestinationType.HOSPITAL)
                }
            ) {
                Text(text = "Hospital")
            }
        }
        if (destinationType != null) {
            Spacer(modifier = Modifier.size(6.dp))
            Text(
                text = if (destinationType == DestinationType.HOSPITAL)
                    "From Room to Hospital" else "From Hospital to Room",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRequester(requester: User?, gotoSearch: () -> Unit) {
    Column(modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 8.dp)) {
        if (requester == null) {
            TextButton(onClick = { gotoSearch() }) {
                Text(text = "Book for others", style = MaterialTheme.typography.titleMedium)
            }
        } else {
            Card(
                onClick = { gotoSearch() },
                colors = CardDefaults.cardColors(containerColor =
                MaterialTheme.colorScheme.primaryContainer)
            ) {
                Text(
                    text = "Booking for ${requester.displayName}",
                    modifier = Modifier.padding(8.dp),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}