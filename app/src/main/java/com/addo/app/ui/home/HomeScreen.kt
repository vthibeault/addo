package com.addo.app.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.addo.app.data.Task
import com.addo.app.ui.MainViewModel
import com.addo.app.ui.Sections
import com.addo.app.ui.components.QuickAddBar
import com.addo.app.ui.components.TaskRow
import com.addo.app.ui.theme.AddoColors

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
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = when (tab) {
                                HomeTab.Today -> "Today"
                                HomeTab.Later -> "Later"
                                HomeTab.Done -> "Done ✨"
                            },
                            style = MaterialTheme.typography.headlineMedium
                        )
                        if (streak > 0) {
                            StreakBadge(streak, modifier = Modifier.padding(start = 10.dp))
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
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    windowInsets = NavigationBarDefaults.windowInsets
                ) {
                    val itemColors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                        selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        selectedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                    NavigationBarItem(
                        selected = tab == HomeTab.Today,
                        onClick = { tab = HomeTab.Today },
                        icon = { Icon(Icons.Filled.WbSunny, contentDescription = null) },
                        label = { Text(HomeTab.Today.label) },
                        colors = itemColors
                    )
                    NavigationBarItem(
                        selected = tab == HomeTab.Later,
                        onClick = { tab = HomeTab.Later },
                        icon = { Icon(Icons.Outlined.CalendarMonth, contentDescription = null) },
                        label = { Text(HomeTab.Later.label) },
                        colors = itemColors
                    )
                    NavigationBarItem(
                        selected = tab == HomeTab.Done,
                        onClick = { tab = HomeTab.Done },
                        icon = { Icon(Icons.Filled.TaskAlt, contentDescription = null) },
                        label = { Text(HomeTab.Done.label) },
                        colors = itemColors
                    )
                }
            }
        }
    ) { padding ->
        AnimatedContent(
            targetState = tab,
            transitionSpec = {
                (slideInVertically(spring(stiffness = 500f)) { it / 12 } + fadeIn()) togetherWith fadeOut(tween(90))
            },
            label = "tabContent"
        ) { current ->
            when (current) {
                HomeTab.Today -> TodayList(vm, sections, padding, onOpenTask)
                HomeTab.Later -> LaterList(vm, sections, padding, onOpenTask)
                HomeTab.Done -> DoneList(vm, sections, padding, onOpenTask)
            }
        }
    }
}

@Composable
private fun StreakBadge(streak: Int, modifier: Modifier = Modifier) {
    val pulse by rememberInfiniteTransition(label = "streak").animateFloat(
        initialValue = 1f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
        label = "streakPulse"
    )
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🔥", fontSize = 16.sp, modifier = Modifier.scale(pulse))
            Text(
                " $streak",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
private fun TodayList(
    vm: MainViewModel,
    sections: Sections,
    padding: PaddingValues,
    onOpenTask: (Long) -> Unit
) {
    val hasAnything = sections.todayRemaining > 0 || sections.doneToday.isNotEmpty()
    if (!hasAnything) {
        EmptyState("🌤️", "Nothing on your plate", "Add one small thing below. Just one. 👇", padding)
        return
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item(key = "progress") {
            TodayProgressCard(
                done = sections.doneToday.size,
                total = sections.doneToday.size + sections.todayRemaining,
                modifier = Modifier.animateItem()
            )
        }
        if (sections.todayRemaining >= 2) {
            item(key = "pick") {
                PickForMeButton(onClick = vm::pickForMe, modifier = Modifier.animateItem())
            }
        }
        if (sections.carriedOver.isNotEmpty()) {
            item(key = "header-carried") {
                SectionHeader("💌 Carried over — today's a fresh start", Modifier.animateItem())
            }
            items(sections.carriedOver, key = { it.id }) { task ->
                TaskRow(
                    task = task,
                    stepCount = sections.stepCounts[task.id],
                    onToggleDone = { vm.setDone(task, it) },
                    onClick = { onOpenTask(task.id) },
                    modifier = Modifier.animateItem()
                )
            }
        }
        if (sections.today.isNotEmpty()) {
            if (sections.carriedOver.isNotEmpty()) {
                item(key = "header-today") { SectionHeader("☀️ Today", Modifier.animateItem()) }
            }
            items(sections.today, key = { it.id }) { task ->
                TaskRow(
                    task = task,
                    stepCount = sections.stepCounts[task.id],
                    onToggleDone = { vm.setDone(task, it) },
                    onClick = { onOpenTask(task.id) },
                    modifier = Modifier.animateItem()
                )
            }
        }
        if (sections.todayRemaining == 0 && sections.doneToday.isNotEmpty()) {
            item(key = "all-clear") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp)
                        .animateItem(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    BobbingEmoji("🏝️", size = 44.sp)
                    Text("All clear!", style = MaterialTheme.typography.headlineSmall)
                    Text(
                        "Everything's done. Rest is productive too.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        if (sections.doneToday.isNotEmpty()) {
            item(key = "header-done-today") {
                SectionHeader("✅ Done today (${sections.doneToday.size})", Modifier.animateItem())
            }
            items(sections.doneToday, key = { it.id }) { task ->
                TaskRow(
                    task = task,
                    stepCount = sections.stepCounts[task.id],
                    onToggleDone = { vm.setDone(task, it) },
                    onClick = { onOpenTask(task.id) },
                    showDue = false,
                    modifier = Modifier.animateItem()
                )
            }
        }
    }
}

@Composable
private fun TodayProgressCard(done: Int, total: Int, modifier: Modifier = Modifier) {
    val fraction by animateFloatAsState(
        targetValue = if (total == 0) 0f else done.toFloat() / total,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 120f),
        label = "progress"
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(
                Brush.linearGradient(listOf(AddoColors.coral, AddoColors.sunshine))
            )
            .padding(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$done of $total done",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = when {
                        total == 0 -> ""
                        done == total -> "You did it! 🎉"
                        fraction >= 0.5f -> "Over halfway 🔥"
                        done > 0 -> "Rolling! 💪"
                        else -> "One tiny start 🌱"
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.95f)
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.3f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction.coerceIn(0f, 1f))
                        .height(12.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }
        }
    }
}

