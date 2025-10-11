package com.jashvantsewmangal.voyager.ui.screens

import android.icu.text.DateFormat
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.Locale

@Composable
fun DatePickerFieldToModal(
    modifier: Modifier = Modifier,
    onDateSelected: (LocalDate) -> Unit // 🔹 callback for external handling
) {
    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }
    var showModal by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = selectedDateMillis?.let { convertMillisToDate(it) } ?: "",
        onValueChange = { },
        label = { Text("Select a date") },
        placeholder = { Text("Select a date") },
        trailingIcon = {
            Icon(Icons.Default.DateRange, contentDescription = "Select date icon")
        },
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(selectedDateMillis) {
                awaitEachGesture {
                    awaitFirstDown(pass = PointerEventPass.Initial)
                    val upEvent = waitForUpOrCancellation(pass = PointerEventPass.Initial)
                    if (upEvent != null) {
                        showModal = true
                    }
                }
            }
    )

    if (showModal) {
        DatePickerModal(
            onDateSelected = { millis ->
                millis?.let { nonNullMillis ->
                    selectedDateMillis = nonNullMillis

                    // Convert safely to LocalDate
                    val localDate = Instant.ofEpochMilli(nonNullMillis)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()

                    onDateSelected(localDate)
                }

                showModal = false
            },
            onDismiss = { showModal = false }
        )
    }
}

@Composable
fun DatePickerModal(
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onDateSelected(datePickerState.selectedDateMillis)
                onDismiss()
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

// Locale-aware date formatting
fun convertMillisToDate(millis: Long): String {
    val dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault())
    return dateFormat.format(Date(millis))
}