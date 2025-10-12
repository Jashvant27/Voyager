package com.jashvantsewmangal.voyager.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddCircleOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun NewActivityButton(
    showDialogEvent: () -> Unit,
    expired: Boolean,
    emptyActivities: Boolean,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null // 👈 disables ripple/press animation
            ) {
                showDialogEvent()
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val iconColor = if (expired)
            MaterialTheme.colorScheme.onSurfaceVariant
        else
            MaterialTheme.colorScheme.primary

        Icon(
            imageVector = Icons.Rounded.AddCircleOutline,
            contentDescription = "Back",
            tint = iconColor,
            modifier = Modifier.size(32.dp)
        )
        if (emptyActivities) {
            Text(
                text = "Add an activity",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
