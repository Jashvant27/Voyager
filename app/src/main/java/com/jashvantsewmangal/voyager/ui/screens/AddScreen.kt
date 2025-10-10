package com.jashvantsewmangal.voyager.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.jashvantsewmangal.voyager.viewmodel.AddViewModel

@Composable
fun AddScreen(onSuccess: () -> Unit, viewModel: AddViewModel = hiltViewModel()) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text("Add new day here")
    }
}
