package com.jashvantsewmangal.voyager.ui.screens

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.jashvantsewmangal.voyager.models.Day
import com.jashvantsewmangal.voyager.models.DayState
import com.jashvantsewmangal.voyager.viewmodel.MainViewModel

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun MainScreen(
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onItemClick: (Day) -> Unit,
    onAddClick: () -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    val dayState by viewModel.dayState.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        },
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (val state = dayState) {
                is DayState.Empty -> Text("No items yet")
                is DayState.Error -> Text(state.message)
                is DayState.Done -> Text("Success")
                else -> Text("Loading…")
            }
        }
    }
}