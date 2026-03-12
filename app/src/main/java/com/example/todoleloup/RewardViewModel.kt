package com.example.todoleloup

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.example.todoleloup.data.BackgroundTheme
import com.example.todoleloup.data.RewardStorage

class RewardViewModel(app: Application) : AndroidViewModel(app) {

    private val ctx = app.applicationContext

    var points by mutableStateOf(RewardStorage.loadPoints(ctx))
        private set

    var activeBackground by mutableStateOf(RewardStorage.loadActiveBackground(ctx))
        private set

    var unlockedBackgrounds by mutableStateOf(RewardStorage.loadUnlockedBackgrounds(ctx))
        private set

    var lastEarnedPoints by mutableStateOf<Int?>(null)
        private set

    fun addPoints(amount: Int) {
        points += amount
        lastEarnedPoints = amount
        RewardStorage.savePoints(ctx, points)
    }

    fun clearLastEarned() {
        lastEarnedPoints = null
    }

    fun buyBackground(theme: BackgroundTheme): Boolean {
        if (theme in unlockedBackgrounds) {
            activeBackground = theme
            RewardStorage.saveActiveBackground(ctx, theme)
            return true
        }
        if (points >= theme.cost) {
            points -= theme.cost
            unlockedBackgrounds = unlockedBackgrounds + theme
            activeBackground = theme
            RewardStorage.savePoints(ctx, points)
            RewardStorage.saveUnlockedBackgrounds(ctx, unlockedBackgrounds)
            RewardStorage.saveActiveBackground(ctx, theme)
            return true
        }
        return false
    }

    fun setActiveBackground(theme: BackgroundTheme): Boolean {
        if (theme in unlockedBackgrounds) {
            activeBackground = theme
            RewardStorage.saveActiveBackground(ctx, theme)
            return true
        }
        return false
    }
}
