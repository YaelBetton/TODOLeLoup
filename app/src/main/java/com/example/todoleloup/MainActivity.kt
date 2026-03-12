package com.example.todoleloup

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.todoleloup.data.RecurrenceType
import com.example.todoleloup.data.Task
import com.example.todoleloup.data.TaskStatus
import com.example.todoleloup.data.pointsForPriority
import com.example.todoleloup.notification.NotificationScheduler
import com.example.todoleloup.ui.navigation.Screen
import com.example.todoleloup.ui.screens.*
import com.example.todoleloup.ui.theme.*
import java.time.LocalDate

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* résultat ignoré */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Demander la permission POST_NOTIFICATIONS sur Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            TodoLeLoupTheme {
                TodoLeLoupApp()
            }
        }
    }
}

// ── Logique métier toggle ─────────────────────────────────────────────────────

private fun toggleTaskDone(
    tasks: List<Task>,
    taskToToggle: Task,
    onAddPoints: (Int) -> Unit,
): List<Task> {
    val today = LocalDate.now()
    return if (taskToToggle.status == TaskStatus.DONE) {
        // Décocher → repasse en TODO, supprime la prochaine occurrence future si elle existe
        val nextOccurrence = tasks.find { it.id == taskToToggle.nextOccurrenceId }
        val shouldDeleteNext = nextOccurrence?.deadlineDate?.isAfter(today) == true
        tasks
            .filter { !shouldDeleteNext || it.id != taskToToggle.nextOccurrenceId }
            .map { if (it.id == taskToToggle.id) it.copy(status = TaskStatus.TODO, nextOccurrenceId = null) else it }
    } else {
        // Cocher → points + éventuelle récurrence
        if (!taskToToggle.rewardClaimed) {
            onAddPoints(pointsForPriority(taskToToggle.priority))
        }
        val hasRecurrence = taskToToggle.recurrence != RecurrenceType.NONE
            && taskToToggle.deadlineDate != null
            && !taskToToggle.deadlineDate.isAfter(today)

        if (hasRecurrence) {
            val nextDate = when (taskToToggle.recurrence) {
                RecurrenceType.DAILY -> taskToToggle.deadlineDate!!.plusDays(1)
                RecurrenceType.WEEKLY -> taskToToggle.deadlineDate!!.plusWeeks(1)
                RecurrenceType.MONTHLY -> taskToToggle.deadlineDate!!.plusMonths(1)
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
                    tasks.map {
                        if (it.id == taskToToggle.id) it.copy(status = TaskStatus.DONE, nextOccurrenceId = nextId, rewardClaimed = true)
                        else it
                    } + nextOccurrence
                } else {
                    tasks.map {
                        if (it.id == taskToToggle.id) it.copy(status = TaskStatus.DONE, nextOccurrenceId = null, rewardClaimed = true)
                        else it
                    }
                }
            } else {
                tasks.map { if (it.id == taskToToggle.id) it.copy(status = TaskStatus.DONE, rewardClaimed = true) else it }
            }
        } else {
            tasks.map { if (it.id == taskToToggle.id) it.copy(status = TaskStatus.DONE, rewardClaimed = true) else it }
        }
    }
}

// ── App principale ────────────────────────────────────────────────────────────

