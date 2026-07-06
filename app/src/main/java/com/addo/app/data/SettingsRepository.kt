package com.addo.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

private val Context.dataStore by preferencesDataStore(name = "addo_settings")

data class StreakState(val count: Int, val lastActiveEpochDay: Long)

/**
 * Streak = consecutive days with at least one completed task.
 * Missing a day resets to 1 on the next completion — gentle, never punitive:
 * the streak is only ever shown as a positive number, never as "you broke it".
 */
class SettingsRepository(private val context: Context) {

    private val streakCountKey = intPreferencesKey("streak_count")
    private val streakDayKey = longPreferencesKey("streak_last_day")

    val streak: Flow<StreakState> = context.dataStore.data.map { prefs ->
        StreakState(prefs[streakCountKey] ?: 0, prefs[streakDayKey] ?: 0L)
    }

    /** Call whenever a task is completed. Returns the streak after recording. */
    suspend fun recordCompletion(today: LocalDate = LocalDate.now()): Int {
        var result = 0
        context.dataStore.edit { prefs ->
            val lastDay = prefs[streakDayKey] ?: 0L
            val count = prefs[streakCountKey] ?: 0
            val todayEpoch = today.toEpochDay()
            val newCount = when (todayEpoch - lastDay) {
                0L -> count.coerceAtLeast(1)
                1L -> count + 1
                else -> 1
            }
            prefs[streakCountKey] = newCount
            prefs[streakDayKey] = todayEpoch
            result = newCount
        }
        return result
    }
}
