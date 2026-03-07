package com.example.todoleloup

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.todoleloup.data.BackgroundTheme

class RewardViewModel : ViewModel() {

    var points by mutableStateOf(0)
        private set

    var activeBackground by mutableStateOf(BackgroundTheme.DEFAULT)
        private set

    var unlockedBackgrounds by mutableStateOf(setOf(BackgroundTheme.DEFAULT))
        private set

    // Dernière récompense gagnée (pour affichage temporaire)
    var lastEarnedPoints by mutableStateOf<Int?>(null)
        private set

    fun addPoints(amount: Int) {
        points += amount
        lastEarnedPoints = amount
    }

    fun clearLastEarned() {
        lastEarnedPoints = null
    }

    fun buyBackground(theme: BackgroundTheme): Boolean {
        if (theme in unlockedBackgrounds) {
            // Déjà débloqué → juste l'activer
            activeBackground = theme
            return true
        }
        if (points >= theme.cost) {
            points -= theme.cost
            unlockedBackgrounds = unlockedBackgrounds + theme
            activeBackground = theme
            return true
        }
        return false
    }

    fun setActiveBackground(theme: BackgroundTheme): Boolean {
        if (theme in unlockedBackgrounds) {
            activeBackground = theme
            return true
        }
        return false
    }
}

