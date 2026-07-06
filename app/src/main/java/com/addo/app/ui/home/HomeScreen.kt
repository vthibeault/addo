package com.addo.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Casino
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.addo.app.data.Task
import com.addo.app.ui.MainViewModel
import com.addo.app.ui.components.QuickAddBar
import com.addo.app.ui.components.TaskRow

private enum class HomeTab(val label: String) { Today("Today"), Later("Later"), Done("Done") }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    vm: MainViewModel,
    onOpenTask: (Long) -> Unit
) {
    val sections by vm.sections.collectAsState()
    val streak by vm.streak.collectAsState()
    var tab by rememberSaveable { mutableStateOf(HomeTab.Today) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = when (tab) {
                                HomeTab.Today -> "Today"
                                HomeTab.Later -> "Later"
                                HomeTab.Done -> "Done"
                            },
                            style = MaterialTheme.typography.headlineSmall
                        )
                        if (streak > 0) {
                            Text(
                                text = "  🔥 $streak",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            Column {
                if (tab != HomeTab.Done) {
                    QuickAddBar(onSubmit = vm::quickAdd)
                }
                NavigationBar {
                    NavigationBarItem(
                        selected = tab == HomeTab.Today,
                        onClick = { tab = HomeTab.Today },
                        icon = { Icon(Icons.Filled.WbSunny, contentDescription = null) },
                        label = { Text(HomeTab.Today.label) }
                    )
                    NavigationBarItem(
                        selected = tab == HomeTab.Later,
                        onClick = { tab = HomeTab.Later },
                        icon = { Icon(Icons.Outlined.CalendarMonth, contentDescription = null) },
                        label = { Text(HomeTab.Later.label) }
                    )
                    NavigationBarItem(
                        selected = tab == HomeTab.Done,
                        onClick = { tab = HomeTab.Done },
                        icon = { Icon(Icons.Filled.TaskAlt, contentDescription = null) },
                        label = { Text(HomeTab.Done.label) }
                    )
                }
            }
        }
    ) { padding ->
        when (tab) {
            HomeTab.Today -> TodayList(vm, sections, padding, onOpenTask)
            HomeTab.Later -> LaterList(vm, sections, padding, onOpenTask)
            HomeTab.Done -> DoneList(vm, sections, padding, onOpenTask)
        }
    }
}

@Composable
private fun TodayList(
    vm: MainViewModel,
    sections: com.addo.app.ui.Sections,
    padding: PaddingValues,
    onOpenTask: (Long) -> Unit
) {
    val hasAnything = sections.todayRemaining > 0 || sections.doneToday.isNotEmpty()
    if (!hasAnything) {
        EmptyState(
            title = "Nothing on your plate",
            body = "Add one small thing below. Just one. 👇",
            padding = padding
        )
        return
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (sections.todayRemaining >= 2) {
            item(key = "pick") {
                OutlinedButton(
                    onClick = vm::pickForMe,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Outlined.Casino, contentDescription = null)
                    Text("  Can't decide? Pick for me")
                }
            }
        }
        if (sections.carriedOver.isNotEmpty()) {
            item(key = "header-carried") {
                SectionHeader("Carried over — today's a fresh start")
            }
            items(sections.carriedOver, key = { it.id }) { task ->
                TaskRow(
                    task = task,
                    stepCount = sections.stepCounts[task.id],
                    onToggleDone = { vm.setDone(task, it) },
                    onClick = { onOpenTask(task.id) }
                )
            }
        }
        if (sections.today.isNotEmpty()) {
            if (sections.carriedOver.isNotEmpty()) {
                item(key = "header-today") { SectionHeader("Today") }
            }
            items(sections.today, key = { it.id }) { task ->
                TaskRow(
                    task = task,
                    stepCount = sections.stepCounts[task.id],
                    onToggleDone = { vm.setDone(task, it) },
                    onClick = { onOpenTask(task.id) }
                )
            }
        }
        if (sections.todayRemaining == 0 && sections.doneToday.isNotEmpty()) {
            item(key = "all-clear") {
                EmptyStateInline(
                    title = "All clear 🎉",
                    body = "Everything for today is done. Be proud — rest is productive too."
                )
            }
        }
        if (sections.doneToday.isNotEmpty()) {
            item(key = "header-done-today") {
                SectionHeader("Done today (${sections.doneToday.size})")
            }
            items(sections.doneToday, key = { it.id }) { task ->
                TaskRow(
                    task = task,
                    stepCount = sections.stepCounts[task.id],
                    onToggleDone = { vm.setDone(task, it) },
                    onClick = { onOpenTask(task.id) },
                    showDue = false
                )
            }
        }
    }
}

@Composable
private fun LaterList(
    vm: MainViewModel,
    sections: com.addo.app.ui.Sections,
    padding: PaddingValues,
    onOpenTask: (Long) -> Unit
) {
    if (sections.upcoming.isEmpty() && sections.someday.isEmpty()) {
        EmptyState(
            title = "Nothing scheduled ahead",
            body = "Add a task with a day — \"dentist friday\" — and it lands here.",
            padding = padding
        )
        return
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (sections.upcoming.isNotEmpty()) {
            item(key = "header-upcoming") { SectionHeader("Coming up") }
            items(sections.upcoming, key = { it.id }) { task ->
                TaskRow(
                    task = task,
                    stepCount = sections.stepCounts[task.id],
                    onToggleDone = { vm.setDone(task, it) },
                    onClick = { onOpenTask(task.id) }
                )
            }
        }
        if (sections.someday.isNotEmpty()) {
            item(key = "header-someday") { SectionHeader("Someday") }
            items(sections.someday, key = { it.id }) { task ->
                TaskRow(
                    task = task,
                    stepCount = sections.stepCounts[task.id],
                    onToggleDone = { vm.setDone(task, it) },
                    onClick = { onOpenTask(task.id) }
                )
            }
        }
    }
}

@Composable
private fun DoneList(
    vm: MainViewModel,
    sections: com.addo.app.ui.Sections,
    padding: PaddingValues,
    onOpenTask: (Long) -> Unit
) {
    if (sections.doneAll.isEmpty()) {
        EmptyState(
            title = "Finished tasks land here",
            body = "Proof of everything you've already knocked out.",
            padding = padding
        )
        return
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(sections.doneAll, key = { it.id }) { task ->
            TaskRow(
                task = task,
                stepCount = sections.stepCounts[task.id],
                onToggleDone = { vm.setDone(task, it) },
                onClick = { onOpenTask(task.id) }
            )
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 4.dp, top = 12.dp, bottom = 2.dp)
    )
}

@Composable
private fun EmptyState(title: String, body: String, padding: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge)
        Text(
            body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
private fun EmptyStateInline(title: String, body: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge)
        Text(
            body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}
