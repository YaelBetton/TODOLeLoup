package com.example.todoleloup.data

data class Task(
    val id: Int,
    val title: String,
    val isCompleted: Boolean = false,
    val isUrgent: Boolean = false
)

