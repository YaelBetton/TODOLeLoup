package com.example.todoleloup.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object CreateTask : Screen("create_task")
    object Profile : Screen("profile")
}

