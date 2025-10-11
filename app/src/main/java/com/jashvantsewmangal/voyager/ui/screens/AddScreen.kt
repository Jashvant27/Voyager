package com.jashvantsewmangal.voyager.ui.screens

import android.content.res.Configuration
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.jashvantsewmangal.voyager.models.SaveState
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
    val saveState by viewModel.saveState.collectAsState()
    val blockBackPress by viewModel.blockBackPressed.collectAsState()
    BackHandler(enabled = !blockBackPress) { returnFunction() }

    when (val state = saveState) {
        is SaveState.Done -> SuccessScreen(modifier)
        is SaveState.Error -> ErrorScreen(state.message, modifier)
        is SaveState.Initial -> AddContent(
            returnFunction = returnFunction,
            saveDayFunction = viewModel::saveDay,
            addActivityFunction = viewModel::addActivity,
            modifier = modifier
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
        location: String,
        whenType: WhenEnum,
        specific: LocalTime,
        what: String
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    var date: LocalDate? by remember { mutableStateOf(null) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val scrollState = rememberScrollState()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text("Add Day") },
                navigationIcon = {
                    IconButton(onClick = { returnFunction() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (date == null) {
                            // Show snackbar, toast, or dialog prompting for date selection
                        }
                        /* save */

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
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            DatePickerFieldToModal(
                onDateSelected = { selectedDate ->
                    date = selectedDate
                    Log.d("selectedDate", date.toString())
                },
                modifier = modifier.padding(8.dp)
            )

            // You can add more content here…
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
                modifier = Modifier
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