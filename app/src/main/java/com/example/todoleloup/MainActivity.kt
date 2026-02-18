package com.example.todoleloup

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.todoleloup.data.Task
import com.example.todoleloup.ui.navigation.Screen
import com.example.todoleloup.ui.screens.CreateTaskScreen
import com.example.todoleloup.ui.screens.EditTaskScreen
import com.example.todoleloup.ui.screens.HomeScreen
import com.example.todoleloup.ui.screens.ShopScreen
import com.example.todoleloup.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TodoLeLoupTheme {
                TodoLeLoupApp()
            }
        }
    }
}

@Composable
fun TodoLeLoupApp() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    var selectedTab by remember { mutableStateOf(0) }
    var tasks by remember { mutableStateOf(listOf<Task>()) }
    var editingTaskId by remember { mutableStateOf<Int?>(null) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DarkBackground
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = DarkBackground,
            bottomBar = {
                if (currentScreen == Screen.Home || currentScreen == Screen.Shop) {
                    BottomNavigationBar(
                        selectedTab = selectedTab,
                        onTabSelected = {
                            selectedTab = it
                            currentScreen = when (it) {
                                0 -> Screen.Home
                                1 -> Screen.Shop
                                else -> Screen.Home
                            }
                        }
                    )
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                when (currentScreen) {
                    Screen.Home -> {
                        HomeScreen(
                            onNavigateToCreateTask = {
                                currentScreen = Screen.CreateTask
                            },
                            tasks = tasks,
                            onToggleTaskCompleted = { taskToToggle ->
                                tasks = tasks.map { task ->
                                    if (task.id == taskToToggle.id) {
                                        task.copy(isCompleted = !task.isCompleted)
                                    } else {
                                        task
                                    }
                                }
                            },
                            onEditTask = { taskToEdit ->
                                editingTaskId = taskToEdit.id
                                currentScreen = Screen.EditTask
                            }
                        )
                    }
                    Screen.CreateTask -> {
                        CreateTaskScreen(
                            onNavigateBack = {
                                currentScreen = Screen.Home
                            },
                            onTaskCreated = { title, isUrgent ->
                                val newTask = Task(
                                    id = tasks.size + 1,
                                    title = title,
                                    isCompleted = false,
                                    isUrgent = isUrgent
                                )
                                tasks = tasks + newTask
                            }
                        )
                    }
                    Screen.EditTask -> {
                        val taskToEdit = tasks.find { it.id == editingTaskId }
                        LaunchedEffect(taskToEdit) {
                            if (taskToEdit == null) {
                                currentScreen = Screen.Home
                                editingTaskId = null
                            }
                        }
                        if (taskToEdit != null) {
                            EditTaskScreen(
                                initialTitle = taskToEdit.title,
                                initialIsUrgent = taskToEdit.isUrgent,
                                onNavigateBack = {
                                    currentScreen = Screen.Home
                                    editingTaskId = null
                                },
                                onTaskUpdated = { newTitle, newIsUrgent ->
                                    tasks = tasks.map { task ->
                                        if (task.id == taskToEdit.id) {
                                            task.copy(title = newTitle, isUrgent = newIsUrgent)
                                        } else {
                                            task
                                        }
                                    }
                                }
                            )
                        }
                    }
                    Screen.Shop -> {
                        ShopScreen()
                    }
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp),
        color = DarkSurface
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { onTabSelected(0) }
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Accueil",
                    tint = if (selectedTab == 0) CyanPrimary else TextSecondary,
                    modifier = Modifier.size(28.dp)
                )
            }

            IconButton(
                onClick = { onTabSelected(1) }
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = "Boutique",
                    tint = if (selectedTab == 1) CyanPrimary else TextSecondary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}