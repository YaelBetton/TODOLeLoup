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
import com.example.todoleloup.data.Priority
import com.example.todoleloup.data.RecurrenceType
import com.example.todoleloup.data.TaskStatus
import com.example.todoleloup.data.Task
import com.example.todoleloup.ui.navigation.Screen
import com.example.todoleloup.ui.screens.CreateTaskScreen
import com.example.todoleloup.ui.screens.EditTaskScreen
import com.example.todoleloup.ui.screens.HomeScreen
import com.example.todoleloup.ui.screens.ShopScreen
import com.example.todoleloup.ui.theme.*
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

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
                                        val newStatus = if (task.status == TaskStatus.DONE) {
                                            TaskStatus.TODO
                                        } else {
                                            TaskStatus.DONE
                                        }
                                        task.copy(status = newStatus)
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
                            onTaskCreated = { title, dateStr, timeStr, isUrgent ->
                                val priority = if (isUrgent) {
                                    Priority.HIGH
                                } else {
                                    Priority.MEDIUM
                                }

                                // Parsez la date (format jj/mm/aaaa)
                                var deadlineDate: LocalDate? = null
                                var deadlineTime: LocalTime? = null

                                if (dateStr.isNotBlank()) {
                                    try {
                                        val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                                        deadlineDate = LocalDate.parse(dateStr, dateFormatter)
                                    } catch (e: Exception) {
                                        // Format invalide, ignore
                                    }
                                }

                                if (timeStr.isNotBlank()) {
                                    try {
                                        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
                                        deadlineTime = LocalTime.parse(timeStr, timeFormatter)
                                    } catch (e: Exception) {
                                        // Format invalide, ignore
                                    }
                                }

                                val newTask = Task(
                                    id = tasks.size + 1,
                                    title = title,
                                    description = "",
                                    deadlineDate = deadlineDate,
                                    deadlineTime = deadlineTime,
                                    status = TaskStatus.TODO,
                                    priority = priority,
                                    recurrence = RecurrenceType.NONE
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
                            // Formater la date et l'heure existantes pour l'affichage
                            val initialDateStr = if (taskToEdit.deadlineDate != null) {
                                val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                                taskToEdit.deadlineDate.format(dateFormatter)
                            } else {
                                ""
                            }

                            val initialTimeStr = if (taskToEdit.deadlineTime != null) {
                                val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
                                taskToEdit.deadlineTime.format(timeFormatter)
                            } else {
                                ""
                            }

                            EditTaskScreen(
                                initialTitle = taskToEdit.title,
                                initialIsUrgent = taskToEdit.priority == Priority.HIGH,
                                initialDeadlineDate = initialDateStr,
                                initialDeadlineTime = initialTimeStr,
                                onNavigateBack = {
                                    currentScreen = Screen.Home
                                    editingTaskId = null
                                },
                                onTaskUpdated = { newTitle, dateStr, timeStr, newIsUrgent ->
                                    val updatedPriority = if (newIsUrgent) {
                                        Priority.HIGH
                                    } else {
                                        Priority.MEDIUM
                                    }

                                    // Parser la date (format jj/mm/aaaa)
                                    var deadlineDate: LocalDate? = null
                                    var deadlineTime: LocalTime? = null

                                    if (dateStr.isNotBlank()) {
                                        try {
                                            val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                                            deadlineDate = LocalDate.parse(dateStr, dateFormatter)
                                        } catch (e: Exception) {
                                            // Format invalide, garde la date existante
                                            deadlineDate = taskToEdit.deadlineDate
                                        }
                                    }

                                    if (timeStr.isNotBlank()) {
                                        try {
                                            val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
                                            deadlineTime = LocalTime.parse(timeStr, timeFormatter)
                                        } catch (e: Exception) {
                                            // Format invalide, garde l'heure existante
                                            deadlineTime = taskToEdit.deadlineTime
                                        }
                                    }

                                    tasks = tasks.map { task ->
                                        if (task.id == taskToEdit.id) {
                                            task.copy(
                                                title = newTitle,
                                                priority = updatedPriority,
                                                deadlineDate = deadlineDate,
                                                deadlineTime = deadlineTime
                                            )
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