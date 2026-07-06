package com.addo.app.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.addo.app.AddoApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationActionReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_DONE = "com.addo.app.action.DONE"
        const val ACTION_SNOOZE = "com.addo.app.action.SNOOZE"
        const val EXTRA_TASK_ID = "task_id"
        private const val SNOOZE_MILLIS = 10 * 60 * 1000L
    }

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)
        if (taskId <= 0) return
        val app = context.applicationContext as AddoApp
        val result = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (intent.action) {
                    ACTION_DONE -> {
                        app.repository.byId(taskId)?.let { task ->
                            app.repository.setDone(task, true)
                            app.settings.recordCompletion()
                        }
                    }
                    ACTION_SNOOZE -> {
                        app.repository.snoozeReminder(taskId, System.currentTimeMillis() + SNOOZE_MILLIS)
                    }
                }
                NotificationHelper.cancel(context, taskId)
            } finally {
                result.finish()
            }
        }
    }
}
