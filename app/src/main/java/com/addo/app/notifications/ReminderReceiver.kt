package com.addo.app.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.addo.app.AddoApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_TASK_ID = "task_id"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)
        if (taskId <= 0) return
        val app = context.applicationContext as AddoApp
        val result = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val task = app.repository.byId(taskId)
                if (task != null && !task.done) {
                    NotificationHelper.showReminder(context, task)
                }
            } finally {
                result.finish()
            }
        }
    }
}
