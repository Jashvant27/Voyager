package com.jashvantsewmangal.voyager.ui.screens

import android.content.res.Configuration
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.jashvantsewmangal.voyager.enums.WhenEnum
import com.jashvantsewmangal.voyager.models.NoDateActivity
import com.jashvantsewmangal.voyager.models.SaveState
import com.jashvantsewmangal.voyager.ui.components.DatePickerFieldToModal
import com.jashvantsewmangal.voyager.ui.components.NewActivityBottomSheet
import com.jashvantsewmangal.voyager.ui.components.NewActivityButton
import com.jashvantsewmangal.voyager.ui.items.NoDateActivityListItem
import com.jashvantsewmangal.voyager.ui.theme.VoyagerTheme
import com.jashvantsewmangal.voyager.viewmodel.AddViewModel
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun AddScreen(
    returnFunction: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AddViewModel = hiltViewModel()
) {
    val blockBackPress by viewModel.blockBackPressed.collectAsState()
    BackHandler(enabled = !blockBackPress) { returnFunction() }

    val saveState by viewModel.saveState.collectAsState()
    val activityList by viewModel.activityListState.collectAsState()

    when (val state = saveState) {
        is SaveState.Done -> SuccessScreen(modifier)
        is SaveState.Error -> ErrorScreen(state.message, modifier)
        is SaveState.Initial -> AddContent(
            returnFunction = returnFunction,
            saveDayFunction = viewModel::saveDay,
            addActivityFunction = viewModel::addActivity,
            deleteActivityAction = viewModel::deleteActivity,
            editActivityFunction = viewModel::editActivity,
            modifier = modifier,
            activities = activityList
        )

        is SaveState.Loading -> LoadingScreen(modifier)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddContent(
    returnFunction: () -> Unit,
    saveDayFunction: (
        date: LocalDate,
        locations: List<String>,
        imageUri: String,
    ) -> Unit,
    addActivityFunction: (
        location: String?,
        whenType: WhenEnum,
        specific: LocalTime?,
        what: String
    ) -> Unit,
    editActivityFunction: (
        activity: NoDateActivity,
        activityKey: String,
    ) -> Unit,
    deleteActivityAction: (NoDateActivity) -> Unit,
    activities: List<NoDateActivity>,
    modifier: Modifier = Modifier
) {
    var date: LocalDate? by remember { mutableStateOf(null) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    var showDialog by remember { mutableStateOf(false) }
    var selectedActivity by remember { mutableStateOf<NoDateActivity?>(null) }
    var selectedActivityId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text("Add Day") },
                navigationIcon = {
                    IconButton(onClick = returnFunction) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (date == null) {
                            // Show snackbar or warning
                        }
                        else {
                            // saveDayFunction(date!!, ...)
                        }
                    }) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Save",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            // Date picker section
            item {
                DatePickerFieldToModal(
                    onDateSelected = { selectedDate ->
                        date = selectedDate
                        Log.d("selectedDate", date.toString())
                    },
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }

            // Activities list
            items(
                items = activities,
                key = { activity -> activity.key }) { activity ->
                NoDateActivityListItem(
                    activity = activity,
                    modifier = Modifier.animateItem(),
                    editAction = {
                        selectedActivityId = activity.key
                        selectedActivity = activity
                        showDialog = true
                    },
                    deleteAction = { deleteActivityAction(activity) }
                )

                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                )
            }

            // New activity button
            item {
                NewActivityButton(
                    expired = false,
                    showDialogEvent = {
                        selectedActivityId = null
                        selectedActivity = null
                        showDialog = true
                    },
                    emptyActivities = activities.isEmpty(),
                    modifier = Modifier.padding(8.dp)
                )
            }
        }

        // Bottom-sheet
        if (showDialog) {
            NewActivityBottomSheet(
                activityKey = selectedActivityId,
                activity = selectedActivity,
                onDismissRequest = { showDialog = false },
                saveAction = addActivityFunction,
                editAction = editActivityFunction
            )
        }
    }
}

@Composable
fun SuccessScreen(modifier: Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "No data",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Your data has been saved successfully!",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Preview(
    name = "InitialScreen - Light",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "InitialScreen - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun PreviewInitialScreen() {
    VoyagerTheme {
        Surface {
            AddContent(
                returnFunction = { },
                saveDayFunction = { _, _, _ -> },
                addActivityFunction = { _, _, _, _ -> },
                modifier = Modifier,
                activities = emptyList(),
                deleteActivityAction = { _ -> },
                editActivityFunction = { _, _ -> }
            )
        }
    }
}

@Preview(
    name = "SuccessScreen - Light",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "SuccessScreen - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun PreviewSuccessScreen() {
    VoyagerTheme {
        Surface {
            SuccessScreen(modifier = Modifier)
        }
    }
}