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
                                val today = java.time.LocalDate.now()

                                if (taskToToggle.status == TaskStatus.DONE) {
                                    // Décocher → repasse en TODO
                                    // Supprimer la prochaine occurrence SEULEMENT si elle est encore dans le futur
                                    // (si elle est pour aujourd'hui ou avant, elle est déjà "arrivée" → on la garde)
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
                                    // Cocher
                                    if (taskToToggle.recurrence != RecurrenceType.NONE
                                        && taskToToggle.deadlineDate != null
                                        && !taskToToggle.deadlineDate.isAfter(today)) {
                                        // Tâche récurrente dont la date est passée/aujourd'hui
                                        val nextDate = when (taskToToggle.recurrence) {
                                            RecurrenceType.DAILY -> taskToToggle.deadlineDate.plusDays(1)
                                            RecurrenceType.WEEKLY -> taskToToggle.deadlineDate.plusWeeks(1)
                                            RecurrenceType.MONTHLY -> taskToToggle.deadlineDate.plusMonths(1)
                                            else -> null
                                        }
                                        if (nextDate != null) {
                                            // Vérifier qu'une occurrence pour cette date n'existe pas déjà
                                            // (peu importe son statut DONE/TODO)
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
                                                    nextOccurrenceId = null
                                                )
                                                tasks = tasks.map { task ->
                                                    if (task.id == taskToToggle.id)
                                                        task.copy(status = TaskStatus.DONE, nextOccurrenceId = nextId)
                                                    else task
                                                } + nextOccurrence
                                            } else {
                                                // L'occurrence existe déjà, on coche juste la tâche actuelle
                                                tasks = tasks.map { task ->
                                                    if (task.id == taskToToggle.id)
                                                        task.copy(status = TaskStatus.DONE, nextOccurrenceId = null)
                                                    else task
                                                }
                                            }
                                        } else {
                                            tasks = tasks.map { task ->
                                                if (task.id == taskToToggle.id)
                                                    task.copy(status = TaskStatus.DONE)
                                                else task
                                            }
                                        }
                                    } else {
                                        // Tâche normale ou récurrente dans le futur → DONE simplement
                                        tasks = tasks.map { task ->
                                            if (task.id == taskToToggle.id)
                                                task.copy(status = TaskStatus.DONE)
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
                            }
                        )
                    }
                    Screen.CreateTask -> {
                        CreateTaskScreen(
                            onNavigateBack = {
                                currentScreen = Screen.Home
                            },
                            onTaskCreated = { title, dateStr, timeStr, priority, recurrence ->
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
                                    id = if (tasks.isEmpty()) 1 else tasks.maxOf { it.id } + 1,
                                    title = title,
                                    description = "",
                                    deadlineDate = deadlineDate,
                                    deadlineTime = deadlineTime,
                                    status = TaskStatus.TODO,
                                    priority = priority,
                                    recurrence = recurrence
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
                                initialPriority = taskToEdit.priority,
                                initialDeadlineDate = initialDateStr,
                                initialDeadlineTime = initialTimeStr,
                                initialRecurrence = taskToEdit.recurrence,
                                onNavigateBack = {
                                    currentScreen = Screen.Home
                                    editingTaskId = null
                                },
                                onTaskUpdated = { newTitle, dateStr, timeStr, newPriority, newRecurrence ->
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
                                                priority = newPriority,
                                                deadlineDate = deadlineDate,
                                                deadlineTime = deadlineTime,
                                                recurrence = newRecurrence
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