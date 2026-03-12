package com.example.todoleloup.ui.screens

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.todoleloup.data.Priority
import com.example.todoleloup.data.RecurrenceType
import com.example.todoleloup.ui.theme.*
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// ── Constantes ───────────────────────────────────────────────────────────────

private val DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy")
private val TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm")

// ── Fonctions utilitaires ─────────────────────────────────────────────────────

fun isValidDate(dateStr: String): Boolean =
    runCatching { LocalDate.parse(dateStr, DATE_FORMATTER) }.isSuccess

fun isValidTime(timeStr: String): Boolean =
    runCatching { LocalTime.parse(timeStr, TIME_FORMATTER) }.isSuccess

fun parseDate(dateStr: String): LocalDate? =
    runCatching { LocalDate.parse(dateStr, DATE_FORMATTER) }.getOrNull()

fun parseTime(timeStr: String): LocalTime? =
    runCatching { LocalTime.parse(timeStr, TIME_FORMATTER) }.getOrNull()

fun formatDate(date: LocalDate): String = date.format(DATE_FORMATTER)
fun formatTime(time: LocalTime): String = time.format(TIME_FORMATTER)

fun createCameraUri(context: Context): Uri {
    val file = File(context.externalCacheDir, "photo_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}

fun copyUriToCache(context: Context, uri: Uri): Uri {
    val dest = File(context.externalCacheDir, "gallery_${System.currentTimeMillis()}.jpg")
    return runCatching {
        context.contentResolver.openInputStream(uri)?.use { it.copyTo(dest.outputStream()) }
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", dest)
    }.getOrDefault(uri)
}

fun millisToLocalDate(millis: Long): LocalDate =
    Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).toLocalDate()

fun localDateToMillis(date: LocalDate): Long =
    date.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()

// ── Composants partagés ───────────────────────────────────────────────────────

/** Couleurs communes pour les OutlinedTextField du formulaire */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun taskFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = CyanPrimary,
    unfocusedBorderColor = TextSecondary,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    cursorColor = CyanPrimary
)

/** Label de section (TITRE, DATE LIMITE, etc.) */
@Composable
fun SectionLabel(text: String) {
    Text(text = text, color = TextSecondary, fontSize = 12.sp)
    Spacer(modifier = Modifier.height(6.dp))
}

/** Champ date + champ heure côte à côte */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimeRow(
    dueDateText: String,
    dueTimeText: String,
    onDateChange: (String) -> Unit,
    onTimeChange: (String) -> Unit,
    onOpenDatePicker: () -> Unit,
    onOpenTimePicker: () -> Unit,
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(modifier = Modifier.weight(1f)) {
            SectionLabel("DATE LIMITE")
            OutlinedTextField(
                value = dueDateText,
                onValueChange = onDateChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("jj/mm/aaaa", color = TextSecondary) },
                trailingIcon = {
                    IconButton(onClick = onOpenDatePicker) {
                        Icon(Icons.Default.DateRange, contentDescription = "Calendrier", tint = CyanPrimary)
                    }
                },
                singleLine = true,
                colors = taskFieldColors(),
                shape = RoundedCornerShape(12.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            SectionLabel("HEURE")
            OutlinedTextField(
                value = dueTimeText,
                onValueChange = onTimeChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("--:--", color = TextSecondary) },
                trailingIcon = {
                    IconButton(onClick = onOpenTimePicker) {
                        Icon(Icons.Default.AccessTime, contentDescription = "Heure", tint = CyanPrimary)
                    }
                },
                singleLine = true,
                colors = taskFieldColors(),
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

/** Sélecteur de priorité */
@Composable
fun PrioritySelector(selected: Priority, onSelect: (Priority) -> Unit) {
    val options = listOf(Priority.LOW to "Normale", Priority.MEDIUM to "Haute", Priority.HIGH to "Critique")
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { (priority, label) ->
            val color = when (priority) {
                Priority.LOW -> PriorityLow
                Priority.MEDIUM -> PriorityMedium
                Priority.HIGH -> PriorityHigh
            }
            Button(
                onClick = { onSelect(priority) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selected == priority) color else DarkSurface,
                    contentColor = if (selected == priority) Color.White else TextSecondary
                ),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 10.dp)
            ) {
                Text(text = label, fontSize = 12.sp, fontFamily = irishGroverFont)
            }
        }
    }
}

