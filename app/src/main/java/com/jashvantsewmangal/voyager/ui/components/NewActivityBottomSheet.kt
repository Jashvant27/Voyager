package com.jashvantsewmangal.voyager.ui.components

import android.app.TimePickerDialog
import android.content.Context
import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jashvantsewmangal.voyager.enums.WhenEnum
import com.jashvantsewmangal.voyager.models.DayActivity
import com.jashvantsewmangal.voyager.models.NoDateActivity
import com.jashvantsewmangal.voyager.ui.theme.VoyagerTheme
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewActivityBottomSheet(
    activity: NoDateActivity?,
    activityIndex: Int?,
    onDismissRequest: () -> Unit,
    saveAction: ((
        location: String?,
        whenType: WhenEnum,
        specific: LocalTime?,
        what: String
    ) -> Unit)?,
    editAction: ((
        activity: NoDateActivity,
        activityIndex: Int
    ) -> Unit)?
) {
    ModalBottomSheet(onDismissRequest = onDismissRequest) {
        val context = LocalContext.current
        val scrollState = rememberScrollState()

        // State
        var what by remember { mutableStateOf(TextFieldValue(activity?.what.orEmpty())) }
        var where by remember { mutableStateOf(TextFieldValue(activity?.location.orEmpty())) }
        var whenType by remember { mutableStateOf(activity?.whenType ?: WhenEnum.MORNING) }
        var specificWhen by remember { mutableStateOf(activity?.specific) }
        var showError by remember { mutableStateOf(false) }

        val openTimePicker = rememberTimePickerHandler(
            context = context,
            onTimeSelected = { time ->
                specificWhen = time
                whenType = WhenEnum.CUSTOM
            },
            onCancel = {
                specificWhen = null
                whenType = WhenEnum.MORNING
            },
            current = specificWhen
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            WhatTextField(what, showError) {
                what = it
                showError = false
            }

            WhereTextField(where) { where = it }

            WhenSection(
                whenType = whenType,
                specificWhen = specificWhen,
                onWhenChange = { type ->
                    if (type == WhenEnum.CUSTOM) openTimePicker()
                    else {
                        whenType = type
                        specificWhen = null
                    }
                }
            )

            HorizontalDivider()

            ActionButtons(
                onCancel = onDismissRequest,
                onSave = {
                    if (what.text.isBlank()) {
                        showError = true
                        return@ActionButtons
                    }

                    val whereValue = where.text.ifBlank { null }

                    if (activity != null && activityIndex != null) {
                        val dayActivity = NoDateActivity(
                            location = whereValue,
                            whenType = whenType,
                            specific = specificWhen,
                            what = what.text
                        )

                        editAction?.invoke(dayActivity, activityIndex)
                    }
                    else {
                        saveAction?.invoke(
                            whereValue,
                            whenType,
                            specificWhen,
                            what.text
                        )
                    }
                    onDismissRequest()
                }
            )
        }
    }
}

@Composable
private fun WhatTextField(
    value: TextFieldValue,
    showError: Boolean,
    onChange: (TextFieldValue) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text("What") },
        modifier = Modifier.fillMaxWidth(),
        isError = showError,
        supportingText = {
            if (showError) Text("This field is required")
        }
    )
}

@Composable
private fun WhereTextField(
    value: TextFieldValue,
    onChange: (TextFieldValue) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text("Where") },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun WhenSection(
    whenType: WhenEnum,
    specificWhen: LocalTime?,
    onWhenChange: (WhenEnum) -> Unit
) {
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }
    val timeLabel = specificWhen?.format(timeFormatter) ?: "Custom"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        Text(
            "When",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Normal),
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val buttons = listOf(
                WhenEnum.MORNING to "Morning",
                WhenEnum.AFTERNOON to "Afternoon",
                WhenEnum.EVENING to "Evening",
                WhenEnum.NIGHT to "Night",
                WhenEnum.CUSTOM to timeLabel
            )

            buttons.forEach { (type, label) ->
                FilterChip(
                    selected = whenType == type,
                    onClick = { onWhenChange(type) },
                    label = { Text(label) }
                )
            }
        }
    }
}

@Composable
private fun ActionButtons(
    onCancel: () -> Unit,
    onSave: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        TextButton(onClick = onCancel) { Text("Cancel") }
        TextButton(onClick = onSave) { Text("OK") }
    }
}

@Composable
private fun rememberTimePickerHandler(
    context: Context,
    onTimeSelected: (LocalTime) -> Unit,
    onCancel: () -> Unit,
    current: LocalTime?
): () -> Unit = remember(context, current) {
    {
        val initial = current ?: LocalTime.now()
        TimePickerDialog(
            context,
            { _, hour, minute -> onTimeSelected(LocalTime.of(hour, minute)) },
            initial.hour,
            initial.minute,
            true
        ).apply {
            setOnCancelListener { onCancel() }
        }.show()
    }
}

@Preview(
    name = "NewActivityDialog - Light Mode",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Preview(
    name = "NewActivityDialog - Dark Mode",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
fun NewActivityDialogPreview() {
    val activity = DayActivity(
        id = "250691_349020omdker",
        date = LocalDate.of(2025, 12, 18),
        location = "Samui 82 1, Tambon Ang Thong",
        whenType = WhenEnum.CUSTOM,
        specific = LocalTime.of(11, 30),
        what = "Tree Bridge Jungle Zipline and Waterfall Adventure"
    )

    val noDateActivity =
        NoDateActivity(activity.location, activity.whenType, activity.specific, activity.what)

    VoyagerTheme {
        Surface {
            NewActivityBottomSheet(
                activity = noDateActivity,
                onDismissRequest = {},
                saveAction = { _, _, _, _ -> },
                editAction = null,
                activityIndex = null
            )
        }
    }
}