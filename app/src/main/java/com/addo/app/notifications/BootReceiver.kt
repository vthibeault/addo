package com.addo.app.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.addo.app.AddoApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Alarms don't survive reboots or app updates; re-arm every pending reminder. */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_MY_PACKAGE_REPLACED
        ) return

        val app = context.applicationContext as AddoApp
        val result = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                app.repository.rescheduleAllReminders()
            } finally {
                result.finish()
            }
        }
    }
}
