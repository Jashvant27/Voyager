package com.jashvantsewmangal.voyager.ui.items

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jashvantsewmangal.voyager.enums.WhenEnum
import com.jashvantsewmangal.voyager.models.DayActivity
import com.jashvantsewmangal.voyager.ui.theme.VoyagerTheme
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun ActivityListItem(
    activity: DayActivity,
    editAction: (DayActivity) -> Unit,
    deleteAction: (DayActivity) -> Unit,
    modifier: Modifier = Modifier
) {
    val headline = activity.what
    val supportingText = activity.location
    val displayTime =
        if (activity.whenType == WhenEnum.CUSTOM) activity.specific.toString()
        else activity.whenType.name.lowercase().replaceFirstChar { it.uppercase() }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically, // center vertically
        horizontalArrangement = Arrangement.SpaceBetween // space between columns
    ) {
        // First column: takes remaining width
        SelectionContainer(modifier = Modifier.weight(1f)) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = headline,
                    style = MaterialTheme.typography.titleMedium,
                )

                supportingText?.let{
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        // Second column: time indicator
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            AssistChip(
                onClick = { /* Handle click */ },
                label = { Text(displayTime) },
                shape = CircleShape,
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    labelColor = MaterialTheme.colorScheme.primary
                ),
                border = BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.primary)
            )
        }

        // Third column: dropdown menu
        MinimalDropdownMenu(
            activity = activity,
            editAction = editAction,
            deleteAction = deleteAction
        )
    }
}

@Composable
fun MinimalDropdownMenu(
    activity: DayActivity,
    editAction: (DayActivity) -> Unit,
    deleteAction: (DayActivity) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        IconButton(onClick = { expanded = !expanded }) {
            Icon(Icons.Default.MoreVert, contentDescription = "More options")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Edit") },
                onClick = { editAction(activity) }
            )
            DropdownMenuItem(
                text = { Text("Delete") },
                onClick = { deleteAction(activity) }
            )
        }
    }
}

@Preview(
    name = "ActivityListItem - Light Mode",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Preview(
    name = "ActivityListItem - Dark Mode",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
fun ActivityListItemPreview() {
    val activity = DayActivity(
        id = "250691_349020omdker",
        date = LocalDate.of(2025, 12, 18),
        location = "Samui 82 1, Tambon Ang Thong",
        whenType = WhenEnum.CUSTOM,
        specific = LocalTime.of(11, 30),
        what = "Tree Bridge Jungle Zipline and Waterfall Adventure"
    )

    VoyagerTheme {
        Surface {
            ActivityListItem(
                activity = activity,
                editAction = {},
                deleteAction = {}
            )
        }
    }
}