@Composable
private fun PickForMeButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val wobble by rememberInfiniteTransition(label = "dice").animateFloat(
        initialValue = -14f,
        targetValue = 14f,
        animationSpec = infiniteRepeatable(tween(450), RepeatMode.Reverse),
        label = "diceWobble"
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(Brush.linearGradient(listOf(AddoColors.grape, AddoColors.bubblegum)))
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "🎲",
                fontSize = 20.sp,
                modifier = Modifier.rotate(wobble)
            )
            Text(
                "  Can't decide? Roll for me",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
        }
    }
}

@Composable
private fun LaterList(
    vm: MainViewModel,
    sections: Sections,
    padding: PaddingValues,
    onOpenTask: (Long) -> Unit
) {
    if (sections.upcoming.isEmpty() && sections.someday.isEmpty()) {
        EmptyState("🗓️", "Nothing scheduled ahead", "Add a task with a day — \"dentist friday\" — and it lands here.", padding)
        return
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (sections.upcoming.isNotEmpty()) {
            item(key = "header-upcoming") { SectionHeader("⏭️ Coming up", Modifier.animateItem()) }
            items(sections.upcoming, key = { it.id }) { task ->
                TaskRow(
                    task = task,
                    stepCount = sections.stepCounts[task.id],
                    onToggleDone = { vm.setDone(task, it) },
                    onClick = { onOpenTask(task.id) },
                    modifier = Modifier.animateItem()
                )
            }
        }
        if (sections.someday.isNotEmpty()) {
            item(key = "header-someday") { SectionHeader("💭 Someday", Modifier.animateItem()) }
            items(sections.someday, key = { it.id }) { task ->
                TaskRow(
                    task = task,
                    stepCount = sections.stepCounts[task.id],
                    onToggleDone = { vm.setDone(task, it) },
                    onClick = { onOpenTask(task.id) },
                    modifier = Modifier.animateItem()
                )
            }
        }
    }
}

@Composable
private fun DoneList(
    vm: MainViewModel,
    sections: Sections,
    padding: PaddingValues,
    onOpenTask: (Long) -> Unit
) {
    if (sections.doneAll.isEmpty()) {
        EmptyState("🏆", "Finished tasks land here", "Proof of everything you've already knocked out.", padding)
        return
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(sections.doneAll, key = { it.id }) { task ->
            TaskRow(
                task = task,
                stepCount = sections.stepCounts[task.id],
                onToggleDone = { vm.setDone(task, it) },
                onClick = { onOpenTask(task.id) },
                modifier = Modifier.animateItem()
            )
        }
    }
}

@Composable
private fun SectionHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(start = 4.dp, top = 10.dp)
    )
}

@Composable
private fun BobbingEmoji(emoji: String, size: androidx.compose.ui.unit.TextUnit) {
    val bob by rememberInfiniteTransition(label = "bob").animateFloat(
        initialValue = 0f,
        targetValue = -10f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "bobOffset"
    )
    Text(emoji, fontSize = size, modifier = Modifier.offset(y = bob.dp))
}

@Composable
private fun EmptyState(emoji: String, title: String, body: String, padding: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BobbingEmoji(emoji, size = 56.sp)
        Text(title, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(top = 12.dp))
        Text(
            body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}