@Composable
fun TodoLeLoupApp() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    var selectedTab by remember { mutableStateOf(0) }
    var tasks by remember { mutableStateOf(listOf<Task>()) }
    var editingTaskId by remember { mutableStateOf<Int?>(null) }
    val rewardViewModel: RewardViewModel = viewModel()
    val context = LocalContext.current

    val nextTaskId by derivedStateOf { if (tasks.isEmpty()) 1 else tasks.maxOf { it.id } + 1 }

    // Reprogrammer toutes les alarmes quand la liste de tâches change
    LaunchedEffect(tasks) {
        NotificationScheduler.rescheduleAll(context, tasks)
    }

    Surface(modifier = Modifier.fillMaxSize(), color = DarkBackground) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = DarkBackground,
            bottomBar = {
                if (currentScreen == Screen.Home || currentScreen == Screen.Shop) {
                    BottomNavigationBar(selectedTab = selectedTab) {
                        selectedTab = it
                        currentScreen = if (it == 1) Screen.Shop else Screen.Home
                    }
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                when (currentScreen) {
                    Screen.Home -> HomeScreen(
                        tasks = tasks,
                        points = rewardViewModel.points,
                        lastEarnedPoints = rewardViewModel.lastEarnedPoints,
                        activeBackground = rewardViewModel.activeBackground,
                        onNavigateToCreateTask = { currentScreen = Screen.CreateTask },
                        onToggleTaskCompleted = { task ->
                            tasks = toggleTaskDone(tasks, task) { rewardViewModel.addPoints(it) }
                        },
                        onEditTask = { task -> editingTaskId = task.id; currentScreen = Screen.EditTask },
                        onDeleteTask = { task -> tasks = tasks.filter { it.id != task.id } },
                        onClearCompletedTasks = { tasks = tasks.filter { it.status != TaskStatus.DONE } },
                        onClearLastEarned = { rewardViewModel.clearLastEarned() }
                    )

                    Screen.CreateTask -> CreateTaskScreen(
                        onNavigateBack = { currentScreen = Screen.Home },
                        onTaskCreated = { title, description, dateStr, timeStr, priority, recurrence, photoUri ->
                            tasks = tasks + Task(
                                id = nextTaskId,
                                title = title,
                                description = description,
                                deadlineDate = parseDate(dateStr),
                                deadlineTime = parseTime(timeStr),
                                status = TaskStatus.TODO,
                                priority = priority,
                                recurrence = recurrence,
                                photoUri = photoUri
                            )
                        }
                    )

                    Screen.EditTask -> {
                        val taskToEdit = tasks.find { it.id == editingTaskId }
                        LaunchedEffect(taskToEdit) {
                            if (taskToEdit == null) { currentScreen = Screen.Home; editingTaskId = null }
                        }
                        if (taskToEdit != null) {
                            EditTaskScreen(
                                initialTitle = taskToEdit.title,
                                initialDescription = taskToEdit.description,
                                initialPriority = taskToEdit.priority,
                                initialDeadlineDate = taskToEdit.deadlineDate?.let { formatDate(it) } ?: "",
                                initialDeadlineTime = taskToEdit.deadlineTime?.let { formatTime(it) } ?: "",
                                initialRecurrence = taskToEdit.recurrence,
                                initialPhotoUri = taskToEdit.photoUri,
                                onNavigateBack = { currentScreen = Screen.Home; editingTaskId = null },
                                onTaskUpdated = { newTitle, newDesc, dateStr, timeStr, newPriority, newRecurrence, newPhotoUri ->
                                    tasks = tasks.map { task ->
                                        if (task.id == taskToEdit.id) task.copy(
                                            title = newTitle,
                                            description = newDesc,
                                            priority = newPriority,
                                            deadlineDate = parseDate(dateStr) ?: taskToEdit.deadlineDate,
                                            deadlineTime = parseTime(timeStr) ?: taskToEdit.deadlineTime,
                                            recurrence = newRecurrence,
                                            photoUri = newPhotoUri
                                        ) else task
                                    }
                                }
                            )
                        }
                    }

                    Screen.Shop -> ShopScreen(
                        points = rewardViewModel.points,
                        activeBackground = rewardViewModel.activeBackground,
                        unlockedBackgrounds = rewardViewModel.unlockedBackgrounds,
                        onBuyOrActivate = { rewardViewModel.buyBackground(it) }
                    )
                }
            }
        }
    }
}

// ── Barre de navigation ───────────────────────────────────────────────────────

@Composable
fun BottomNavigationBar(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().height(72.dp),
        color = DarkSurface
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onTabSelected(0) }) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Accueil",
                    tint = if (selectedTab == 0) CyanPrimary else TextSecondary,
                    modifier = Modifier.size(28.dp)
                )
            }
            IconButton(onClick = { onTabSelected(1) }) {
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