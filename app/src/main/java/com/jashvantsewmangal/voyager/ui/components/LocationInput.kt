package com.jashvantsewmangal.voyager.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

@Composable
fun LocationInput(locations: List<String>, addFunction: (String) -> Unit, removeFunction: (String) -> Unit) {
    var locationInput by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(8.dp)) {
        OutlinedTextField(
            value = locationInput,
            onValueChange = { locationInput = it },
            placeholder = { Text("Locations") },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    if (!focusState.isFocused) { // TextField lost focus
                        val trimmed = locationInput.trim()
                        if (trimmed.isNotEmpty()) {
                            addFunction(trimmed)
                            (trimmed)
                            locationInput = ""
                        }
                    }
                },
            trailingIcon = {
                IconButton(
                    onClick = {
                        val trimmed = locationInput.trim()
                        if (trimmed.isNotEmpty()) {
                            addFunction(trimmed)
                            locationInput = ""
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Location"
                    )
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    val trimmed = locationInput.trim()
                    if (trimmed.isNotEmpty()) {
                        addFunction(trimmed)
                        locationInput = ""
                    }
                }
            )
        )
        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            locations.forEach { loc ->
                AssistChip(
                    onClick = { },
                    label = { Text(loc) },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove Location",
                            modifier = Modifier
                                .size(18.dp)
                                .clickable { removeFunction(loc) }
                        )
                    }
                )
            }
        }
    }
}
