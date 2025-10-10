package com.jashvantsewmangal.voyager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jashvantsewmangal.voyager.constants.AppConstants.TRANSITION_DURATION
import com.jashvantsewmangal.voyager.models.Day
import com.jashvantsewmangal.voyager.ui.screens.AddScreen
import com.jashvantsewmangal.voyager.ui.screens.DetailScreen
import com.jashvantsewmangal.voyager.ui.screens.MainScreen
import com.jashvantsewmangal.voyager.ui.screens.Screen
import com.jashvantsewmangal.voyager.ui.theme.VoyagerTheme
import dagger.hilt.android.AndroidEntryPoint

// Allows Hilt to inject dependencies into this Activity
@AndroidEntryPoint
// Opt-in for experimental SharedTransition API for screen animations
@OptIn(ExperimentalSharedTransitionApi::class)
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

                    // Remember a NavController for navigation between screens
                    val navController = rememberNavController() // Supports transitions

                    // SharedTransitionLayout enables animated transitions between composable
                    SharedTransitionLayout {

                        // NavHost manages navigation between composable screens
                        NavHost(
                            navController = navController,
                            startDestination = Screen.Home.route
                        ) {

                            // Home/Main screen route
                            composable(Screen.Home.route) {
                                MainScreen(
                                    sharedTransitionScope = this@SharedTransitionLayout,
                                    animatedContentScope = this@composable,
                                    onItemClick = { item ->
                                        // Save the selected item in the current backstack entry
                                        navController.currentBackStackEntry?.savedStateHandle?.set(
                                            "item",
                                            item
                                        )
                                        // Navigate to the details screen with item's date as argument
                                        navController.navigate("details/${item.date}")
                                    },
                                    onAddClick = {
                                        // Navigate to the Add screen
                                        navController.navigate(Screen.Add.route)
                                    }
                                )
                            }

                            // Add screen route with enter/exit animations
                            composable(
                                route = Screen.Add.route,
                                enterTransition = {
                                    slideIntoContainer(
                                        AnimatedContentTransitionScope.SlideDirection.Up,
                                        animationSpec = tween(TRANSITION_DURATION) // 700ms animation
                                    )
                                },
                                exitTransition = {
                                    slideOutOfContainer(
                                        AnimatedContentTransitionScope.SlideDirection.Down,
                                        animationSpec = tween(TRANSITION_DURATION)
                                    )
                                }
                            ) {
                                // Display the AddScreen composable
                                AddScreen(onSuccess = { navController.navigate(Screen.Add.route) })
                            }

                            // Details screen route with a string argument "item"
                            composable(
                                Screen.Details.route,
                                arguments = listOf(navArgument("item") {
                                    type = NavType.StringType
                                })
                            ) {
                                // Retrieve the Day object from the previous backstack entry
                                val day = navController.previousBackStackEntry
                                    ?.savedStateHandle
                                    ?.get<Day>("day")

                                // If no Day is found, skip composition
                                if (day == null) return@composable

                                // Display the DetailScreen composable
                                DetailScreen(
                                    day = day,
                                    sharedTransitionScope = this@SharedTransitionLayout,
                                    animatedContentScope = this@composable,
                                    onBackPressed = {
                                        // Navigate back to Home screen
                                        navController.navigate(Screen.Home.route)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}