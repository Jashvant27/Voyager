package com.jashvantsewmangal.voyager.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.jashvantsewmangal.voyager.models.Day
import com.jashvantsewmangal.voyager.viewmodel.EditViewModel

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun DetailScreen(
    day: Day,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onBackPressed: () -> Unit,
    viewModel: EditViewModel = hiltViewModel()
) {
    // Observe the boolean from ViewModel
    val blockBackPress by viewModel.blockBackPressed.collectAsState()

    // Intercept back press conditionally
    BackHandler(enabled = !blockBackPress) {
        // Only trigger if not blocked
        onBackPressed()
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
    }
}
