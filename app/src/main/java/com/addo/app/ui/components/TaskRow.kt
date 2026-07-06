package com.addo.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.addo.app.data.PRIORITY_HIGH
import com.addo.app.data.PRIORITY_LOW
import com.addo.app.data.PRIORITY_MEDIUM
import com.addo.app.data.Task
import com.addo.app.ui.theme.AddoColors
import kotlin.math.cos
import kotlin.math.sin

/**
 * Tasks as tilted candy-colored stickers, not spreadsheet rows.
 * Checking one off springs, bursts, and buzzes.
 */
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
    // Every card leans a little its own way, like stickers slapped on paper.
    val tilt = remember(task.id) { ((task.id % 7).toInt() - 3) * 0.4f }

    val cardColor by animateColorAsState(
        targetValue = when {
            task.done -> MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
            task.priority == PRIORITY_HIGH -> MaterialTheme.colorScheme.primaryContainer
            task.priority == PRIORITY_MEDIUM -> MaterialTheme.colorScheme.secondaryContainer
            task.priority == PRIORITY_LOW -> MaterialTheme.colorScheme.tertiaryContainer
            else -> MaterialTheme.colorScheme.surface
        },
        label = "cardColor"
    )
    val titleColor by animateColorAsState(
        targetValue = if (task.done) MaterialTheme.colorScheme.onSurfaceVariant
        else MaterialTheme.colorScheme.onSurface,
        label = "titleColor"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer { rotationZ = if (task.done) 0f else tilt },
        shape = MaterialTheme.shapes.large,
        color = cardColor,
        shadowElevation = if (task.done) 0.dp else 3.dp,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BouncyCheck(
                done = task.done,
                onToggle = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onToggleDone(!task.done)
                }
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 4.dp),
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
                    when (task.priority) {
                        PRIORITY_HIGH -> add("🔥 High")
                        PRIORITY_MEDIUM -> add("⚡ Medium")
                        PRIORITY_LOW -> add("🌱 Low")
                    }
                    if (showDue) dueLabel(task)?.let { add("📅 $it") }
                    stepCount?.let { (done, total) -> if (total > 0) add("🪜 $done/$total") }
                    task.tagList.forEach { add("#$it") }
                }
                if (meta.isNotEmpty()) {
                    Text(
                        text = meta.joinToString("   "),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

private val burstColors = listOf(
    AddoColors.coral, AddoColors.sunshine, AddoColors.mint,
    AddoColors.grape, AddoColors.bubblegum
)

/** A check circle that overshoots when tapped and fires a little star burst. */
@Composable
fun BouncyCheck(
    done: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (done) 1.15f else 1f,
        animationSpec = spring(dampingRatio = 0.3f, stiffness = 700f),
        label = "checkScale"
    )
    val burst = remember { Animatable(1f) }
    LaunchedEffect(done) {
        if (done) {
            burst.snapTo(0f)
            burst.animateTo(1f, tween(durationMillis = 550, easing = FastOutSlowInEasing))
        }
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        val t = burst.value
        if (done && t < 1f) {
            Canvas(modifier = Modifier.size(48.dp)) {
                val radius = 12.dp.toPx() + (16.dp.toPx() * t)
                repeat(8) { i ->
                    val angle = i * (Math.PI * 2 / 8).toFloat()
                    drawCircle(
                        color = burstColors[i % burstColors.size].copy(alpha = 1f - t),
                        radius = 3.dp.toPx() * (1f - t * 0.5f),
                        center = Offset(
                            center.x + cos(angle) * radius,
                            center.y + sin(angle) * radius
                        )
                    )
                }
            }
        }
        IconButton(onClick = onToggle) {
            Icon(
                imageVector = if (done) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
                contentDescription = if (done) "Mark not done" else "Mark done",
                tint = if (done) MaterialTheme.colorScheme.tertiary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(30.dp)
                    .scale(scale)
            )
        }
    }
}
