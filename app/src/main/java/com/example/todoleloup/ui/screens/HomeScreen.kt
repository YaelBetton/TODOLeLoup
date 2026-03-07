package com.example.todoleloup.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todoleloup.R
import com.example.todoleloup.data.Priority
import com.example.todoleloup.data.RecurrenceType
import com.example.todoleloup.data.Task
import com.example.todoleloup.data.TaskStatus
import com.example.todoleloup.ui.theme.*
import com.example.todoleloup.ui.theme.irishGroverFont
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.max
import kotlin.random.Random

@Composable
fun HomeScreen(
    onNavigateToCreateTask: () -> Unit,
    tasks: List<Task>,
    onToggleTaskCompleted: (Task) -> Unit,
    onEditTask: (Task) -> Unit,
    onDeleteTask: (Task) -> Unit,
    onClearCompletedTasks: () -> Unit = {}
) {
    var selectedFilter by remember { mutableStateOf(0) }
    var showNotifications by remember { mutableStateOf(true) }
    var celebrateTrigger by remember { mutableStateOf(0) }

    val today = java.time.LocalDate.now()

    // Une tâche est "visible aujourd'hui" si elle est DONE, ou si sa deadline est <= aujourd'hui, ou si elle n'a pas de deadline
    fun Task.isVisibleToday(): Boolean {
        if (status == TaskStatus.DONE) return true
        if (deadlineDate == null) return true
        return !deadlineDate.isAfter(today)
    }

    fun priorityOrder(p: Priority) = when (p) {
        Priority.HIGH -> 0
        Priority.MEDIUM -> 1
        Priority.LOW -> 2
    }

    val filteredTasks = when (selectedFilter) {
        1 -> tasks.filter { it.status == TaskStatus.TODO && it.isVisibleToday() }
            .sortedBy { priorityOrder(it.priority) }
        2 -> tasks.filter { it.isUrgent() }
            .sortedBy { priorityOrder(it.priority) }
        3 -> tasks.filter { it.status == TaskStatus.DONE }
        else -> tasks.filter { it.isVisibleToday() }
            .sortedWith(compareBy({ it.status == TaskStatus.DONE }, { priorityOrder(it.priority) }))
    }

    // Récupérer les tâches en retard
    val overdueTasks = tasks.filter { it.isOverdue() && it.status != TaskStatus.DONE }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Header avec logo, titre et bouton notification
            HeaderSection(
                overdueTasks = overdueTasks,
                onToggleNotifications = { showNotifications = !showNotifications }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Zone de notifications (en dessous du titre)
            if (showNotifications && overdueTasks.isNotEmpty()) {
                NotificationBanner(
                    overdueTasks = overdueTasks
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Barre de recherche
            SearchBar()

            Spacer(modifier = Modifier.height(20.dp))

            // Boutons de filtre
            FilterButtons(
                selectedFilter = selectedFilter,
                onFilterSelected = { selectedFilter = it }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Liste des tâches
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Liste des tâches",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 16.dp),
                    fontFamily = irishGroverFont
                )
                val doneTasks = tasks.filter { it.status == TaskStatus.DONE }
                if (doneTasks.isNotEmpty()) {
                        TextButton(
                        onClick = onClearCompletedTasks,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Effacer les tâches effectuées (${doneTasks.size})",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontFamily = irishGroverFont
                        )
                    }
                }
            }

            if (filteredTasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Aucune tache pour le moment",
                        color = TextSecondary,
                        fontSize = 14.sp,
                        fontFamily = irishGroverFont
                    )
                }
            } else {
                TaskList(
                    tasks = filteredTasks,
                    onToggleTaskCompleted = { task ->
                        // Déclencher l'effet AVANT le toggle, si la tâche va passer en DONE
                        if (task.status != TaskStatus.DONE) {
                            celebrateTrigger += 1
                        }
                        onToggleTaskCompleted(task)
                    },
                    onEditTask = onEditTask,
                    onDeleteTask = onDeleteTask,
                    onTaskCompleted = {}
                )
            }
        }

        // Bouton flottant pour créer une tâche
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomEnd
        ) {
            FloatingActionButton(
                onClick = onNavigateToCreateTask,
                containerColor = CyanPrimary,
                contentColor = Color.Black,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(end = 20.dp, bottom = 8.dp)
                    .size(64.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Ajouter une tâche",
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        TaskCompleteEffect(
            trigger = celebrateTrigger,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun HeaderSection(
    overdueTasks: List<Task>,
    onToggleNotifications: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Logo et titre
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Logo lune
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                     painter = painterResource(id = R.drawable.ic_moon),
                     contentDescription = "Logo lune",
                     tint = CyanPrimary,
                     modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Titre
            Row {
                Text(
                    text = "TODO",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = irishGroverFont
                )
                Text(
                    text = "LeLoup",
                    color = CyanPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = irishGroverFont
                )
            }
        }

        // Bouton notification avec badge
        Box {
            IconButton(
                onClick = onToggleNotifications,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = if (overdueTasks.isNotEmpty()) Color.Red else TextSecondary,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Badge avec le nombre de notifications
            if (overdueTasks.isNotEmpty()) {
                Surface(
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.TopEnd),
                    shape = CircleShape,
                    color = Color.Red
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = overdueTasks.size.toString(),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationBanner(
    overdueTasks: List<Task>
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = DarkSurface,
        border = BorderStroke(2.dp, Color.Red)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // En-tête avec titre
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "●",
                    color = Color.White,
                    fontSize = 12.sp
                )
                Text(
                    text = "NOTIFICATIONS (${overdueTasks.size})",
                    color = Color.Red,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = impactFont
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Liste des tâches en retard
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                overdueTasks.take(3).forEach { task ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "●",
                            color = Color.White,
                            fontSize = 8.sp
                        )
                        Text(
                            text = "En retard : ${task.title}",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontFamily = impactFont
                        )
                    }
                }
            }

            // Afficher "et plus..." si plus de 3 tâches
            if (overdueTasks.size > 3) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "et ${overdueTasks.size - 3} tâche(s) en retard",
                    color = Color.Red,
                    fontSize = 12.sp,
                    fontFamily = irishGroverFont,
                    modifier = Modifier.padding(start = 14.dp)
                )
            }
        }
    }
}

