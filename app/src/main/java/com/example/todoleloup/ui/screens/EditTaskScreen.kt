package com.example.todoleloup.ui.screens

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todoleloup.data.Priority
import com.example.todoleloup.data.RecurrenceType
import com.example.todoleloup.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTaskScreen(
    initialTitle: String,
    initialDescription: String = "",
    initialPriority: Priority = Priority.MEDIUM,
    initialDeadlineDate: String,
    initialDeadlineTime: String,
    initialRecurrence: RecurrenceType = RecurrenceType.NONE,
    initialPhotoUri: String? = null,
    onNavigateBack: () -> Unit,
    onTaskUpdated: (String, String, String, String, Priority, RecurrenceType, String?) -> Unit,
) {
    val context = LocalContext.current

    var taskTitle by remember { mutableStateOf(initialTitle) }
    var taskDescription by remember { mutableStateOf(initialDescription) }
    var dueDateText by remember { mutableStateOf(initialDeadlineDate) }
    var dueTimeText by remember { mutableStateOf(initialDeadlineTime) }
    var selectedPriority by remember { mutableStateOf(initialPriority) }
    var selectedRecurrence by remember { mutableStateOf(initialRecurrence) }
    var recurrenceMenuExpanded by remember { mutableStateOf(false) }
    var selectedPhotoUri by remember { mutableStateOf<Uri?>(initialPhotoUri?.let(Uri::parse)) }
    var cameraUri by remember { mutableStateOf<Uri?>(null) }
    var photoMenuExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { selectedPhotoUri = copyUriToCache(context, it) }
    }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) selectedPhotoUri = cameraUri
    }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            createCameraUri(context).also { cameraUri = it; cameraLauncher.launch(it) }
        }
    }

    val isFormValid = taskTitle.isNotBlank()
        && dueDateText.isNotBlank()
        && isValidDate(dueDateText)
        && (dueTimeText.isBlank() || isValidTime(dueTimeText))

    if (showDatePicker) {
        TaskDatePickerDialog(
            currentDateText = dueDateText,
            onDismiss = { showDatePicker = false },
            onConfirm = { dueDateText = it }
        )
    }
    if (showTimePicker) {
        TaskTimePickerDialog(
            currentTimeText = dueTimeText,
            onDismiss = { showTimePicker = false },
            onConfirm = { dueTimeText = it }
        )
    }

    Box(
        modifier = Modifier.fillMaxSize().background(DarkBackground),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.padding(20.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text("Modifier Tache", color = TextPrimary, fontSize = 22.sp, fontFamily = irishGroverFont)
                Spacer(Modifier.height(16.dp))

                SectionLabel("TITRE")
                OutlinedTextField(
                    value = taskTitle,
                    onValueChange = { taskTitle = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Ex: Tuer le dragon...", color = TextSecondary) },
                    colors = taskFieldColors(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(Modifier.height(12.dp))

                SectionLabel("DESCRIPTION")
                OutlinedTextField(
                    value = taskDescription,
                    onValueChange = { taskDescription = it },
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    placeholder = { Text("Détails optionnels...", color = TextSecondary) },
                    colors = taskFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 3
                )
                Spacer(Modifier.height(12.dp))

                SectionLabel("PHOTO")
                PhotoSection(
                    photoUri = selectedPhotoUri,
                    menuExpanded = photoMenuExpanded,
                    onMenuExpandChange = { photoMenuExpanded = it },
                    onRemovePhoto = { selectedPhotoUri = null },
                    onPickFromGallery = { galleryLauncher.launch("image/*") },
                    onTakePhoto = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) }
                )
                Spacer(Modifier.height(12.dp))

                DateTimeRow(
                    dueDateText = dueDateText,
                    dueTimeText = dueTimeText,
                    onDateChange = { dueDateText = it },
                    onTimeChange = { dueTimeText = it },
                    onOpenDatePicker = { showDatePicker = true },
                    onOpenTimePicker = { showTimePicker = true }
                )
                Spacer(Modifier.height(12.dp))

                SectionLabel("PRIORITÉ")
                PrioritySelector(selected = selectedPriority, onSelect = { selectedPriority = it })
                Spacer(Modifier.height(12.dp))

                SectionLabel("PÉRIODICITÉ")
                RecurrenceSelector(
                    selected = selectedRecurrence,
                    expanded = recurrenceMenuExpanded,
                    onExpandChange = { recurrenceMenuExpanded = it },
                    onSelect = { selectedRecurrence = it }
                )
                Spacer(Modifier.height(16.dp))

                FormActionButtons(
                    confirmLabel = "Enregistrer",
                    confirmEnabled = isFormValid,
                    onCancel = onNavigateBack,
                    onConfirm = {
                        if (isFormValid) {
                            onTaskUpdated(
                                taskTitle, taskDescription,
                                dueDateText, dueTimeText,
                                selectedPriority, selectedRecurrence,
                                selectedPhotoUri?.toString()
                            )
                            onNavigateBack()
                        }
                    }
                )
            }
        }
    }
}
