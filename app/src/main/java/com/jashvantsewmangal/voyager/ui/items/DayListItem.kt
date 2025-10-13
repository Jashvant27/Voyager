@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.jashvantsewmangal.voyager.ui.items

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.jashvantsewmangal.voyager.R
import com.jashvantsewmangal.voyager.constants.AppConstants.TRANSITION_DURATION
import com.jashvantsewmangal.voyager.models.Day
import com.jashvantsewmangal.voyager.ui.theme.VoyagerTheme
import java.time.LocalDate

@Composable
fun SharedTransitionScope.DayListItem(
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    day: Day,
    onItemClick: (Day) -> Unit,
    modifier: Modifier = Modifier
) {
    val expired = day.expired()

    OutlinedCard(
        modifier = modifier
            .wrapContentHeight()
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        onClick = { onItemClick(day) }
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            DayImageSection(
                day = day,
                expired = expired,
                sharedTransitionScope,
                animatedContentScope
            )
            DayTextSection(
                day = day,
                expired = expired,
                sharedTransitionScope = sharedTransitionScope,
                animatedContentScope = animatedContentScope
            )
        }
    }
}

@Composable
private fun SharedTransitionScope.DayImageSection(
    day: Day,
    expired: Boolean,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope
) {
    val painter = if (LocalInspectionMode.current) {
        painterResource(id = R.drawable.fallback)
    }
    else {
        rememberAsyncImagePainter(
            model = day.imageUri,
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
            .fillMaxWidth()
            .aspectRatio(4f / 3f)
            .graphicsLayer { if (expired) alpha = 0.7f }
            // keep sharedElement before clip if you still see issues with overlay clipping:
            .sharedElement(
                sharedTransitionScope.rememberSharedContentState(key = "image-${day.date}"),
                animatedVisibilityScope = animatedContentScope,
                boundsTransform = { _, _ ->
                    tween(durationMillis = TRANSITION_DURATION)
                }
            )
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
        contentScale = ContentScale.Crop,
        // pass ColorFilter directly instead of saveLayer
        colorFilter = if (expired) ColorFilter.colorMatrix(colorMatrix) else null
    )
}

@Composable
private fun SharedTransitionScope.DayTextSection(
    day: Day,
    expired: Boolean,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope
) {
    val titleColor = if (expired)
        MaterialTheme.colorScheme.onSurfaceVariant
    else
        MaterialTheme.colorScheme.primary

    val subtitleColor = if (expired)
        MaterialTheme.colorScheme.outline
    else
        MaterialTheme.colorScheme.onSurfaceVariant

    Column(modifier = Modifier.padding(12.dp)) {
        Text(
            text = day.formattedDate(),
            style = MaterialTheme.typography.titleMedium,
            color = titleColor,
            modifier = Modifier.sharedElement(
                sharedTransitionScope.rememberSharedContentState(key = "date-${day.date}"),
                animatedVisibilityScope = animatedContentScope,
                boundsTransform = { _, _ ->
                    tween(durationMillis = TRANSITION_DURATION)
                }
            )
        )
        day.formattedLocations()?.let { formattedLocations ->

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = formattedLocations,
                style = MaterialTheme.typography.bodyMedium,
                color = subtitleColor
            )
        }
    }
}

@Composable
fun DayListItemPreviewable(
    day: Day,
    modifier: Modifier = Modifier
) {
    // Provide fake scopes for preview
    SharedTransitionLayout {
        AnimatedContent(targetState = day) { targetDay ->
            DayListItem(
                sharedTransitionScope = this@SharedTransitionLayout,
                animatedContentScope = this@AnimatedContent,
                day = targetDay,
                onItemClick = {},
                modifier = modifier
            )
        }
    }
}

@Preview(
    name = "DayListItem - Light Mode",
    showBackground = false,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Preview(
    name = "DayListItem - Dark Mode",
    showBackground = false,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
fun PreviewDayListItem() {
    val previewDay = Day(
        date = LocalDate.of(2025, 12, 18),
        locations = listOf("Bangkok", "Laem Chaebok", "Pattaya"),
        imageUri = null,
        activities = null
    )

    VoyagerTheme {
        Surface(
            modifier = Modifier
                .padding(16.dp)
                .width(240.dp),
            color = Color.Transparent
        ) {
            DayListItemPreviewable(day = previewDay)
        }
    }
}
