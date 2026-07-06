package com.addo.app.data

import com.addo.app.notifications.ReminderScheduler
import kotlinx.coroutines.flow.Flow

class TaskRepository(
    private val dao: TaskDao,
    private val reminders: ReminderScheduler
) {
    fun observeAll(): Flow<List<Task>> = dao.observeAll()

    fun observeById(id: Long): Flow<Task?> = dao.observeById(id)

    fun observeSubtasks(parentId: Long): Flow<List<Task>> = dao.observeSubtasks(parentId)

    suspend fun byId(id: Long): Task? = dao.byId(id)

    suspend fun add(task: Task): Long {
        val id = dao.insert(task)
        task.reminderAt?.let { reminders.schedule(task.copy(id = id)) }
        return id
    }

    suspend fun update(task: Task) {
        dao.update(task)
        if (task.reminderAt != null && !task.done) {
            reminders.schedule(task)
        } else {
            reminders.cancel(task.id)
        }
    }

    suspend fun setDone(task: Task, done: Boolean) {
        dao.setDone(task.id, done, if (done) System.currentTimeMillis() else null)
        if (done) reminders.cancel(task.id) else task.reminderAt?.let { reminders.schedule(task) }
    }

    suspend fun delete(task: Task) {
        reminders.cancel(task.id)
        dao.delete(task)
    }

    suspend fun snoozeReminder(id: Long, untilMillis: Long) {
        val task = dao.byId(id) ?: return
        val updated = task.copy(reminderAt = untilMillis)
        dao.update(updated)
        reminders.schedule(updated)
    }

    suspend fun rescheduleAllReminders() {
        dao.withPendingReminders(System.currentTimeMillis()).forEach { reminders.schedule(it) }
    }
}
