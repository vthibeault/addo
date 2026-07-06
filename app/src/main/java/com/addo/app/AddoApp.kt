package com.addo.app

import android.app.Application
import com.addo.app.data.AddoDatabase
import com.addo.app.data.SettingsRepository
import com.addo.app.data.TaskRepository
import com.addo.app.notifications.NotificationHelper
import com.addo.app.notifications.ReminderScheduler

/**
 * Plain manual dependency wiring — the app is deliberately small enough
 * that a DI framework would be more machinery than product.
 */
class AddoApp : Application() {

    val database: AddoDatabase by lazy { AddoDatabase.build(this) }
    val reminderScheduler: ReminderScheduler by lazy { ReminderScheduler(this) }
    val repository: TaskRepository by lazy { TaskRepository(database.taskDao(), reminderScheduler) }
    val settings: SettingsRepository by lazy { SettingsRepository(this) }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.ensureChannel(this)
    }
}
