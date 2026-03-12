package com.example.todoleloup.data

import android.content.Context

object RewardStorage {

    private const val PREFS_NAME      = "todoleloup_prefs"
    private const val KEY_POINTS      = "reward_points"
    private const val KEY_ACTIVE_BG   = "active_background"
    private const val KEY_UNLOCKED_BG = "unlocked_backgrounds"

    fun savePoints(context: Context, points: Int) {
        prefs(context).edit().putInt(KEY_POINTS, points).apply()
    }

    fun loadPoints(context: Context): Int =
        prefs(context).getInt(KEY_POINTS, 0)

    fun saveActiveBackground(context: Context, theme: BackgroundTheme) {
        prefs(context).edit().putString(KEY_ACTIVE_BG, theme.name).apply()
    }

    fun loadActiveBackground(context: Context): BackgroundTheme =
        try {
            BackgroundTheme.valueOf(
                prefs(context).getString(KEY_ACTIVE_BG, BackgroundTheme.DEFAULT.name)!!
            )
        } catch (e: Exception) {
            BackgroundTheme.DEFAULT
        }

    fun saveUnlockedBackgrounds(context: Context, themes: Set<BackgroundTheme>) {
        val names = themes.map { it.name }.toSet()
        prefs(context).edit().putStringSet(KEY_UNLOCKED_BG, names).apply()
    }

    fun loadUnlockedBackgrounds(context: Context): Set<BackgroundTheme> {
        val names = prefs(context).getStringSet(KEY_UNLOCKED_BG, setOf(BackgroundTheme.DEFAULT.name))!!
        return names.mapNotNull {
            try { BackgroundTheme.valueOf(it) } catch (e: Exception) { null }
        }.toSet().ifEmpty { setOf(BackgroundTheme.DEFAULT) }
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}

