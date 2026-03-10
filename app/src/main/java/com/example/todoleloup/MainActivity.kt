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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.todoleloup.data.Priority
import com.example.todoleloup.data.RecurrenceType
import com.example.todoleloup.data.TaskStatus
import com.example.todoleloup.data.Task
import com.example.todoleloup.data.pointsForPriority
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
    val rewardViewModel: RewardViewModel = viewModel()

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
                                val today = java.time.LocalDate.now()

                                if (taskToToggle.status == TaskStatus.DONE) {
                                    // Décocher → repasse en TODO
                                    val nextOccurrence = tasks.find { it.id == taskToToggle.nextOccurrenceId }
                                    val shouldDeleteNext = nextOccurrence != null
                                        && nextOccurrence.deadlineDate != null
                                        && nextOccurrence.deadlineDate.isAfter(today)

                                    tasks = tasks
                                        .filter { !shouldDeleteNext || it.id != taskToToggle.nextOccurrenceId }
                                        .map { task ->
                                            if (task.id == taskToToggle.id)
                                                task.copy(status = TaskStatus.TODO, nextOccurrenceId = null)
                                            else task
                                        }
                                } else {
                                    // Cocher → gagner des points une seule fois
                                    if (!taskToToggle.rewardClaimed) {
                                        rewardViewModel.addPoints(pointsForPriority(taskToToggle.priority))
                                    }

                                    if (taskToToggle.recurrence != RecurrenceType.NONE
                                        && taskToToggle.deadlineDate != null
                                        && !taskToToggle.deadlineDate.isAfter(today)) {
                                        val nextDate = when (taskToToggle.recurrence) {
                                            RecurrenceType.DAILY -> taskToToggle.deadlineDate.plusDays(1)
                                            RecurrenceType.WEEKLY -> taskToToggle.deadlineDate.plusWeeks(1)
                                            RecurrenceType.MONTHLY -> taskToToggle.deadlineDate.plusMonths(1)
                                            else -> null
                                        }
                                        if (nextDate != null) {
                                            val alreadyExists = tasks.any { t ->
                                                t.id != taskToToggle.id
                                                && t.title == taskToToggle.title
                                                && t.recurrence == taskToToggle.recurrence
                                                && t.deadlineDate == nextDate
                                            }
                                            if (!alreadyExists) {
                                                val nextId = tasks.maxOf { it.id } + 1
                                                val nextOccurrence = taskToToggle.copy(
                                                    id = nextId,
                                                    status = TaskStatus.TODO,
                                                    deadlineDate = nextDate,
                                                    nextOccurrenceId = null,
                                                    rewardClaimed = false
                                                )
                                                tasks = tasks.map { task ->
                                                    if (task.id == taskToToggle.id)
                                                        task.copy(status = TaskStatus.DONE, nextOccurrenceId = nextId, rewardClaimed = true)
                                                    else task
                                                } + nextOccurrence
                                            } else {
                                                tasks = tasks.map { task ->
                                                    if (task.id == taskToToggle.id)
                                                        task.copy(status = TaskStatus.DONE, nextOccurrenceId = null, rewardClaimed = true)
                                                    else task
                                                }
                                            }
                                        } else {
                                            tasks = tasks.map { task ->
                                                if (task.id == taskToToggle.id)
                                                    task.copy(status = TaskStatus.DONE, rewardClaimed = true)
                                                else task
                                            }
                                        }
                                    } else {
                                        tasks = tasks.map { task ->
                                            if (task.id == taskToToggle.id)
                                                task.copy(status = TaskStatus.DONE, rewardClaimed = true)
                                            else task
                                        }
                                    }
                                }
                            },
                            onEditTask = { taskToEdit ->
                                editingTaskId = taskToEdit.id
                                currentScreen = Screen.EditTask
                            },
                            onDeleteTask = { taskToDelete ->
                                tasks = tasks.filter { it.id != taskToDelete.id }
                            },
                            onClearCompletedTasks = {
                                tasks = tasks.filter { it.status != TaskStatus.DONE }
                            },
                            points = rewardViewModel.points,
                            lastEarnedPoints = rewardViewModel.lastEarnedPoints,
                            onClearLastEarned = { rewardViewModel.clearLastEarned() },
                            activeBackground = rewardViewModel.activeBackground
                        )
                    }
                    Screen.CreateTask -> {
                        CreateTaskScreen(
                            onNavigateBack = { currentScreen = Screen.Home },
                            onTaskCreated = { title, description, dateStr, timeStr, priority, recurrence, photoUri ->
                                var deadlineDate: LocalDate? = null
                                var deadlineTime: LocalTime? = null
                                if (dateStr.isNotBlank()) {
                                    try { deadlineDate = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("dd/MM/yyyy")) } catch (e: Exception) {}
                                }
                                if (timeStr.isNotBlank()) {
                                    try { deadlineTime = LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm")) } catch (e: Exception) {}
                                }
                                val newTask = Task(
                                    id = if (tasks.isEmpty()) 1 else tasks.maxOf { it.id } + 1,
                                    title = title,
                                    description = description,
                                    deadlineDate = deadlineDate,
                                    deadlineTime = deadlineTime,
                                    status = TaskStatus.TODO,
                                    priority = priority,
                                    recurrence = recurrence,
                                    photoUri = photoUri
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
                                initialDescription = taskToEdit.description,
                                initialPriority = taskToEdit.priority,
                                initialDeadlineDate = initialDateStr,
                                initialDeadlineTime = initialTimeStr,
                                initialRecurrence = taskToEdit.recurrence,
                                initialPhotoUri = taskToEdit.photoUri,
                                onNavigateBack = {
                                    currentScreen = Screen.Home
                                    editingTaskId = null
                                },
                                onTaskUpdated = { newTitle, newDescription, dateStr, timeStr, newPriority, newRecurrence, newPhotoUri ->
                                    var deadlineDate: LocalDate? = null
                                    var deadlineTime: LocalTime? = null
                                    if (dateStr.isNotBlank()) {
                                        try { deadlineDate = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("dd/MM/yyyy")) }
                                        catch (e: Exception) { deadlineDate = taskToEdit.deadlineDate }
                                    }
                                    if (timeStr.isNotBlank()) {
                                        try { deadlineTime = LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm")) }
                                        catch (e: Exception) { deadlineTime = taskToEdit.deadlineTime }
                                    }
                                    tasks = tasks.map { task ->
                                        if (task.id == taskToEdit.id)
                                            task.copy(title = newTitle, description = newDescription, priority = newPriority,
                                                deadlineDate = deadlineDate, deadlineTime = deadlineTime,
                                                recurrence = newRecurrence, photoUri = newPhotoUri)
                                        else task
                                    }
                                }
                            )
                        }
                    }
                    Screen.Shop -> {
                        ShopScreen(
                            points = rewardViewModel.points,
                            activeBackground = rewardViewModel.activeBackground,
                            unlockedBackgrounds = rewardViewModel.unlockedBackgrounds,
                            onBuyOrActivate = { theme -> rewardViewModel.buyBackground(theme) }
                        )
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