@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.jashvantsewmangal.voyager.ui.screens

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jashvantsewmangal.voyager.constants.AppConstants.TRANSITION_DURATION
import com.jashvantsewmangal.voyager.models.Day

@Composable
fun MainScreen(){
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
            composable(
                route = Screen.Home.route,
                enterTransition = {
                    if (initialState.destination.route == Screen.Add.route) {
                        slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.Down,
                            animationSpec = tween(TRANSITION_DURATION)
                        )
                    }
                    else {
                        EnterTransition.None
                    }
                },
                exitTransition = {
                    if (initialState.destination.route == Screen.Add.route) {
                        slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.Up,
                            animationSpec = tween(TRANSITION_DURATION)
                        )
                    }
                    else {
                        ExitTransition.None
                    }
                }
            ) {
                HomeScreen(
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
                AddScreen(onSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(navController.graph.id) {
                            inclusive = true
                        }
                    }
                })
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
                    originalDay = day,
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