package com.addo.app.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.addo.app.data.PRIORITY_HIGH
import com.addo.app.data.PRIORITY_LOW
import com.addo.app.data.PRIORITY_MEDIUM
import com.addo.app.data.PRIORITY_NONE
import com.addo.app.data.Task
import com.addo.app.ui.MainViewModel
import com.addo.app.ui.components.dueLabel
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailSheet(
    vm: MainViewModel,
    taskId: Long,
    onDismiss: () -> Unit,
    onFocus: (Long) -> Unit
) {
    val task by remember(taskId) { vm.taskFlow(taskId) }.collectAsState(initial = null)
    val steps by remember(taskId) { vm.subtasksFlow(taskId) }.collectAsState(initial = emptyList())
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val current = task ?: return
    val zone = ZoneId.systemDefault()

    var title by remember(taskId) { mutableStateOf(current.title) }
    var notes by remember(taskId) { mutableStateOf(current.notes) }
    var newStep by remember(taskId) { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    fun save(updated: Task) = vm.update(updated)

    ModalBottomSheet(onDismissRequest = {
        val trimmed = title.trim()
        if (trimmed.isNotEmpty() && (trimmed != current.title || notes != current.notes)) {
            save(current.copy(title = trimmed, notes = notes))
        }
        onDismiss()
    }, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.titleLarge,
                placeholder = { Text("Task title") }
            )

            // When --------------------------------------------------------
            Text("📅 When", style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val today = LocalDate.now(zone)
                val dueDay = current.dueDay(zone)
                FilterChip(
                    selected = dueDay == today,
                    onClick = { vm.moveToToday(current) },
                    label = { Text("Today") }
                )
                FilterChip(
                    selected = dueDay == today.plusDays(1),
                    onClick = { vm.moveToTomorrow(current) },
                    label = { Text("Tomorrow") }
                )
                FilterChip(
                    selected = dueDay != null && dueDay != today && dueDay != today.plusDays(1),
                    onClick = { showDatePicker = true },
                    label = {
                        Text(
                            if (dueDay != null && dueDay != today && dueDay != today.plusDays(1))
                                dueLabel(current) ?: "Pick a day"
                            else "Pick a day"
                        )
                    }
                )
                if (current.dueAt != null) {
                    FilterChip(
                        selected = false,
                        onClick = {
                            save(current.copy(dueAt = null, isAllDay = true, reminderAt = null))
                        },
                        label = { Text("No date") }
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = !current.isAllDay,
                    onClick = { showTimePicker = true },
                    label = {
                        Text(
                            if (current.isAllDay) "Set a time" else
                                Instant.ofEpochMilli(current.dueAt!!).atZone(zone).toLocalTime()
                                    .let { "%d:%02d".format(if (it.hour % 12 == 0) 12 else it.hour % 12, it.minute) +
                                            if (it.hour < 12) " AM" else " PM" }
                        )
                    }
                )
                if (!current.isAllDay && current.dueAt != null) {
                    TextButton(onClick = {
                        val day = current.dueDay(zone)!!
                        save(
                            current.copy(
                                dueAt = day.atStartOfDay(zone).toInstant().toEpochMilli(),
                                isAllDay = true,
                                reminderAt = null
                            )
                        )
                    }) { Text("Clear time") }
                }
            }

            // Remind ------------------------------------------------------
            if (current.dueAt != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Remind me", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            if (current.isAllDay) "All-day tasks remind at 9:00 AM"
                            else "At the task's time",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = current.reminderAt != null,
                        onCheckedChange = { wantReminder ->
                            val reminderAt = if (!wantReminder) null else {
                                if (current.isAllDay)
                                    current.dueDay(zone)!!.atTime(LocalTime.of(9, 0))
                                        .atZone(zone).toInstant().toEpochMilli()
                                else current.dueAt
                            }
                            save(current.copy(reminderAt = reminderAt))
                        }
                    )
                }
            }

            // Priority ----------------------------------------------------
            Text("⚡ Priority", style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    PRIORITY_NONE to "None",
                    PRIORITY_LOW to "Low",
                    PRIORITY_MEDIUM to "Medium",
                    PRIORITY_HIGH to "High"
                ).forEach { (value, label) ->
                    FilterChip(
                        selected = current.priority == value,
                        onClick = { save(current.copy(priority = value)) },
                        label = { Text(label) }
                    )
                }
            }

            // Steps -------------------------------------------------------
            Text("🪜 Steps — make it smaller", style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            steps.forEach { step ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { vm.setDone(step, !step.done) }) {
                        Icon(
                            if (step.done) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
                            contentDescription = null,
                            tint = if (step.done) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(step.title, modifier = Modifier.weight(1f))
                    IconButton(onClick = { vm.delete(step) }) {
                        Icon(
                            Icons.Filled.Delete, contentDescription = "Delete step",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = newStep,
                    onValueChange = { newStep = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Add a tiny first step…") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        vm.addSubtask(taskId, newStep)
                        newStep = ""
                    })
                )
                IconButton(
                    onClick = {
                        vm.addSubtask(taskId, newStep)
                        newStep = ""
                    },
                    enabled = newStep.isNotBlank()
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add step")
                }
            }

            // Notes -------------------------------------------------------
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Notes") },
                minLines = 2
            )

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = {
                    vm.delete(current)
                    onDismiss()
                }) {
                    Icon(Icons.Filled.Delete, contentDescription = null)
                    Text("  Delete")
                }
                TextButton(onClick = {
                    val trimmed = title.trim()
                    if (trimmed.isNotEmpty() && (trimmed != current.title || notes != current.notes)) {
                        save(current.copy(title = trimmed, notes = notes))
                    }
                    onFocus(taskId)
                }) {
                    Icon(Icons.Outlined.Timer, contentDescription = null)
                    Text("  Focus on this")
                }
            }
        }
    }

    if (showDatePicker) {
        val initialMillis = current.dueDay(zone)
            ?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { utcMillis ->
                        val day = Instant.ofEpochMilli(utcMillis).atZone(ZoneOffset.UTC).toLocalDate()
                        val newDue = if (current.isAllDay || current.dueAt == null) {
                            day.atStartOfDay(zone).toInstant().toEpochMilli()
                        } else {
                            val time = Instant.ofEpochMilli(current.dueAt).atZone(zone).toLocalTime()
                            day.atTime(time).atZone(zone).toInstant().toEpochMilli()
                        }
                        save(current.copy(dueAt = newDue))
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val existing = if (!current.isAllDay && current.dueAt != null)
            Instant.ofEpochMilli(current.dueAt).atZone(zone).toLocalTime()
        else LocalTime.of(9, 0)
        val timeState = rememberTimePickerState(
            initialHour = existing.hour,
            initialMinute = existing.minute
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val day = current.dueDay(zone) ?: LocalDate.now(zone)
                    val newDue = day.atTime(timeState.hour, timeState.minute)
                        .atZone(zone).toInstant().toEpochMilli()
                    save(
                        current.copy(
                            dueAt = newDue,
                            isAllDay = false,
                            // A timed task defaults to reminding — that's the point of the time.
                            reminderAt = newDue.takeIf { it > System.currentTimeMillis() }
                                ?: current.reminderAt
                        )
                    )
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
            },
            text = { TimePicker(state = timeState) }
        )
    }
}
