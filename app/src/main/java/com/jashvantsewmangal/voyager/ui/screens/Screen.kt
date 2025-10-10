package com.jashvantsewmangal.voyager.ui.screens

import kotlinx.serialization.Serializable

@Serializable
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Details : Screen("details/{item}")
    object Add : Screen("add")
}