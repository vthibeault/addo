package com.addo.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.addo.app.data.PRIORITY_HIGH
import com.addo.app.data.PRIORITY_MEDIUM
import com.addo.app.data.Task

@Composable
fun TaskRow(
    task: Task,
    stepCount: Pair<Int, Int>?,
    onToggleDone: (Boolean) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showDue: Boolean = true
) {
    val haptics = LocalHapticFeedback.current
    val checkScale by animateFloatAsState(
        targetValue = if (task.done) 1.1f else 1f,
        animationSpec = spring(dampingRatio = 0.35f, stiffness = 900f),
        label = "checkScale"
    )
    val titleColor by animateColorAsState(
        targetValue = if (task.done) MaterialTheme.colorScheme.onSurfaceVariant
        else MaterialTheme.colorScheme.onSurface,
        label = "titleColor"
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onToggleDone(!task.done)
            }) {
                Icon(
                    imageVector = if (task.done) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
                    contentDescription = if (task.done) "Mark not done" else "Mark done",
                    tint = if (task.done) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(28.dp)
                        .scale(checkScale)
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = titleColor,
                    textDecoration = if (task.done) TextDecoration.LineThrough else null,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                val meta = buildList {
                    if (task.priority >= PRIORITY_MEDIUM) {
                        add(if (task.priority == PRIORITY_HIGH) "High" else "Medium")
                    }
                    if (showDue) dueLabel(task)?.let { add(it) }
                    stepCount?.let { (done, total) -> if (total > 0) add("$done/$total steps") }
                    task.tagList.forEach { add("#$it") }
                }
                if (meta.isNotEmpty()) {
                    Text(
                        text = meta.joinToString("  ·  "),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (task.priority == PRIORITY_HIGH && !task.done)
                            MaterialTheme.colorScheme.secondary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
