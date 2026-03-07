package com.example.todoleloup.data

import androidx.compose.ui.graphics.Color
import com.example.todoleloup.R

// Points gagnés selon la priorité de la tâche
fun pointsForPriority(priority: Priority): Int = when (priority) {
    Priority.LOW -> 10
    Priority.MEDIUM -> 25
    Priority.HIGH -> 50
}

// Les fonds disponibles dans la boutique
enum class BackgroundTheme(
    val id: String,
    val displayName: String,
    val description: String,
    val cost: Int,
    val colors: List<Color>,
    val emoji: String,
    val drawableRes: Int? = null
) {
    DEFAULT(
        id = "default",
        displayName = "Palier 0",
        description = "Le fond de base",
        cost = 0,
        colors = listOf(Color(0xFF0A0E27), Color(0xFF0A0E27)),
        emoji = "🌑"
    ),
    FOREST_NIGHT(
        id = "forest_night",
        displayName = "Palier 1",
        description = "Premier fond débloqué",
        cost = 100,
        colors = listOf(Color(0xFF0A1A0A), Color(0xFF0D2B1A)),
        emoji = "🐾",
        drawableRes = R.drawable.first_bg
    ),
    BLOOD_MOON(
        id = "blood_moon",
        displayName = "Palier 2",
        description = "Deuxième fond débloqué",
        cost = 250,
        colors = listOf(Color(0xFF1A0A0A), Color(0xFF2B0D0D)),
        emoji = "🐾",
        drawableRes = R.drawable.second_bg
    ),
    ARCTIC_TUNDRA(
        id = "arctic_tundra",
        displayName = "Palier 3",
        description = "Troisième fond débloqué",
        cost = 400,
        colors = listOf(Color(0xFF0A1020), Color(0xFF102040)),
        emoji = "🐾",
        drawableRes = R.drawable.third_bg
    )
}

