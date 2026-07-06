package com.addo.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.addo.app.AddoApp
import com.addo.app.data.Task
import com.addo.app.domain.QuickAddParser
import com.addo.app.domain.TaskPicker
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/** Everything the home screens need, derived from the single task list. */
data class Sections(
    val carriedOver: List<Task> = emptyList(),
    val today: List<Task> = emptyList(),
    val doneToday: List<Task> = emptyList(),
    val upcoming: List<Task> = emptyList(),
    val someday: List<Task> = emptyList(),
    val doneAll: List<Task> = emptyList(),
    /** parentId → (done steps, total steps) */
    val stepCounts: Map<Long, Pair<Int, Int>> = emptyMap()
) {
    val todayRemaining: Int get() = carriedOver.size + today.size
}

data class TimerState(
    val totalMillis: Long = 25 * 60_000L,
    val remainingMillis: Long = 25 * 60_000L,
    val running: Boolean = false,
    val finished: Boolean = false
)

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = (app as AddoApp).repository
    private val settings = app.settings

    val tasks: StateFlow<List<Task>> = repo.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val sections: StateFlow<Sections> = tasks
        .map { buildSections(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Sections())

    val streak: StateFlow<Int> = settings.streak
        .map { state ->
            val today = LocalDate.now().toEpochDay()
            // Show yesterday's streak as still alive (today isn't over), older ones as 0.
            if (today - state.lastActiveEpochDay <= 1) state.count else 0
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    /** Fires when the user checks off the last remaining task of the day. */
    private val _celebrate = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val celebrate: SharedFlow<Unit> = _celebrate

    /** Task id picked by "pick for me", consumed by navigation. */
    private val _pickedTaskId = MutableSharedFlow<Long>(extraBufferCapacity = 1)
    val pickedTaskId: SharedFlow<Long> = _pickedTaskId

    // ---- Quick add ----------------------------------------------------------

    fun quickAdd(input: String) {
        val parsed = QuickAddParser.parse(input)
        if (parsed.title.isBlank()) return
        viewModelScope.launch {
            val zone = ZoneId.systemDefault()
            // No date mentioned → it lands on Today. Capture first, plan never.
            val dueAt = parsed.dueAt
                ?: LocalDate.now(zone).atStartOfDay(zone).toInstant().toEpochMilli()
            repo.add(
                Task(
                    title = parsed.title,
                    dueAt = dueAt,
                    isAllDay = !parsed.hasTime,
                    priority = parsed.priority,
                    tags = parsed.tags.joinToString(","),
                    // A task with a specific time gets a reminder automatically —
                    // "I set it and forgot it existed" is the exact failure we're for.
                    reminderAt = if (parsed.hasTime) parsed.dueAt else null
                )
            )
        }
    }

    // ---- Task actions -------------------------------------------------------

    fun setDone(task: Task, done: Boolean) {
        viewModelScope.launch {
            if (done && task.parentId == null) {
                val remaining = sections.value.let { it.carriedOver + it.today }
                    .count { it.id != task.id }
                if (remaining == 0) _celebrate.tryEmit(Unit)
                settings.recordCompletion()
            }
            repo.setDone(task, done)
        }
    }

    fun update(task: Task) {
        viewModelScope.launch { repo.update(task) }
    }

    fun delete(task: Task) {
        viewModelScope.launch { repo.delete(task) }
    }

    fun addSubtask(parentId: Long, title: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            repo.add(Task(title = title.trim(), parentId = parentId, dueAt = null))
        }
    }

    fun subtasksFlow(parentId: Long) = repo.observeSubtasks(parentId)

    fun taskFlow(id: Long) = repo.observeById(id)

    fun moveToToday(task: Task) {
        val zone = ZoneId.systemDefault()
        val startOfToday = LocalDate.now(zone).atStartOfDay(zone).toInstant().toEpochMilli()
        update(task.copy(dueAt = startOfToday, isAllDay = true, reminderAt = null))
    }

    fun moveToTomorrow(task: Task) {
        val zone = ZoneId.systemDefault()
        val tomorrow = LocalDate.now(zone).plusDays(1)
        if (task.isAllDay || task.dueAt == null) {
            update(
                task.copy(
                    dueAt = tomorrow.atStartOfDay(zone).toInstant().toEpochMilli(),
                    isAllDay = true
                )
            )
        } else {
            val time = java.time.Instant.ofEpochMilli(task.dueAt).atZone(zone).toLocalTime()
            val newDue = tomorrow.atTime(time).atZone(zone).toInstant().toEpochMilli()
            update(task.copy(dueAt = newDue, reminderAt = task.reminderAt?.let { newDue }))
        }
    }

    fun pickForMe() {
        val candidates = sections.value.let { it.carriedOver + it.today }
        TaskPicker.pick(candidates)?.let { _pickedTaskId.tryEmit(it.id) }
    }

    // ---- Focus timer --------------------------------------------------------
    // Lives here (not in the screen) so leaving the focus screen doesn't kill
    // a running countdown.

    private val _focusTaskId = MutableStateFlow<Long?>(null)
    val focusTaskId: StateFlow<Long?> = _focusTaskId.asStateFlow()

    private val _timer = MutableStateFlow(TimerState())
    val timer: StateFlow<TimerState> = _timer.asStateFlow()

    private var timerJob: Job? = null

    fun openFocus(taskId: Long) {
        if (_focusTaskId.value != taskId) {
            stopTimer()
            _timer.value = TimerState()
        }
        _focusTaskId.value = taskId
    }

    fun setTimerDuration(minutes: Int) {
        stopTimer()
        val millis = minutes * 60_000L
        _timer.value = TimerState(totalMillis = millis, remainingMillis = millis)
    }

    fun toggleTimer() {
        val state = _timer.value
        if (state.running) {
            stopTimer()
        } else {
            val base = if (state.finished || state.remainingMillis <= 0) state.totalMillis else state.remainingMillis
            _timer.value = state.copy(remainingMillis = base, running = true, finished = false)
            timerJob = viewModelScope.launch {
                var endAt = System.currentTimeMillis() + _timer.value.remainingMillis
                while (true) {
                    delay(250)
                    val remaining = endAt - System.currentTimeMillis()
                    if (remaining <= 0) {
                        _timer.value = _timer.value.copy(remainingMillis = 0, running = false, finished = true)
                        break
                    }
                    _timer.value = _timer.value.copy(remainingMillis = remaining)
                }
            }
        }
    }

    fun resetTimer() {
        stopTimer()
        _timer.value = _timer.value.copy(remainingMillis = _timer.value.totalMillis, finished = false)
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
        _timer.value = _timer.value.copy(running = false)
    }

    // ---- Section building ---------------------------------------------------

    private fun buildSections(all: List<Task>): Sections {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val startOfToday = today.atStartOfDay(zone).toInstant().toEpochMilli()

        val stepCounts = all.filter { it.parentId != null }
            .groupBy { it.parentId!! }
            .mapValues { (_, steps) -> steps.count { it.done } to steps.size }

        val topLevel = all.filter { it.parentId == null }
        val (done, open) = topLevel.partition { it.done }

        val carriedOver = mutableListOf<Task>()
        val dueToday = mutableListOf<Task>()
        val upcoming = mutableListOf<Task>()
        val someday = mutableListOf<Task>()

        for (task in open) {
            val day = task.dueDay(zone)
            when {
                day == null -> someday += task
                day.isBefore(today) -> carriedOver += task
                day == today -> dueToday += task
                else -> upcoming += task
            }
        }

        val byUrgency = compareByDescending<Task> { it.priority }
            .thenBy { it.isAllDay }
            .thenBy { it.dueAt ?: Long.MAX_VALUE }
            .thenBy { it.createdAt }

        return Sections(
            carriedOver = carriedOver.sortedWith(byUrgency),
            today = dueToday.sortedWith(byUrgency),
            doneToday = done.filter { (it.completedAt ?: 0) >= startOfToday }
                .sortedByDescending { it.completedAt },
            upcoming = upcoming.sortedWith(compareBy({ it.dueAt }, { -it.priority })),
            someday = someday.sortedByDescending { it.createdAt },
            doneAll = done.sortedByDescending { it.completedAt ?: 0 },
            stepCounts = stepCounts
        )
    }
}