/** Sélecteur de périodicité (dropdown) */
@Composable
fun RecurrenceSelector(
    selected: RecurrenceType,
    expanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    onSelect: (RecurrenceType) -> Unit,
) {
    val label = when (selected) {
        RecurrenceType.NONE -> "Aucune"
        RecurrenceType.DAILY -> "Quotidienne"
        RecurrenceType.WEEKLY -> "Hebdomadaire"
        RecurrenceType.MONTHLY -> "Mensuelle"
    }
    val options = listOf(
        RecurrenceType.NONE to "Aucune",
        RecurrenceType.DAILY to "Quotidienne",
        RecurrenceType.WEEKLY to "Hebdomadaire",
        RecurrenceType.MONTHLY to "Mensuelle"
    )
    Box {
        OutlinedButton(
            onClick = { onExpandChange(true) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
            border = BorderStroke(1.dp, TextSecondary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = label, fontFamily = irishGroverFont, modifier = Modifier.weight(1f))
            Text(text = "▾", color = TextSecondary)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { onExpandChange(false) }) {
            options.forEach { (type, text) ->
                DropdownMenuItem(
                    text = { Text(text, fontFamily = irishGroverFont) },
                    onClick = { onSelect(type); onExpandChange(false) }
                )
            }
        }
    }
}

/** Section photo : aperçu avec bouton supprimer, ou bouton d'ajout */
@Composable
fun PhotoSection(
    photoUri: Uri?,
    menuExpanded: Boolean,
    onMenuExpandChange: (Boolean) -> Unit,
    onRemovePhoto: () -> Unit,
    onPickFromGallery: () -> Unit,
    onTakePhoto: () -> Unit,
) {
    val context = LocalContext.current
    if (photoUri != null) {
        Box(modifier = Modifier.fillMaxWidth()) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(photoUri)
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
                onClick = onRemovePhoto,
                modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)
            ) {
                Surface(shape = RoundedCornerShape(50), color = Color.Black.copy(alpha = 0.6f)) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Supprimer photo",
                        tint = Color.White,
                        modifier = Modifier.padding(4.dp).size(20.dp)
                    )
                }
            }
        }
    } else {
        Box {
            OutlinedButton(
                onClick = { onMenuExpandChange(true) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                border = BorderStroke(1.dp, TextSecondary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ajouter une photo", fontFamily = irishGroverFont, modifier = Modifier.weight(1f))
                Text("▾", color = TextSecondary)
            }
            DropdownMenu(expanded = menuExpanded, onDismissRequest = { onMenuExpandChange(false) }) {
                DropdownMenuItem(
                    text = { Text("📷  Prendre une photo", fontFamily = irishGroverFont) },
                    onClick = { onMenuExpandChange(false); onTakePhoto() }
                )
                DropdownMenuItem(
                    text = { Text("🖼️  Choisir depuis la galerie", fontFamily = irishGroverFont) },
                    onClick = { onMenuExpandChange(false); onPickFromGallery() }
                )
            }
        }
    }
}

/** DatePickerDialog Material3 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDatePickerDialog(
    currentDateText: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    val initialMillis = if (isValidDate(currentDateText))
        parseDate(currentDateText)?.let { localDateToMillis(it) } ?: System.currentTimeMillis()
    else System.currentTimeMillis()

    val state = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                state.selectedDateMillis?.let { onConfirm(formatDate(millisToLocalDate(it))) }
                onDismiss()
            }) { Text("OK", color = CyanPrimary, fontFamily = irishGroverFont) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler", color = TextSecondary, fontFamily = irishGroverFont)
            }
        }
    ) { DatePicker(state = state) }
}

/** TimePickerDialog Material3 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskTimePickerDialog(
    currentTimeText: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    val now = LocalTime.now()
    val initialHour = if (isValidTime(currentTimeText)) currentTimeText.split(":")[0].toInt() else now.hour
    val initialMinute = if (isValidTime(currentTimeText)) currentTimeText.split(":")[1].toInt() else 0
    val state = rememberTimePickerState(initialHour = initialHour, initialMinute = initialMinute, is24Hour = true)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choisir l'heure", color = TextPrimary, fontFamily = irishGroverFont) },
        text = { TimePicker(state = state) },
        containerColor = CardBackground,
        confirmButton = {
            TextButton(onClick = {
                onConfirm("%02d:%02d".format(state.hour, state.minute))
                onDismiss()
            }) { Text("OK", color = CyanPrimary, fontFamily = irishGroverFont) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler", color = TextSecondary, fontFamily = irishGroverFont)
            }
        }
    )
}

/** Boutons Annuler / Valider en bas du formulaire */
@Composable
fun FormActionButtons(
    confirmLabel: String,
    confirmEnabled: Boolean,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(
            onClick = onCancel,
            modifier = Modifier.weight(1f).height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DarkSurface, contentColor = TextPrimary),
            shape = RoundedCornerShape(14.dp)
        ) { Text("Annuler") }
        Button(
            onClick = onConfirm,
            modifier = Modifier.weight(1f).height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color.Black),
            shape = RoundedCornerShape(14.dp),
            enabled = confirmEnabled
        ) { Text(confirmLabel) }
    }
}

