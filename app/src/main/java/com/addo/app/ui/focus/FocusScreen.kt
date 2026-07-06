package com.addo.app.ui.focus

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.addo.app.ui.MainViewModel
import com.addo.app.ui.components.formatMillisAsClock

/**
 * One task. Nothing else on screen. A timer to make time visible,
 * steps to make starting smaller, and a big satisfying Done.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusScreen(
    vm: MainViewModel,
    taskId: Long,
    onBack: () -> Unit
) {
    val task by remember(taskId) { vm.taskFlow(taskId) }.collectAsState(initial = null)
    val steps by remember(taskId) { vm.subtasksFlow(taskId) }.collectAsState(initial = emptyList())
    val timer by vm.timer.collectAsState()
    val haptics = LocalHapticFeedback.current

    LaunchedEffect(taskId) { vm.openFocus(taskId) }
    LaunchedEffect(timer.finished) {
        if (timer.finished) haptics.performHapticFeedback(HapticFeedbackType.LongPress)
    }
    // Task completed (or deleted) while focused → leave gracefully.
    LaunchedEffect(task?.done) {
        if (task?.done == true) onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Focus") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        val current = task ?: return@Scaffold
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = current.title,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 16.dp, bottom = 24.dp)
            )

            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = {
                        if (timer.totalMillis == 0L) 0f
                        else (timer.remainingMillis.toFloat() / timer.totalMillis).coerceIn(0f, 1f)
                    },
                    modifier = Modifier.size(220.dp),
                    strokeWidth = 10.dp,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (timer.finished) "Time's up!"
                        else formatMillisAsClock(timer.remainingMillis),
                        style = MaterialTheme.typography.displayMedium
                    )
                    if (timer.finished) {
                        Text(
                            "Nice work. Stretch, sip water, or keep rolling.",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(10, 25, 50).forEach { minutes ->
                    FilterChip(
                        selected = timer.totalMillis == minutes * 60_000L,
                        onClick = { vm.setTimerDuration(minutes) },
                        label = { Text("$minutes min") }
                    )
                }
            }

            Row(
                modifier = Modifier.padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = vm::resetTimer) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Reset timer")
                }
                Button(onClick = vm::toggleTimer) {
                    Icon(
                        if (timer.running) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = null
                    )
                    Text(if (timer.running) "  Pause" else "  Start")
                }
            }

            if (steps.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        "Steps",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
                            Text(
                                step.title,
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (step.done) MaterialTheme.colorScheme.onSurfaceVariant
                                else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Button(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    vm.setDone(current, true)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp)
            ) {
                Icon(Icons.Filled.Check, contentDescription = null)
                Text("  Done — mark complete", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
