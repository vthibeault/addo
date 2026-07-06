package com.addo.app.domain

import com.addo.app.data.Task
import kotlin.random.Random

/**
 * "Pick for me": weighted random choice for when choosing is the hard part.
 * Weighting nudges toward urgent/important, but any task can win — a guaranteed
 * "always the scariest task" picker would just get avoided.
 */
object TaskPicker {

    fun pick(candidates: List<Task>, random: Random = Random.Default): Task? {
        if (candidates.isEmpty()) return null
        if (candidates.size == 1) return candidates.first()

        val now = System.currentTimeMillis()
        val weights = candidates.map { task ->
            var w = 1.0
            w += task.priority * 1.5
            if (task.isOverdue(now)) w += 2.0
            if (task.isDueToday()) w += 1.0
            w
        }
        var roll = random.nextDouble() * weights.sum()
        for (i in candidates.indices) {
            roll -= weights[i]
            if (roll <= 0) return candidates[i]
        }
        return candidates.last()
    }
}