@Composable
fun SearchBar() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        color = DarkSurface
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Rechercher",
                tint = TextSecondary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Rechercher une tâche",
                color = TextSecondary,
                fontSize = 16.sp ,
                fontFamily = irishGroverFont
            )
        }
    }
}

@Composable
fun FilterButtons(selectedFilter: Int, onFilterSelected: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Toutes les tâches
        FilterButton(
            text = "TOUTES LES\nTÂCHES",
            iconResId = R.drawable.ic_footprints,
            isSelected = selectedFilter == 0,
            onClick = { onFilterSelected(0) },
            modifier = Modifier.weight(1f)
        )

        // À faire
        FilterButton(
            text = "À FAIRE",
            iconResId = R.drawable.ic_zap,
            isSelected = selectedFilter == 1,
            onClick = { onFilterSelected(1) },
            modifier = Modifier.weight(1f)
        )
    }

    Spacer(modifier = Modifier.height(12.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Urgent
        FilterButton(
            text = "URGENT",
            iconResId = R.drawable.ic_alarm_clock,
            isSelected = selectedFilter == 2,
            onClick = { onFilterSelected(2) },
            modifier = Modifier.weight(1f)
        )

        // Fait
        FilterButton(
            text = "FAIT",
            iconResId = R.drawable.ic_check_check,
            isSelected = selectedFilter == 3,
            onClick = { onFilterSelected(3) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun FilterButton(
    text: String,
    iconResId: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(64.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) CyanPrimary else DarkSurface,
            contentColor = if (isSelected) Color.Black else TextPrimary
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = iconResId),
                contentDescription = text,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 14.sp,
                fontFamily = impactFont
            )
        }
    }
}

@Composable
fun TaskList(
    tasks: List<Task>,
    onToggleTaskCompleted: (Task) -> Unit,
    onEditTask: (Task) -> Unit,
    onDeleteTask: (Task) -> Unit,
    onTaskCompleted: () -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(tasks) { task ->
            TaskItem(
                task = task,
                onToggleTaskCompleted = onToggleTaskCompleted,
                onEditTask = onEditTask,
                onDeleteTask = onDeleteTask,
                onTaskCompleted = onTaskCompleted
            )
        }
    }
}

