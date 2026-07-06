package com.addo.app.ui.components

import com.addo.app.data.Task
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val timeFormat = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
private val dayFormat = DateTimeFormatter.ofPattern("EEE, MMM d", Locale.getDefault())

/** Human phrasing for due chips: "Today 3:00 PM", "Tomorrow", "Fri, Mar 8". */
fun dueLabel(task: Task, zone: ZoneId = ZoneId.systemDefault()): String? {
    val dueAt = task.dueAt ?: return null
    val dateTime = Instant.ofEpochMilli(dueAt).atZone(zone)
    val day = dateTime.toLocalDate()
    val today = LocalDate.now(zone)
    val dayPart = when (day) {
        today -> "Today"
        today.plusDays(1) -> "Tomorrow"
        today.minusDays(1) -> "Yesterday"
        else -> day.format(dayFormat)
    }
    return if (task.isAllDay) dayPart else "$dayPart ${dateTime.format(timeFormat)}"
}

fun formatMillisAsClock(millis: Long): String {
    val totalSeconds = (millis + 999) / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
