package com.example.todoleloup.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todoleloup.R
import com.example.todoleloup.data.Priority
import com.example.todoleloup.data.Task
import com.example.todoleloup.data.TaskStatus
import com.example.todoleloup.ui.theme.*
import com.example.todoleloup.ui.theme.irishGroverFont
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(
    onNavigateToCreateTask: () -> Unit,
    tasks: List<Task>,
    onToggleTaskCompleted: (Task) -> Unit,
    onEditTask: (Task) -> Unit
) {
    var selectedFilter by remember { mutableStateOf(0) }

    val filteredTasks = when (selectedFilter) {
        1 -> tasks.filter { it.status == TaskStatus.TODO }
        2 -> tasks.filter { it.isUrgent() }
        3 -> tasks.filter { it.status == TaskStatus.DONE }
        else -> tasks
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Header avec logo et titre
        HeaderSection()

        Spacer(modifier = Modifier.height(24.dp))

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
        Text(
            text = "Liste des tâches",
            color = TextSecondary,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 16.dp),
            fontFamily = irishGroverFont
        )

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
                onToggleTaskCompleted = onToggleTaskCompleted,
                onEditTask = onEditTask
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
                .padding(end = 20.dp, bottom = 90.dp)
                .size(64.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Ajouter une tâche",
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
fun HeaderSection() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
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
    onEditTask: (Task) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(tasks) { task ->
            TaskItem(
                task = task,
                onToggleTaskCompleted = onToggleTaskCompleted,
                onEditTask = onEditTask
            )
        }
    }
}

@Composable
fun TaskItem(
    task: Task,
    onToggleTaskCompleted: (Task) -> Unit,
    onEditTask: (Task) -> Unit
) {
    var isMenuExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        shape = RoundedCornerShape(16.dp),
        color = CardBackground
    ) {
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
                Text(
                    text = task.title,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = impactFont
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Date et Badges sur la même ligne
                Row(
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Affichage de la date et heure
                    if (task.deadlineDate != null || task.deadlineTime != null) {
                        val dateStr = if (task.deadlineDate != null && task.deadlineTime != null) {
                            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                            val dateTime = LocalDateTime.of(task.deadlineDate!!, task.deadlineTime!!)
                            dateTime.format(formatter)
                        } else if (task.deadlineDate != null) {
                            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                            task.deadlineDate?.format(formatter)
                        } else {
                            val formatter = DateTimeFormatter.ofPattern("HH:mm")
                            task.deadlineTime?.format(formatter)
                        }

                        if (dateStr != null) {
                            Text(
                                text = dateStr,
                                color = TextSecondary,
                                fontSize = 12.sp,
                                fontFamily = irishGroverFont
                            )
                        }
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
                                text = "DATE PASSÉE",
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
                }
            }
        }
    }
}
