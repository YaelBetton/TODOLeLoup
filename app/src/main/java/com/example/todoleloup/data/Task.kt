package com.example.todoleloup.data

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class Task(
    val id: Int,
    val title: String,
    val description: String,
    val deadlineDate: LocalDate?,
    val deadlineTime: LocalTime?,
    val status: TaskStatus,
    val priority: Priority,
    val recurrence: RecurrenceType
) {
    fun isOverdue(): Boolean {
        if (status == TaskStatus.DONE) {
            return false
        }
        val date = deadlineDate ?: return false
        val time = deadlineTime ?: LocalTime.MIDNIGHT
        return LocalDateTime.now().isAfter(LocalDateTime.of(date, time))
    }

    fun isUpcoming(thresholdHours: Int): Boolean {
        if (status == TaskStatus.DONE) {
            return false
        }
        val date = deadlineDate ?: return false
        val time = deadlineTime ?: LocalTime.MIDNIGHT
        val deadline = LocalDateTime.of(date, time)
        val now = LocalDateTime.now()
        val threshold = now.plusHours(thresholdHours.toLong())
        return !deadline.isBefore(now) && !deadline.isAfter(threshold)
    }
}

enum class TaskStatus {
    TODO,
    OVERDUE,
    DONE
}

enum class Priority {
    LOW,
    MEDIUM,
    HIGH
}

enum class RecurrenceType {
    NONE,
    DAILY,
    WEEKLY,
    MONTHLY
}
