@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.jashvantsewmangal.voyager.ui.screens

import android.content.res.Configuration
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.jashvantsewmangal.voyager.R
import com.jashvantsewmangal.voyager.constants.AppConstants.DB_DELETE_SUCCESS
import com.jashvantsewmangal.voyager.enums.WhenEnum
import com.jashvantsewmangal.voyager.models.Day
import com.jashvantsewmangal.voyager.models.DayActivity
import com.jashvantsewmangal.voyager.models.NoDateActivity
import com.jashvantsewmangal.voyager.ui.components.LocationBottomSheet
import com.jashvantsewmangal.voyager.ui.components.NewActivityButton
import com.jashvantsewmangal.voyager.ui.items.ActivityListItem
import com.jashvantsewmangal.voyager.ui.theme.VoyagerTheme
import com.jashvantsewmangal.voyager.viewmodel.EditViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate
import java.time.LocalTime

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun DetailScreen(
    originalDay: Day,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EditViewModel = hiltViewModel()
) {
    // Give the viewmodel the current day
    LaunchedEffect(Unit) {
        viewModel.setDay(originalDay, emitToStateFlow = false)
    }

    val blockBackPress by viewModel.blockBackPressed.collectAsState()
    BackHandler(enabled = !blockBackPress) { onBackPressed() }

    // SnackBar
    val toastMessage by viewModel.toastState.collectAsState()
    val snackBarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(toastMessage) {
        toastMessage?.let { message ->
            snackBarHostState.currentSnackbarData?.dismiss()
            coroutineScope.launch {
                snackBarHostState.showSnackbar(
                    message = message,
                    duration = SnackbarDuration.Short
                )

                if(toastMessage == DB_DELETE_SUCCESS){
                    onBackPressed()
                }
            }

        }
    }

    val day by viewModel.dayStateFlow.collectAsState()
    val detailDay = day ?: originalDay
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) }
    ) { innerPadding ->
        with(sharedTransitionScope) {
            DetailContent(
                day = detailDay,
                sharedTransitionScope = sharedTransitionScope,
                animatedContentScope = animatedContentScope,
                onBackPressed = onBackPressed,
                allowedBack = !blockBackPress,
                deleteDayAction = viewModel::deleteDay,
                changeImageAction = viewModel::changeImage,
                saveActivityAction = viewModel::saveActivity,
                editActivityAction = viewModel::updateActivity,
                deleteActivityAction = viewModel::deleteActivity,
                updateLocationAction = { showDialog = true },
                modifier = modifier.padding(innerPadding)
            )
        }
    }

    if (showDialog) {
        LocationBottomSheet (
            locations = detailDay.locations,
            onDismissRequest = {showDialog = false},
            addFunction = viewModel::addLocation,
            removeFunction = viewModel::removeLocation
        )
    }
}

