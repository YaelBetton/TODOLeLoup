package com.example.todoleloup.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.todoleloup.data.Task
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

object NotificationScheduler {

    /**
     * Programme une notification pour la tâche donnée.
     * - Si la tâche a une date ET une heure → notif à l'heure exacte
     * - Si la tâche a seulement une date → notif à 8h00 ce jour-là
     * - Si la date/heure est déjà passée → rien
     */
    fun schedule(context: Context, task: Task) {
        val deadlineDate = task.deadlineDate ?: return
        val deadlineTime = task.deadlineTime ?: LocalTime.of(8, 0)

        val triggerAt = LocalDateTime.of(deadlineDate, deadlineTime)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        if (triggerAt <= System.currentTimeMillis()) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // SCHEDULE_EXACT_ALARM requis (minSdk 33 = Android 13, permission déclarée dans le manifest)
        if (!alarmManager.canScheduleExactAlarms()) return

        val intent = Intent(context, TaskNotificationReceiver::class.java).apply {
            putExtra(TaskNotificationReceiver.EXTRA_TASK_ID, task.id)
            putExtra(TaskNotificationReceiver.EXTRA_TASK_TITLE, task.title)
            putExtra(TaskNotificationReceiver.EXTRA_TASK_DESC, task.description.ifBlank { "Échéance atteinte !" })
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.id, // requestCode unique par tâche
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
    }

    /**
     * Annule la notification programmée pour une tâche.
     */
    fun cancel(context: Context, taskId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, TaskNotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    /**
     * Reprogramme toutes les tâches (ex : au démarrage de l'app).
     */
    fun rescheduleAll(context: Context, tasks: List<Task>) {
        tasks.forEach { task ->
            cancel(context, task.id)
            if (task.deadlineDate != null) schedule(context, task)
        }
    }
}



