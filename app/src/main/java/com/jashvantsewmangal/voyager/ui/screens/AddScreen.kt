package com.jashvantsewmangal.voyager.ui.screens

import android.content.res.Configuration
import android.net.Uri
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.jashvantsewmangal.voyager.enums.WhenEnum
import com.jashvantsewmangal.voyager.models.NoDateActivity
import com.jashvantsewmangal.voyager.models.SaveState
import com.jashvantsewmangal.voyager.ui.components.DatePickerFieldToModal
import com.jashvantsewmangal.voyager.ui.components.LocationInput
import com.jashvantsewmangal.voyager.ui.components.NewActivityBottomSheet
import com.jashvantsewmangal.voyager.ui.components.NewActivityButton
import com.jashvantsewmangal.voyager.ui.components.copyImageToInternalStorage
import com.jashvantsewmangal.voyager.ui.items.NoDateActivityListItem
import com.jashvantsewmangal.voyager.ui.theme.VoyagerTheme
import com.jashvantsewmangal.voyager.viewmodel.AddViewModel
import kotlinx.coroutines.launch
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
        is SaveState.Done -> SuccessScreen(modifier, returnFunction)
        is SaveState.Error -> ErrorScreen(state.message, modifier, dismissFunction = returnFunction)
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
private fun AddContent(
    returnFunction: () -> Unit,
    saveDayFunction: (
        date: LocalDate,
        locations: List<String>,
        imageUri: String?,
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
    var showDialog by remember { mutableStateOf(false) }
    var selectedActivity by remember { mutableStateOf<NoDateActivity?>(null) }
    var selectedActivityId by remember { mutableStateOf<String?>(null) }
    var imageUri: Uri? by remember { mutableStateOf(null) }
    val locations = remember { mutableStateListOf<String>() }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val snackBarHostState = remember { SnackbarHostState() }

    val pickMedia =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                imageUri = uri
            }
        }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            AddContentTopBar(
                returnFunction = returnFunction,
                date = date,
                saveDayFunction = saveDayFunction,
                locations = locations,
                imageUri = imageUri,
                snackBarHostState = snackBarHostState,
                scrollBehaviour = scrollBehavior
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) }
    ) { innerPadding ->
        AddContentBody(
            innerPadding = innerPadding,
            scrollBehavior = scrollBehavior,
            dateSetter = { date = it },
            imageUri = imageUri,
            onImageSelected = { pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
            locations = locations,
            activities = activities,
            selectActivity = { activity, id ->
                selectedActivity = activity
                selectedActivityId = id
                showDialog = true
            },
            deleteActivityAction = deleteActivityAction
        )
    }

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddContentTopBar(
    returnFunction: () -> Unit,
    date: LocalDate?,
    saveDayFunction: (LocalDate, List<String>, String?) -> Unit,
    locations: List<String>,
    imageUri: Uri?,
    scrollBehaviour: TopAppBarScrollBehavior,
    snackBarHostState: SnackbarHostState,
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

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
                    scope.launch {
                        snackBarHostState.showSnackbar("Please select a date")
                    }
                }
                else {
                    val uri: String? = if (imageUri != null)
                        copyImageToInternalStorage(context, imageUri, date.toString())
                    else
                        null

                    saveDayFunction(date, locations, uri)
                }
            }) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Save",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        scrollBehavior = scrollBehaviour
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddContentBody(
    innerPadding: PaddingValues,
    scrollBehavior: TopAppBarScrollBehavior,
    dateSetter: (LocalDate) -> Unit,
    imageUri: Uri?,
    onImageSelected: () -> Unit,
    locations: SnapshotStateList<String>,
    activities: List<NoDateActivity>,
    selectActivity: (NoDateActivity?, String?) -> Unit,
    deleteActivityAction: (NoDateActivity) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .padding(innerPadding)
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        item { ImagePickerCard(imageUri, onImageSelected) }
        item { DatePickerSection(dateSetter) }
        item {
            LocationInput(
                locations = locations,
                addFunction = locations::add,
                removeFunction = locations::remove
            )
        }
        items(items = activities, key = { it.key }) { activity ->
            NoDateActivityListItem(
                activity = activity,
                modifier = Modifier.animateItem(),
                editAction = { selectActivity(activity, activity.key) },
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
        item {
            NewActivityButton(
                expired = false,
                showDialogEvent = { selectActivity(null, null) },
                emptyActivities = activities.isEmpty(),
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
private fun ImagePickerCard(imageUri: Uri?, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .height(200.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        if (imageUri != null) {
            AsyncImage(
                model = imageUri,
                contentDescription = "Selected Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = "Add Image",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tap to add an image",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun DatePickerSection(onDateSelected: (LocalDate) -> Unit) {
    DatePickerFieldToModal(
        onDateSelected = { selectedDate ->
            onDateSelected(selectedDate)
            Log.d("selectedDate", selectedDate.toString())
        },
        modifier = Modifier.padding(horizontal = 8.dp)
    )
}

@Composable
private fun SuccessScreen(modifier: Modifier = Modifier, returnFunction: () -> Unit) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Success",
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
            Spacer(modifier = Modifier.height(48.dp))
            Button(
                onClick = { returnFunction() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "OK",
                    color = MaterialTheme.colorScheme.onPrimary
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
            SuccessScreen(modifier = Modifier, returnFunction = {})
        }
    }
}