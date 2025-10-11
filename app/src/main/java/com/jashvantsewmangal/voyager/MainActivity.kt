package com.jashvantsewmangal.voyager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.jashvantsewmangal.voyager.ui.screens.MainScreen
import com.jashvantsewmangal.voyager.ui.theme.VoyagerTheme
import dagger.hilt.android.AndroidEntryPoint

// Allows Hilt to inject dependencies into this Activity
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge layout (content behind status/navigation bars)
        enableEdgeToEdge()

        // Set the Compose content for this Activity
        setContent {

            // Apply your app's custom Material3 theme (VoyagerTheme)
            VoyagerTheme {

                // Surface is the root container for your screens
                // It provides a background color and contentColor from the theme
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainScreen()
                }
            }
        }
    }
}