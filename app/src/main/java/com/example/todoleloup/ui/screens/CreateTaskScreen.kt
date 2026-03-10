package com.example.todoleloup.ui.screens

import android.Manifest
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.todoleloup.data.Priority
import com.example.todoleloup.data.RecurrenceType
import com.example.todoleloup.ui.theme.irishGroverFont
import com.example.todoleloup.ui.theme.*
import java.io.File
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

fun createImageUri(context: Context): Uri {
    val file = File(context.externalCacheDir, "photo_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}

fun copyUriToCache(context: Context, uri: Uri): Uri {
    val dest = File(context.externalCacheDir, "gallery_${System.currentTimeMillis()}.jpg")
    try {
        context.contentResolver.openInputStream(uri)?.use { input ->
            dest.outputStream().use { output -> input.copyTo(output) }
        }
    } catch (e: Exception) {
        return uri // fallback sur l'URI original
    }
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", dest)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTaskScreen(
    onNavigateBack: () -> Unit,
    onTaskCreated: (String, String, String, String, Priority, RecurrenceType, String?) -> Unit
) {
    val context = LocalContext.current
    var taskTitle by remember { mutableStateOf("") }
    var taskDescription by remember { mutableStateOf("") }
    var dueDateText by remember { mutableStateOf("") }
    var dueTimeText by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf(Priority.MEDIUM) }
    var selectedRecurrence by remember { mutableStateOf(RecurrenceType.NONE) }
    var recurrenceMenuExpanded by remember { mutableStateOf(false) }
    var selectedPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var cameraUri by remember { mutableStateOf<Uri?>(null) }
    var showPhotoMenuExpanded by remember { mutableStateOf(false) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    // Launcher galerie
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedPhotoUri = copyUriToCache(context, uri)
        }
    }

    // Launcher caméra
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success -> if (success && cameraUri != null) selectedPhotoUri = cameraUri }

    // Permission caméra
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = createImageUri(context)
            cameraUri = uri
            cameraLauncher.launch(uri)
        }
    }

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
                        val date = Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).toLocalDate()
                        dueDateText = date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    }
                    showDatePicker = false
                }) { Text("OK", color = CyanPrimary, fontFamily = irishGroverFont) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Annuler", color = TextSecondary, fontFamily = irishGroverFont)
                }
            }
        ) { DatePicker(state = datePickerState) }
    }

    // TimePickerDialog Material3
    if (showTimePicker) {
        val initialHour = if (isValidTime(dueTimeText)) dueTimeText.split(":")[0].toInt() else java.time.LocalTime.now().hour
        val initialMinute = if (isValidTime(dueTimeText)) dueTimeText.split(":")[1].toInt() else 0
        val timePickerState = rememberTimePickerState(initialHour = initialHour, initialMinute = initialMinute, is24Hour = true)
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
            text = { TimePicker(state = timePickerState) }
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
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(text = "Nouvelle Tache", color = TextPrimary, fontSize = 22.sp, fontFamily = irishGroverFont)

                Spacer(modifier = Modifier.height(16.dp))

                // TITRE
                Text(text = "TITRE", color = TextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = taskTitle,
                    onValueChange = { taskTitle = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(text = "Ex: Faire les courses...", color = TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanPrimary, unfocusedBorderColor = TextSecondary,
                        focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, cursorColor = CyanPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // DESCRIPTION
                Text(text = "DESCRIPTION", color = TextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = taskDescription,
                    onValueChange = { taskDescription = it },
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    placeholder = { Text(text = "Détails optionnels...", color = TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanPrimary, unfocusedBorderColor = TextSecondary,
                        focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, cursorColor = CyanPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(12.dp))

                // PHOTO
                Text(text = "PHOTO", color = TextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))
                if (selectedPhotoUri != null) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        coil.compose.AsyncImage(
                            model = coil.request.ImageRequest.Builder(context)
                                .data(selectedPhotoUri)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Photo jointe",
                            contentScale = ContentScale.Inside,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 100.dp, max = 400.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, CyanPrimary, RoundedCornerShape(12.dp))
                        )
                        IconButton(
                            onClick = { selectedPhotoUri = null },
                            modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)
                        ) {
                            Surface(shape = RoundedCornerShape(50), color = Color.Black.copy(alpha = 0.6f)) {
                                Icon(Icons.Default.Close, contentDescription = "Supprimer photo",
                                    tint = Color.White, modifier = Modifier.padding(4.dp).size(20.dp))
                            }
                        }
                    }
                } else {
                    Box {
                        OutlinedButton(
                            onClick = { showPhotoMenuExpanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                            border = androidx.compose.foundation.BorderStroke(1.dp, TextSecondary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Ajouter une photo", fontFamily = irishGroverFont, modifier = Modifier.weight(1f))
                            Text(text = "▾", color = TextSecondary)
                        }
                        DropdownMenu(expanded = showPhotoMenuExpanded, onDismissRequest = { showPhotoMenuExpanded = false }) {
                            DropdownMenuItem(
                                text = { Text("📷  Prendre une photo", fontFamily = irishGroverFont) },
                                onClick = {
                                    showPhotoMenuExpanded = false
                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("🖼️  Choisir depuis la galerie", fontFamily = irishGroverFont) },
                                onClick = {
                                    showPhotoMenuExpanded = false
                                    galleryLauncher.launch("image/*")
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // DATE + HEURE
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "DATE LIMITE", color = TextSecondary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = dueDateText, onValueChange = { dueDateText = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text(text = "jj/mm/aaaa", color = TextSecondary) },
                            trailingIcon = {
                                IconButton(onClick = { showDatePicker = true }) {
                                    Icon(Icons.Default.DateRange, contentDescription = "Calendrier", tint = CyanPrimary)
                                }
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanPrimary, unfocusedBorderColor = TextSecondary,
                                focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, cursorColor = CyanPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "HEURE", color = TextSecondary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = dueTimeText, onValueChange = { dueTimeText = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text(text = "--:--", color = TextSecondary) },
                            trailingIcon = {
                                IconButton(onClick = { showTimePicker = true }) {
                                    Icon(Icons.Default.AccessTime, contentDescription = "Heure", tint = CyanPrimary)
                                }
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanPrimary, unfocusedBorderColor = TextSecondary,
                                focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, cursorColor = CyanPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // PRIORITÉ
                Text(text = "PRIORITÉ", color = TextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(Priority.LOW to "Normale", Priority.MEDIUM to "Haute", Priority.HIGH to "Critique")
                        .forEach { (priority, label) ->
                            val color = when (priority) { Priority.LOW -> PriorityLow; Priority.MEDIUM -> PriorityMedium; Priority.HIGH -> PriorityHigh }
                            Button(
                                onClick = { selectedPriority = priority },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selectedPriority == priority) color else DarkSurface,
                                    contentColor = if (selectedPriority == priority) Color.White else TextSecondary
                                ),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 10.dp)
                            ) { Text(text = label, fontSize = 12.sp, fontFamily = irishGroverFont) }
                        }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // PÉRIODICITÉ
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
                                RecurrenceType.NONE -> "Aucune"; RecurrenceType.DAILY -> "Quotidienne"
                                RecurrenceType.WEEKLY -> "Hebdomadaire"; RecurrenceType.MONTHLY -> "Mensuelle"
                            },
                            fontFamily = irishGroverFont, modifier = Modifier.weight(1f)
                        )
                        Text(text = "▾", color = TextSecondary)
                    }
                    DropdownMenu(expanded = recurrenceMenuExpanded, onDismissRequest = { recurrenceMenuExpanded = false }) {
                        listOf(RecurrenceType.NONE to "Aucune", RecurrenceType.DAILY to "Quotidienne",
                            RecurrenceType.WEEKLY to "Hebdomadaire", RecurrenceType.MONTHLY to "Mensuelle"
                        ).forEach { (type, label) ->
                            DropdownMenuItem(
                                text = { Text(label, fontFamily = irishGroverFont) },
                                onClick = { selectedRecurrence = type; recurrenceMenuExpanded = false }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = onNavigateBack,
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurface, contentColor = TextPrimary),
                        shape = RoundedCornerShape(14.dp)
                    ) { Text(text = "Annuler", textAlign = TextAlign.Center) }
                    Button(
                        onClick = {
                            if (taskTitle.isNotBlank() && dueDateText.isNotBlank() && isValidDate(dueDateText)) {
                                val isTimeValid = dueTimeText.isBlank() || isValidTime(dueTimeText)
                                if (isTimeValid) {
                                    onTaskCreated(taskTitle, taskDescription, dueDateText, dueTimeText, selectedPriority, selectedRecurrence, selectedPhotoUri?.toString())
                                    onNavigateBack()
                                }
                            }
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color.Black),
                        shape = RoundedCornerShape(14.dp),
                        enabled = taskTitle.isNotBlank() && dueDateText.isNotBlank() && isValidDate(dueDateText) && (dueTimeText.isBlank() || isValidTime(dueTimeText))
                    ) { Text(text = "Ajouter", textAlign = TextAlign.Center) }
                }
            }
        }
    }
}
