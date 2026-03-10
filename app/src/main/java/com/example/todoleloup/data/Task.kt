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
    val recurrence: RecurrenceType,
    val nextOccurrenceId: Int? = null,
    val rewardClaimed: Boolean = false,  // true si les points ont déjà été donnés
    val photoUri: String? = null         // URI de la photo jointe
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

    fun isUrgent(): Boolean {
        // Une tâche est urgente si elle est en retard ou marquée urgente.
        if (status == TaskStatus.DONE) {
            return false
        }
        return isOverdue() || priority == Priority.HIGH
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