@Composable
fun TaskItem(
    task: Task,
    onToggleTaskCompleted: (Task) -> Unit,
    onEditTask: (Task) -> Unit,
    onDeleteTask: (Task) -> Unit,
    onTaskCompleted: () -> Unit
) {
    var isMenuExpanded by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val priorityColor = when (task.priority) {
        Priority.HIGH -> PriorityHigh
        Priority.MEDIUM -> PriorityMedium
        Priority.LOW -> PriorityLow
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        shape = RoundedCornerShape(16.dp),
        color = CardBackground,
        border = if (task.isOverdue()) BorderStroke(2.dp, Color.Red) else null
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Barre de priorité colorée sur le côté gauche
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
                    .background(priorityColor)
            )
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.Transparent)
                    .then(
                        Modifier.padding(2.dp)
                    )
                    .clickable { onToggleTaskCompleted(task) },
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier.size(28.dp),
                    shape = CircleShape,
                    color = if (task.status == TaskStatus.DONE) CyanPrimary else Color.Transparent,
                    border = BorderStroke(2.dp, TextSecondary)
                ) {
                    if (task.status == TaskStatus.DONE) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Tache terminee",
                            tint = Color.Black,
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Texte de la tâche
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Titre + heure sur la même ligne
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = task.title,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = impactFont
                    )
                    if (task.deadlineTime != null) {
                        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
                        Text(
                            text = task.deadlineTime.format(timeFormatter),
                            color = CyanPrimary,
                            fontSize = 13.sp,
                            fontFamily = irishGroverFont
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))

                // Date et Badges sur la même ligne
                Row(
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Affichage de la date uniquement
                    if (task.deadlineDate != null) {
                        val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                        Text(
                            text = task.deadlineDate.format(dateFormatter),
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontFamily = irishGroverFont
                        )
                    }

                    // Badge "FAIT", "DATE PASSÉE" ou "PAS FAIT"
                    if (task.status == TaskStatus.DONE) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.Transparent,
                            border = BorderStroke(2.dp, CyanPrimary),
                            modifier = Modifier.wrapContentWidth()
                        ) {
                            Text(
                                text = "FAIT",
                                color = CyanPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                fontFamily = irishGroverFont
                            )
                        }
                    } else if (task.isOverdue()) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.Transparent,
                            border = BorderStroke(2.dp, Color.Red),
                            modifier = Modifier.wrapContentWidth()
                        ) {
                            Text(
                                text = "EN RETARD",
                                color = Color.Red,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                fontFamily = irishGroverFont
                            )
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.Transparent,
                            border = BorderStroke(2.dp, TextSecondary),
                            modifier = Modifier.wrapContentWidth()
                        ) {
                            Text(
                                text = "PAS FAIT",
                                color = TextSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                fontFamily = irishGroverFont
                            )
                        }
                    }

                    // Badge de périodicité
                    if (task.recurrence != RecurrenceType.NONE) {
                        val recurrenceLabel = when (task.recurrence) {
                            RecurrenceType.DAILY -> "↻ /jour"
                            RecurrenceType.WEEKLY -> "↻ /sem"
                            RecurrenceType.MONTHLY -> "↻ /mois"
                            else -> ""
                        }
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.Transparent,
                            border = BorderStroke(2.dp, CyanPrimary),
                            modifier = Modifier.wrapContentWidth()
                        ) {
                            Text(
                                text = recurrenceLabel,
                                color = CyanPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontFamily = irishGroverFont
                            )
                        }
                    }
                }
            }

            // Menu trois points
            Box {
                IconButton(
                    onClick = { isMenuExpanded = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Plus d'options",
                        tint = TextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                DropdownMenu(
                    expanded = isMenuExpanded,
                    onDismissRequest = { isMenuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Modifier") },
                        onClick = {
                            isMenuExpanded = false
                            onEditTask(task)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Supprimer", color = Color.Red) },
                        onClick = {
                            isMenuExpanded = false
                            showDeleteDialog = true
                        }
                    )
                }
            }

            // Dialogue de confirmation de suppression
            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    containerColor = CardBackground,
                    titleContentColor = Color.White,
                    textContentColor = TextSecondary,
                    title = {
                        Text(
                            text = "Supprimer la tâche ?",
                            fontFamily = impactFont,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Text(
                            text = "\"${task.title}\" sera définitivement supprimée.",
                            fontFamily = irishGroverFont
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showDeleteDialog = false
                                onDeleteTask(task)
                            }
                        ) {
                            Text(
                                text = "Supprimer",
                                color = Color.Red,
                                fontFamily = impactFont,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) {
                            Text(
                                text = "Annuler",
                                color = CyanPrimary,
                                fontFamily = irishGroverFont
                            )
                        }
                    }
                )
            }
        } // fin Row interne
        } // fin Row externe (barre priorité)
    }
}

