package com.example.todoleloup.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todoleloup.data.Priority
import com.example.todoleloup.data.RecurrenceType
import com.example.todoleloup.ui.theme.irishGroverFont
import com.example.todoleloup.ui.theme.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// Fonction pour valider le format de la date
fun isValidDate(dateStr: String): Boolean {
    return try {
        val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        LocalDate.parse(dateStr, dateFormatter)
        true
    } catch (e: Exception) {
        false
    }
}

// Fonction pour valider le format de l'heure
fun isValidTime(timeStr: String): Boolean {
    return try {
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        java.time.LocalTime.parse(timeStr, timeFormatter)
        true
    } catch (e: Exception) {
        false
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTaskScreen(
    onNavigateBack: () -> Unit,
    onTaskCreated: (String, String, String, Priority, RecurrenceType) -> Unit
) {
    var taskTitle by remember { mutableStateOf("") }
    var dueDateText by remember { mutableStateOf("") }
    var dueTimeText by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf(Priority.MEDIUM) }
    var selectedRecurrence by remember { mutableStateOf(RecurrenceType.NONE) }
    var recurrenceMenuExpanded by remember { mutableStateOf(false) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    // DatePickerDialog Material3
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = if (isValidDate(dueDateText)) {
                val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                LocalDate.parse(dueDateText, formatter)
                    .atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
            } else {
                System.currentTimeMillis()
            }
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.of("UTC")).toLocalDate()
                        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                        dueDateText = date.format(formatter)
                    }
                    showDatePicker = false
                }) { Text("OK", color = CyanPrimary, fontFamily = irishGroverFont) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Annuler", color = TextSecondary, fontFamily = irishGroverFont)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // TimePickerDialog Material3
    if (showTimePicker) {
        val initialHour = if (isValidTime(dueTimeText)) dueTimeText.split(":")[0].toInt() else java.time.LocalTime.now().hour
        val initialMinute = if (isValidTime(dueTimeText)) dueTimeText.split(":")[1].toInt() else 0
        val timePickerState = rememberTimePickerState(
            initialHour = initialHour,
            initialMinute = initialMinute,
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dueTimeText = "%02d:%02d".format(timePickerState.hour, timePickerState.minute)
                    showTimePicker = false
                }) { Text("OK", color = CyanPrimary, fontFamily = irishGroverFont) }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Annuler", color = TextSecondary, fontFamily = irishGroverFont)
                }
            },
            title = { Text("Choisir l'heure", color = TextPrimary, fontFamily = irishGroverFont) },
            containerColor = CardBackground,
            text = {
                TimePicker(state = timePickerState)
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Card(
            modifier = Modifier
                .padding(20.dp)
                .align(Alignment.Center),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Nouvelle Tache",
                    color = TextPrimary,
                    fontSize = 22.sp,
                    fontFamily = irishGroverFont
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(text = "TITRE", color = TextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = taskTitle,
                    onValueChange = { taskTitle = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(text = "Ex: Faire les courses...", color = TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanPrimary,
                        unfocusedBorderColor = TextSecondary,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = CyanPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // DATE
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "DATE LIMITE", color = TextSecondary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = dueDateText,
                            onValueChange = { dueDateText = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text(text = "jj/mm/aaaa", color = TextSecondary) },
                            trailingIcon = {
                                IconButton(onClick = { showDatePicker = true }) {
                                    Icon(
                                        imageVector = Icons.Default.DateRange,
                                        contentDescription = "Ouvrir calendrier",
                                        tint = CyanPrimary
                                    )
                                }
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanPrimary,
                                unfocusedBorderColor = TextSecondary,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                cursorColor = CyanPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    // HEURE
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "HEURE", color = TextSecondary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = dueTimeText,
                            onValueChange = { dueTimeText = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text(text = "--:--", color = TextSecondary) },
                            trailingIcon = {
                                IconButton(onClick = { showTimePicker = true }) {
                                    Icon(
                                        imageVector = Icons.Default.AccessTime,
                                        contentDescription = "Ouvrir horloge",
                                        tint = CyanPrimary
                                    )
                                }
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanPrimary,
                                unfocusedBorderColor = TextSecondary,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                cursorColor = CyanPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(text = "PRIORITÉ", color = TextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        Priority.LOW to "Normale",
                        Priority.MEDIUM to "Haute",
                        Priority.HIGH to "Critique"
                    ).forEach { (priority, label) ->
                        val color = when (priority) {
                            Priority.LOW -> PriorityLow
                            Priority.MEDIUM -> PriorityMedium
                            Priority.HIGH -> PriorityHigh
                        }
                        Button(
                            onClick = { selectedPriority = priority },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedPriority == priority) color else DarkSurface,
                                contentColor = if (selectedPriority == priority) Color.White else TextSecondary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 10.dp)
                        ) {
                            Text(text = label, fontSize = 12.sp, fontFamily = irishGroverFont)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(text = "PÉRIODICITÉ", color = TextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Box {
                    OutlinedButton(
                        onClick = { recurrenceMenuExpanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, TextSecondary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = when (selectedRecurrence) {
                                RecurrenceType.NONE -> "Aucune"
                                RecurrenceType.DAILY -> "Quotidienne"
                                RecurrenceType.WEEKLY -> "Hebdomadaire"
                                RecurrenceType.MONTHLY -> "Mensuelle"
                            },
                            fontFamily = irishGroverFont,
                            modifier = Modifier.weight(1f)
                        )
                        Text(text = "▾", color = TextSecondary)
                    }
                    DropdownMenu(
                        expanded = recurrenceMenuExpanded,
                        onDismissRequest = { recurrenceMenuExpanded = false }
                    ) {
                        listOf(
                            RecurrenceType.NONE to "Aucune",
                            RecurrenceType.DAILY to "Quotidienne",
                            RecurrenceType.WEEKLY to "Hebdomadaire",
                            RecurrenceType.MONTHLY to "Mensuelle"
                        ).forEach { (type, label) ->
                            DropdownMenuItem(
                                text = { Text(label, fontFamily = irishGroverFont) },
                                onClick = {
                                    selectedRecurrence = type
                                    recurrenceMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onNavigateBack,
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DarkSurface,
                            contentColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(text = "Annuler", textAlign = TextAlign.Center)
                    }
                    Button(
                        onClick = {
                            if (taskTitle.isNotBlank() && dueDateText.isNotBlank() && isValidDate(dueDateText)) {
                                val isTimeValid = dueTimeText.isBlank() || isValidTime(dueTimeText)
                                if (isTimeValid) {
                                    onTaskCreated(taskTitle, dueDateText, dueTimeText, selectedPriority, selectedRecurrence)
                                    onNavigateBack()
                                }
                            }
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyanPrimary,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(14.dp),
                        enabled = taskTitle.isNotBlank() && dueDateText.isNotBlank() && isValidDate(dueDateText) && (dueTimeText.isBlank() || isValidTime(dueTimeText))
                    ) {
                        Text(text = "Ajouter", textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}
