package com.addo.app.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.addo.app.MainActivity
import com.addo.app.R
import com.addo.app.data.Task

object NotificationHelper {

    const val CHANNEL_REMINDERS = "reminders"

    fun ensureChannel(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_REMINDERS,
            context.getString(R.string.channel_reminders),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.channel_reminders_desc)
        }
        manager.createNotificationChannel(channel)
    }

    fun showReminder(context: Context, task: Task) {
        val openIntent = PendingIntent.getActivity(
            context,
            task.id.toInt(),
            Intent(context, MainActivity::class.java).apply {
                putExtra(MainActivity.EXTRA_TASK_ID, task.id)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        fun action(actionName: String, requestOffset: Int): PendingIntent =
            PendingIntent.getBroadcast(
                context,
                task.id.toInt() * 10 + requestOffset,
                Intent(context, NotificationActionReceiver::class.java).apply {
                    action = actionName
                    putExtra(NotificationActionReceiver.EXTRA_TASK_ID, task.id)
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

        val notification = NotificationCompat.Builder(context, CHANNEL_REMINDERS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(task.title)
            .setContentText(context.getString(R.string.reminder_body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(openIntent)
            .addAction(0, context.getString(R.string.action_done), action(NotificationActionReceiver.ACTION_DONE, 1))
            .addAction(0, context.getString(R.string.action_snooze), action(NotificationActionReceiver.ACTION_SNOOZE, 2))
            .build()

        val manager = context.getSystemService(NotificationManager::class.java)
        runCatching { manager.notify(task.id.toInt(), notification) }
    }

    fun cancel(context: Context, taskId: Long) {
        context.getSystemService(NotificationManager::class.java).cancel(taskId.toInt())
    }
}
