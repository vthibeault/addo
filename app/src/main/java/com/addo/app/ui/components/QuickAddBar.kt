package com.addo.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.addo.app.data.PRIORITY_HIGH
import com.addo.app.data.PRIORITY_LOW
import com.addo.app.data.Task
import com.addo.app.domain.QuickAddParser
import com.addo.app.ui.theme.AddoColors

private val placeholders = listOf(
    "brain dump it… \"gym tomorrow 6pm !!\"",
    "what's on your mind? \"call mom tonight\"",
    "just type it… \"meds at 9am\"",
    "one tiny thing… \"water plants\"",
    "toss it in… \"dentist friday #health\""
)

/**
 * The single most important surface in the app: always visible, one line,
 * shows what it understood as you type so there are no surprises.
 */
@Composable
fun QuickAddBar(
    onSubmit: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var text by remember { mutableStateOf("") }
    val placeholder = remember { placeholders.random() }
    val parsed = remember(text) { if (text.isBlank()) null else QuickAddParser.parse(text) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            if (parsed != null && (parsed.dueAt != null || parsed.priority > 0 || parsed.tags.isNotEmpty())) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    parsed.dueAt?.let {
                        val preview = Task(title = "", dueAt = it, isAllDay = !parsed.hasTime)
                        dueLabel(preview)?.let { label ->
                            ParseChip("📅 $label", MaterialTheme.colorScheme.tertiaryContainer,
                                MaterialTheme.colorScheme.onTertiaryContainer)
                        }
                    }
                    if (parsed.priority > 0) {
                        ParseChip(
                            when (parsed.priority) {
                                PRIORITY_HIGH -> "🔥 High"
                                PRIORITY_LOW -> "🌱 Low"
                                else -> "⚡ Medium"
                            },
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    parsed.tags.forEach { tag ->
                        ParseChip(
                            "#$tag",
                            AddoColors.grape.copy(alpha = 0.2f),
                            AddoColors.grape
                        )
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    },
                    singleLine = true,
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        if (text.isNotBlank()) {
                            onSubmit(text)
                            text = ""
                        }
                    })
                )
                AnimatedVisibility(
                    visible = text.isNotBlank(),
                    enter = scaleIn(spring(dampingRatio = 0.35f, stiffness = 600f)) + fadeIn(),
                    exit = scaleOut() + fadeOut()
                ) {
                    IconButton(
                        onClick = {
                            onSubmit(text)
                            text = ""
                        },
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                    ) {
                        Icon(
                            Icons.Filled.ArrowUpward,
                            contentDescription = "Add task",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ParseChip(label: String, container: Color, content: Color) {
    Surface(shape = MaterialTheme.shapes.small, color = container) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = content,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}
