package com.addo.app.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

const val PRIORITY_NONE = 0
const val PRIORITY_LOW = 1
const val PRIORITY_MEDIUM = 2
const val PRIORITY_HIGH = 3

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = Task::class,
            parentColumns = ["id"],
            childColumns = ["parentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("parentId"), Index("done"), Index("dueAt")]
)
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val notes: String = "",
    /** Epoch millis. For all-day tasks this is the start of the due day. */
    val dueAt: Long? = null,
    /** True when the task has a due day but no specific time. */
    @ColumnInfo(defaultValue = "1")
    val isAllDay: Boolean = true,
    val priority: Int = PRIORITY_NONE,
    val done: Boolean = false,
    val completedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    /** Non-null for subtasks (steps) of another task. */
    val parentId: Long? = null,
    /** Epoch millis at which a reminder notification should fire. */
    val reminderAt: Long? = null,
    /** Comma-separated lowercase tags, e.g. "home,errands". */
    val tags: String = ""
) {
    val tagList: List<String>
        get() = tags.split(',').map { it.trim() }.filter { it.isNotEmpty() }

    fun dueDay(zone: ZoneId = ZoneId.systemDefault()): LocalDate? =
        dueAt?.let { Instant.ofEpochMilli(it).atZone(zone).toLocalDate() }

    fun isDueToday(zone: ZoneId = ZoneId.systemDefault()): Boolean =
        dueDay(zone) == LocalDate.now(zone)

    fun isOverdue(nowMillis: Long = System.currentTimeMillis(), zone: ZoneId = ZoneId.systemDefault()): Boolean {
        val due = dueAt ?: return false
        return if (isAllDay) {
            dueDay(zone)!!.isBefore(LocalDate.now(zone))
        } else {
            due < nowMillis
        }
    }
}