@Composable
private fun SharedTransitionScope.DetailContent(
    day: Day,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onBackPressed: () -> Unit,
    allowedBack: Boolean,
    changeImageAction: (imageUri: String?) -> Unit,
    deleteDayAction: (day: Day) -> Unit,
    saveActivityAction: ((
        location: String,
        whenType: WhenEnum,
        specific: LocalTime,
        what: String
    ) -> Unit),
    editActivityAction: (id: String, activity: NoDateActivity) -> Unit,
    updateLocationAction: () -> Unit,
    deleteActivityAction: (activity: DayActivity) -> Unit,
    modifier: Modifier = Modifier
) {
    val expired = day.expired()
    val activitiesEmpty = day.activities.isNullOrEmpty()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 8.dp)
    ) {
        // Top App Bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    if (allowedBack) {
                        onBackPressed()
                    }
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }

                MinimalDropdownMenu(
                    date = day.date,
                    changeImageAction = changeImageAction,
                    updateLocationAction =
                        updateLocationAction,
                    deleteDayAction = {
                        deleteDayAction(day)
                    }
                )
            }
        }

        // Header Image
        item {
            HeaderImage(
                day.imageUri,
                day.date,
                expired,
                sharedTransitionScope,
                animatedContentScope
            )
        }

        // Title & Locations
        item {
            TitleBar(day, expired, sharedTransitionScope, animatedContentScope)
        }

        day.activities?.let { activities ->
            items(items = activities, key = { day -> day.id }) { activity ->
                ActivityListItem(
                    activity = activity,
                    modifier = Modifier.animateItem(),
                    editAction = {
                        // TODO: show pop-up where you can edit the values in
                        // On-save call the edit Activity Action with the new values
                    },
                    deleteAction = deleteActivityAction
                )

                // Add a divider between items, but skip after the last one
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                )
            }
        }

        // Optional: New Activity Button
        item {
            NewActivityButton(
                showDialogEvent = {
                    //TODO: show pop-up
                },
                expired = expired,
                emptyActivities = activitiesEmpty,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun MinimalDropdownMenu(
    date: LocalDate,
    changeImageAction: (imageUri: String?) -> Unit,
    updateLocationAction: () -> Unit,
    deleteDayAction: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val pickMedia =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { sourceUri ->
            if (sourceUri != null) {
                val inputStream = context.contentResolver.openInputStream(sourceUri)
                val file = File(context.filesDir, "${date}_header.jpg")
                inputStream.use { input ->
                    file.outputStream().use { output ->
                        input?.copyTo(output)
                    }
                }
                // Convert the saved file to a Uri
                changeImageAction(Uri.fromFile(file).toString())
            }
        }

    Box {
        IconButton(onClick = { expanded = !expanded }) {
            Icon(Icons.Default.MoreVert, contentDescription = "More options")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Change image") },
                onClick = {
                    // Launch the photo picker and let the user choose only images.
                    pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }
            )
            DropdownMenuItem(
                text = { Text("Remove image") },
                onClick = {
                    changeImageAction(null)
                }
            )
            DropdownMenuItem(
                text = { Text("Change locations") },
                onClick = updateLocationAction
            )
            DropdownMenuItem(
                text = { Text("Delete day") },
                onClick = deleteDayAction
            )
        }
    }
}

@Composable
private fun SharedTransitionScope.HeaderImage(
    imageUri: String?,
    date: LocalDate,
    expired: Boolean,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope
) {
    val painter = if (LocalInspectionMode.current) {
        painterResource(id = R.drawable.fallback)
    }
    else {
        rememberAsyncImagePainter(
            model = imageUri,
            error = painterResource(id = R.drawable.fallback),
            fallback = painterResource(id = R.drawable.fallback)
        )
    }

    val colorMatrix = remember(expired) {
        if (expired) ColorMatrix().apply { setToSaturation(0f) } else ColorMatrix()
    }

    Image(
        painter = painter,
        contentDescription = "Day image",
        modifier = Modifier
            .padding(horizontal = 8.dp) // horizontal margin
            .clip(RoundedCornerShape(12.dp)) // rounded corners
            .fillMaxWidth() // width fills parent
            .graphicsLayer {
                if (expired) alpha = 0.7f
            }
            .sharedElement(
                sharedTransitionScope.rememberSharedContentState(key = "image-${date}"),
                animatedVisibilityScope = animatedContentScope
            )
            .drawWithContent {
                val paint = Paint().apply {
                    colorFilter = ColorFilter.colorMatrix(colorMatrix)
                }
                drawContext.canvas.saveLayer(bounds = size.toRect(), paint)
                drawContent()
                drawContext.canvas.restore()
            },
        contentScale = ContentScale.Crop // scales content inside bounds
    )

    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
private fun SharedTransitionScope.TitleBar(
    day: Day,
    expired: Boolean,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
) {
    val titleColor = if (expired)
        MaterialTheme.colorScheme.onSurfaceVariant
    else
        MaterialTheme.colorScheme.primary

    Text(
        text = day.formattedDate(),
        style = MaterialTheme.typography.headlineLarge,
        color = titleColor,
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp, top = 8.dp)
            .sharedElement(
                sharedTransitionScope.rememberSharedContentState(key = "date-${day.date}"),
                animatedVisibilityScope = animatedContentScope
            )
    )

    val locations = day.locations

    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        locations.forEach { location ->
            AssistChip(
                onClick = { /* Handle click */ },
                label = { Text(location) },
                shape = CircleShape,
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = titleColor,
                    labelColor = MaterialTheme.colorScheme.background
                ),
                border = BorderStroke(width = 0.dp, color = MaterialTheme.colorScheme.primary)
            )
        }
    }
}

@Composable
private fun DetailScreenPreviewable(
    day: Day
) {
    // Provide fake scopes for preview
    SharedTransitionLayout {
        AnimatedContent(targetState = day) { targetDay ->
            DetailContent(
                day = targetDay,
                sharedTransitionScope = this@SharedTransitionLayout,
                animatedContentScope = this@AnimatedContent,
                onBackPressed = {},
                allowedBack = true,
                changeImageAction = { _ -> },
                deleteDayAction = { _ -> },
                saveActivityAction = { _, _, _, _ -> },
                editActivityAction = { _, _ -> },
                updateLocationAction = { -> },
                deleteActivityAction = { _ -> }
            )
        }
    }
}

@Preview(
    name = "DetailScreen - Light Mode",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Preview(
    name = "DetailScreen - Dark Mode",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
fun PreviewDetailScreen() {
    val previewActivityCustom = DayActivity(
        id = "250691_349020omdker",
        date = LocalDate.of(2025, 12, 18),
        location = "Samui 82 1, Tambon Ang Thong",
        whenType = WhenEnum.CUSTOM,
        specific = LocalTime.of(11, 30),
        what = "Tree Bridge Jungle Zipline and Waterfall Adventure"
    )

    val previewActivityAfternoon = DayActivity(
        id = "250691_349020o3203mdker",
        date = LocalDate.of(2025, 12, 14),
        location = "Chua Buu Dai Son",
        whenType = WhenEnum.AFTERNOON,
        specific = null,
        what = "Private Paragliding Experience in Da Nang"
    )

    val previewActivityNight = DayActivity(
        id = "253691_349020omdker",
        date = LocalDate.of(2025, 12, 3),
        location = null,
        whenType = WhenEnum.EVENING,
        specific = null,
        what = "Victoria Harbour Night Cruise"
    )

    val previewDay = Day(
        date = LocalDate.of(2025, 12, 18),
        locations = listOf("Bangkok", "Laem Chaebok", "Pattaya", "Kuala Lumpur"),
        imageUri = null,
        activities = listOf(previewActivityCustom, previewActivityAfternoon, previewActivityNight)
    )

    VoyagerTheme {
        Surface {
            DetailScreenPreviewable(day = previewDay)
        }
    }
}

@Preview(
    name = "DetailScreen - Light Mode (No activities)",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Preview(
    name = "DetailScreen - Dark Mode (No activities)",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
fun PreviewDetailScreenNoActivities() {
    val previewDay = Day(
        date = LocalDate.of(2025, 3, 3),
        locations = listOf("Hong Kong"),
        imageUri = null,
        activities = null
    )

    VoyagerTheme {
        Surface {
            DetailScreenPreviewable(day = previewDay)
        }
    }
}

