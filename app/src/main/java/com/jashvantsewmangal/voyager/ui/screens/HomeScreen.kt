@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.jashvantsewmangal.voyager.ui.screens

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.jashvantsewmangal.voyager.models.Day
import com.jashvantsewmangal.voyager.models.DayState
import com.jashvantsewmangal.voyager.ui.items.DayListItem
import com.jashvantsewmangal.voyager.ui.theme.VoyagerTheme
import com.jashvantsewmangal.voyager.viewmodel.HomeViewModel
import java.time.LocalDate

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun HomeScreen(
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onItemClick: (Day) -> Unit,
    onAddClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
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
                is DayState.Initial -> LoadingScreen(Modifier)
                is DayState.Loading -> LoadingScreen(Modifier)
                is DayState.Empty -> EmptyScreen(Modifier)
                is DayState.Error -> ErrorScreen(state.message, Modifier)
                is DayState.Done -> ListScreen(
                    state.data,
                    sharedTransitionScope,
                    animatedContentScope,
                    onItemClick,
                    Modifier.padding(horizontal = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptyScreen(modifier: Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Info,
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
                    text = "Your itinerary is empty. Tap the add button to begin planning.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun ListScreen(
    items: List<Day>,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onItemClick: (Day) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        modifier = modifier.fillMaxWidth(),
        verticalItemSpacing = 12.dp,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header item that spans both columns
        item(span = StaggeredGridItemSpan.FullLine) {
            val greeting = remember {
                val hour = java.util.Calendar.getInstance()[(java.util.Calendar.HOUR_OF_DAY)]
                when (hour) {
                    in 5..11 -> "Good morning"
                    in 12..16 -> "Good afternoon"
                    in 17..20 -> "Good evening"
                    else -> "Good night"
                }
            }

            Text(
                text = greeting,
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                textAlign = TextAlign.Center
            )
        }

        items(items = items, key = { day -> day.date }) { day ->
            with(sharedTransitionScope){
                DayListItem(
                    sharedTransitionScope = sharedTransitionScope,
                    animatedContentScope = animatedContentScope,
                    day = day,
                    onItemClick = onItemClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem()
                )
            }
        }
    }
}

@Preview(
    name = "EmptyScreen - Light",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "EmptyScreen - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun PreviewEmptyScreen() {
    VoyagerTheme {
        Surface {
            EmptyScreen(modifier = Modifier)
        }
    }
}

@Preview(
    name = "ListScreen - Light",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "ListScreen - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun PreviewListScreen() {
    val items = listOf(
        Day(
            date = LocalDate.of(2025, 12, 18),
            locations = listOf("Bangkok", "Laem Chaebok", "Pattaya"),
            imageUri = null,
            activities = null
        ),
        Day(
            date = LocalDate.of(2025, 7, 12),
            locations = listOf("Ho Chi Minh City", "Hanoi"),
            imageUri = null,
            activities = null
        ),
        Day(
            date = LocalDate.of(2023, 7, 12),
            locations = listOf("Washington DC"),
            imageUri = null,
            activities = null
        ),
        Day(
            date = LocalDate.of(2026, 7, 10),
            locations = listOf("Amsterdam", "Rotterdam", "Den Haag", "Leiden"),
            imageUri = null,
            activities = null
        )
    )

    VoyagerTheme {
        Surface {
            ListPreviewable(days = items)
        }
    }
}

@Composable
fun ListPreviewable(
    days: List<Day>,
    modifier: Modifier = Modifier
) {
    // Provide fake scopes for preview
    SharedTransitionLayout {
        AnimatedContent(targetState = days) { targetDays ->
            ListScreen(targetDays, this@SharedTransitionLayout, this@AnimatedContent, {}, modifier)
        }
    }
}