private enum class ParticleType {
    MOON,
    BONE,
    FOOTPRINTS
}

private data class Particle(
    val xFraction: Float,
    val sizeDp: Dp,
    val type: ParticleType,
    val startOffsetPx: Float,
    val delayFraction: Float
)

@Composable
private fun TaskCompleteEffect(
    trigger: Int,
    modifier: Modifier = Modifier
) {
    var showEffect by remember { mutableStateOf(false) }
    val progress = remember { Animatable(0f) }
    val particles = remember(trigger) {
        List(40) {
            Particle(
                xFraction = Random.nextFloat(),
                sizeDp = (10 + Random.nextInt(18)).dp,
                type = when (Random.nextInt(3)) {
                    0 -> ParticleType.MOON
                    1 -> ParticleType.BONE
                    else -> ParticleType.FOOTPRINTS
                },
                startOffsetPx = Random.nextInt(80, 260).toFloat(),
                delayFraction = Random.nextInt(0, 50) / 100f
            )
        }
    }

    LaunchedEffect(trigger) {
        if (trigger == 0) return@LaunchedEffect
        showEffect = true
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1700, easing = LinearEasing)
        )
        showEffect = false
    }

    if (!showEffect) return

    BoxWithConstraints(modifier = modifier) {
        // 1. On stocke le scope explicitement pour aider l'IDE
        val boxScope = this
        val widthPx = boxScope.constraints.maxWidth.toFloat()
        val heightPx = boxScope.constraints.maxHeight.toFloat()

        particles.forEach { particle ->
            // 2. On remplace max() par coerceAtLeast() qui est beaucoup mieux compris par l'IDE
            val effective =
                ((progress.value - particle.delayFraction) / (1f - particle.delayFraction)).coerceAtLeast(
                    0f
                )

            val x = particle.xFraction * widthPx
            val y = -particle.startOffsetPx + (heightPx + particle.startOffsetPx) * effective

            if (particle.type == ParticleType.FOOTPRINTS || particle.type == ParticleType.BONE) {
                val resId = if (particle.type == ParticleType.BONE) {
                    R.drawable.ic_bone
                } else {
                    R.drawable.ic_footprints
                }
                Image(
                    painter = painterResource(id = resId),
                    contentDescription = null,
                    modifier = Modifier
                        .offset { IntOffset(x.toInt(), y.toInt()) }
                        .size(particle.sizeDp),
                    colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color.White)
                )
            } else {
                Canvas(
                    modifier = Modifier
                        .offset { IntOffset(x.toInt(), y.toInt()) }
                        .size(particle.sizeDp)
                ) {
                    when (particle.type) {
                        ParticleType.MOON -> {
                            val radius = size.minDimension / 2f
                            drawCircle(
                                color = CyanPrimary,
                                radius = radius,
                                center = Offset(radius, radius)
                            )
                            drawCircle(
                                color = DarkBackground,
                                radius = radius * 0.7f,
                                center = Offset(radius * 1.2f, radius * 0.8f)
                            )
                        }

                        else -> Unit
                    }
                }
            }
        }

        val bounce = rememberInfiniteTransition(label = "wolf-bounce").animateFloat(
            initialValue = 0f,
            targetValue = 8f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 700, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "wolf-bounce-offset"
        )

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.offset(y = (-bounce.value).dp)
            ) {
                Box(
                    modifier = Modifier.size(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_wolf),
                        contentDescription = "Loup",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "AOUUUH !",
                    color = CyanPrimary,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = impactFont
                )
                Text(
                    text = "Tache devoree !",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = irishGroverFont
                )
            }
        }
    }
}