package com.mrknti.vaidyaseva.ui.services

import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mrknti.vaidyaseva.data.LOCALE_IN
import com.mrknti.vaidyaseva.ui.components.LoadingView
import com.mrknti.vaidyaseva.ui.components.TimePickerDialog
import com.mrknti.vaidyaseva.util.TODAY_START
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

@Composable
fun BookService(modifier: Modifier = Modifier) {
    val viewModel: BookServiceViewModel = viewModel()
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    val request = viewModel.serviceBooking
    val context = LocalContext.current

    if (viewState.isLoading) {
        LoadingView(alignment = Alignment.TopCenter)
    } else if (viewState.error.isNotEmpty()) {
        LaunchedEffect(key1 = viewState.error) {
            Toast.makeText(context, viewState.error, Toast.LENGTH_SHORT).show()
        }
    }

    Surface(modifier = modifier
        .fillMaxSize()
        .padding(top = 16.dp, bottom = 0.dp, start = 20.dp, end = 20.dp) ) {
        Column(modifier = modifier.fillMaxSize()) {
            Text(
                text = "Book ${request.title} Service",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
            )
            Spacer(modifier = Modifier.size(8.dp))
            BookingTime(viewModel::onDateChange)
            Spacer(modifier = Modifier.size(8.dp))
            Box(modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()) {

            }
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
            Button(
                onClick = viewModel::bookService,
                modifier = Modifier.fillMaxWidth(),
                enabled = viewState.serviceDate!= null && !viewState.isLoading
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
    val datePickerState = rememberDatePickerState()
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
            DatePicker(state = datePickerState, dateValidator = { it >= TODAY_START })
        }
    }

    Row {
        Button( onClick = { showDatePicker = true }) {
            Image(
                imageVector = Icons.Rounded.DateRange,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.size(4.dp))
            Text(
                text = dateText,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
        }
        Spacer(modifier = Modifier.size(8.dp))
        Button( onClick = { showTimePicker = true }) {
            Image(
                imageVector = Icons.Rounded.DateRange,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.size(4.dp))
            Text(
                text = timeText,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
        }
    }
}
