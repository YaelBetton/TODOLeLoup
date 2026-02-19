package com.example.todoleloup.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todoleloup.ui.theme.irishGroverFont
import com.example.todoleloup.ui.theme.*
import java.time.LocalDate
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
    onTaskCreated: (String, String, String, Boolean) -> Unit
) {
    var taskTitle by remember { mutableStateOf("") }
    var dueDateText by remember { mutableStateOf("") }
    var dueTimeText by remember { mutableStateOf("") }
    var isUrgent by remember { mutableStateOf(false) }

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

                Text(
                    text = "TITRE",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = taskTitle,
                    onValueChange = { taskTitle = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = "Ex: Faire les courses...",
                            color = TextSecondary
                        )
                    },
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
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "DATE LIMITE",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = dueDateText,
                            onValueChange = { dueDateText = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(text = "jj/mm/aaaa", color = TextSecondary)
                            },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = "Date",
                                    tint = TextSecondary
                                )
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

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "HEURE",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = dueTimeText,
                            onValueChange = { dueTimeText = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(text = "--:--", color = TextSecondary)
                            },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = "Heure",
                                    tint = TextSecondary
                                )
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

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = isUrgent,
                        onCheckedChange = { isUrgent = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = CyanPrimary,
                            uncheckedColor = TextSecondary,
                            checkmarkColor = Color.Black
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Marquer comme Urgent",
                        color = TextPrimary,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
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
                            if (taskTitle.isNotBlank() && dueDateText.isNotBlank() && isValidDate(dueDateText) && dueTimeText.isNotBlank() && isValidTime(dueTimeText)) {
                                onTaskCreated(taskTitle, dueDateText, dueTimeText, isUrgent)
                                onNavigateBack()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyanPrimary,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(14.dp),
                        enabled = taskTitle.isNotBlank() && dueDateText.isNotBlank() && isValidDate(dueDateText) && dueTimeText.isNotBlank() && isValidTime(dueTimeText)
                    ) {
                        Text(text = "Ajouter", textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}
