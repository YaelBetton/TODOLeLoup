package com.example.todoleloup.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import java.time.LocalDate
import java.time.LocalTime

// ── Adaptateurs Gson pour LocalDate et LocalTime ──────────────────────────────

private object LocalDateAdapter : JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {
    override fun serialize(src: LocalDate, typeOfSrc: Type, ctx: JsonSerializationContext) =
        JsonPrimitive(src.toString())

    override fun deserialize(json: JsonElement, typeOfT: Type, ctx: JsonDeserializationContext): LocalDate =
        LocalDate.parse(json.asString)
}

private object LocalTimeAdapter : JsonSerializer<LocalTime>, JsonDeserializer<LocalTime> {
    override fun serialize(src: LocalTime, typeOfSrc: Type, ctx: JsonSerializationContext) =
        JsonPrimitive(src.toString())

    override fun deserialize(json: JsonElement, typeOfT: Type, ctx: JsonDeserializationContext): LocalTime =
        LocalTime.parse(json.asString)
}

// ── Objet principal de stockage ───────────────────────────────────────────────

object TaskStorage {

    private const val PREFS_NAME = "todoleloup_prefs"
    private const val KEY_TASKS  = "tasks_json"

    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter)
        .registerTypeAdapter(LocalTime::class.java, LocalTimeAdapter)
        .create()

    fun saveTasks(context: Context, tasks: List<Task>) {
        val json = gson.toJson(tasks)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_TASKS, json)
            .apply()
    }

    fun loadTasks(context: Context): List<Task> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json  = prefs.getString(KEY_TASKS, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<Task>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}

