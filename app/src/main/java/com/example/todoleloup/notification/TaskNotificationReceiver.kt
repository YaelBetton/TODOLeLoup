package com.example.todoleloup.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.todoleloup.MainActivity
import com.example.todoleloup.R

class TaskNotificationReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_TASK_ID = "task_id"
        const val EXTRA_TASK_TITLE = "task_title"
        const val EXTRA_TASK_DESC = "task_desc"
        const val CHANNEL_ID = "todoleloup_tasks"
        const val CHANNEL_NAME = "Rappels de tâches"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val taskId    = intent.getIntExtra(EXTRA_TASK_ID, 0)
        val taskTitle = intent.getStringExtra(EXTRA_TASK_TITLE) ?: "Tâche"
        val taskDesc  = intent.getStringExtra(EXTRA_TASK_DESC)  ?: "Échéance atteinte !"

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Créer le canal (ignoré si déjà existant)
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications de rappel pour vos tâches TodoLeLoup"
            enableVibration(true)
        }
        notificationManager.createNotificationChannel(channel)

        // Intent pour ouvrir l'app au tap
        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val tapPendingIntent = PendingIntent.getActivity(
            context, taskId, tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_moon)
            .setContentTitle("🐺 $taskTitle")
            .setContentText(taskDesc)
            .setStyle(NotificationCompat.BigTextStyle().bigText(taskDesc))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(tapPendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(taskId, notification)
    }
}

