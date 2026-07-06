package com.addo.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.addo.app.data.Task
import com.addo.app.domain.QuickAddParser

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
    val parsed = remember(text) { if (text.isBlank()) null else QuickAddParser.parse(text) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            if (parsed != null && (parsed.dueAt != null || parsed.priority > 0 || parsed.tags.isNotEmpty())) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(bottom = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val chips = buildList {
                        parsed.dueAt?.let {
                            val preview = Task(title = "", dueAt = it, isAllDay = !parsed.hasTime)
                            dueLabel(preview)?.let { label -> add("📅 $label") }
                        }
                        if (parsed.priority > 0) {
                            add(
                                when (parsed.priority) {
                                    PRIORITY_HIGH -> "⚑ High"
                                    PRIORITY_LOW -> "⚑ Low"
                                    else -> "⚑ Medium"
                                }
                            )
                        }
                        parsed.tags.forEach { add("#$it") }
                    }
                    chips.forEach { label ->
                        AssistChip(
                            onClick = {},
                            label = { Text(label) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Add a task…  try \"gym tomorrow 6pm !!\"") },
                    singleLine = true,
                    shape = MaterialTheme.shapes.extraLarge,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        if (text.isNotBlank()) {
                            onSubmit(text)
                            text = ""
                        }
                    })
                )
                if (text.isNotBlank()) {
                    FilledIconButton(
                        onClick = {
                            onSubmit(text)
                            text = ""
                        },
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Icon(Icons.Filled.ArrowUpward, contentDescription = "Add task")
                    }
                }
            }
        }
    }
}
