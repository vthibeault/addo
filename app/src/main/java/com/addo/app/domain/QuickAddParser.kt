package com.addo.app.domain

import com.addo.app.data.PRIORITY_HIGH
import com.addo.app.data.PRIORITY_LOW
import com.addo.app.data.PRIORITY_MEDIUM
import com.addo.app.data.PRIORITY_NONE
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime
import java.time.temporal.TemporalAdjusters

data class ParsedTask(
    val title: String,
    /** Epoch millis, or null when no date was recognized. */
    val dueAt: Long?,
    /** True when a specific clock time was recognized (not just a day). */
    val hasTime: Boolean,
    val priority: Int,
    val tags: List<String>
)

/**
 * Turns things like "call mom tomorrow 3pm !! #family" into a structured task.
 *
 * Recognized, all case-insensitive:
 *  - days:     today, tod, tonight, tomorrow, tmr, tmrw, tom, monday..sunday (and 3-letter forms), next week
 *  - relative: in 30m / in 30 min / in 2h / in 2 hours / in 3d / in 1w
 *  - dates:    jan 5, 5 jan, january 15 (next occurrence)
 *  - times:    3pm, 3:30pm, at 5, 15:00, noon, midnight
 *  - priority: !!! or !high → high, !! or !med → medium, ! or !low → low
 *  - tags:     #word
 *
 * Everything recognized is stripped from the title. Unrecognized text is left alone —
 * the worst case is always just "the whole thing becomes the title", never a lost task.
 */
object QuickAddParser {

    private val priorityRegex = Regex(
        """(?<=^|\s)(!!!|!!|!(?!\w)|!high|!h(?!\w)|!med(?:ium)?|!m(?!\w)|!low|!l(?!\w)|!\d)(?=\s|$)""",
        RegexOption.IGNORE_CASE
    )

    private val tagRegex = Regex("""(?<=^|\s)#([\p{L}\p{N}_-]+)""")

    private val relativeRegex = Regex(
        """(?<=^|\s)in\s+(\d{1,3})\s*(m|min|mins|minutes?|h|hr|hrs|hours?|d|days?|w|weeks?)(?=\s|$)""",
        RegexOption.IGNORE_CASE
    )

    private val dayWordRegex = Regex(
        """(?<=^|\s)(?:(on)\s+)?(today|tod|tonight|tomorrow|tmrw|tmr|tom|next\s+week|monday|mon|tuesday|tue|tues|wednesday|wed|thursday|thu|thur|thurs|friday|fri|saturday|sat|sunday|sun)(?=\s|$)""",
        RegexOption.IGNORE_CASE
    )

    private val monthDayRegex = Regex(
        """(?<=^|\s)(?:(on)\s+)?(?:(jan(?:uary)?|feb(?:ruary)?|mar(?:ch)?|apr(?:il)?|may|jun(?:e)?|jul(?:y)?|aug(?:ust)?|sep(?:t(?:ember)?)?|oct(?:ober)?|nov(?:ember)?|dec(?:ember)?)\s+(\d{1,2})|(\d{1,2})\s+(jan(?:uary)?|feb(?:ruary)?|mar(?:ch)?|apr(?:il)?|may|jun(?:e)?|jul(?:y)?|aug(?:ust)?|sep(?:t(?:ember)?)?|oct(?:ober)?|nov(?:ember)?|dec(?:ember)?))(?=\s|$)""",
        RegexOption.IGNORE_CASE
    )

    // A time needs am/pm, a colon, an "at " prefix, or a word form — a bare "2"
    // in "buy 2 apples" must never become 2 o'clock.
    private val timeRegex = Regex(
        """(?<=^|\s)(?:at\s+(\d{1,2})(?::(\d{2}))?\s*(am|pm)?|(\d{1,2}):(\d{2})\s*(am|pm)?|(\d{1,2})\s*(am|pm)|noon|midnight)(?=\s|$)""",
        RegexOption.IGNORE_CASE
    )

    private val months = mapOf(
        "jan" to 1, "feb" to 2, "mar" to 3, "apr" to 4, "may" to 5, "jun" to 6,
        "jul" to 7, "aug" to 8, "sep" to 9, "oct" to 10, "nov" to 11, "dec" to 12
    )

    fun parse(input: String, now: ZonedDateTime = ZonedDateTime.now()): ParsedTask {
        var working = input

        var priority = PRIORITY_NONE
        priorityRegex.find(working)?.let { match ->
            priority = when (match.value.lowercase().trimStart('!').ifEmpty { "!".repeat(match.value.length) }) {
                "!!!", "high", "h", "3" -> PRIORITY_HIGH
                "!!", "med", "medium", "m", "2" -> PRIORITY_MEDIUM
                else -> PRIORITY_LOW
            }
            // Bare bangs: value is "!", "!!" or "!!!"
            if (match.value.all { it == '!' }) {
                priority = when (match.value.length) {
                    3 -> PRIORITY_HIGH
                    2 -> PRIORITY_MEDIUM
                    else -> PRIORITY_LOW
                }
            }
            working = working.removeRange(match.range)
        }

        val tags = mutableListOf<String>()
        working = tagRegex.replace(working) { m ->
            tags += m.groupValues[1].lowercase()
            ""
        }

        var date: LocalDate? = null
        var time: LocalTime? = null

        relativeRegex.find(working)?.let { match ->
            val amount = match.groupValues[1].toLong()
            val unit = match.groupValues[2].lowercase()
            val target = when {
                unit.startsWith("m") -> now.plusMinutes(amount)
                unit.startsWith("h") -> now.plusHours(amount)
                unit.startsWith("d") -> now.plusDays(amount)
                else -> now.plusWeeks(amount)
            }
            date = target.toLocalDate()
            if (unit.startsWith("m") || unit.startsWith("h")) {
                time = target.toLocalTime().withSecond(0).withNano(0)
            }
            working = working.removeRange(match.range)
        }

        if (date == null) {
            dayWordRegex.find(working)?.let { match ->
                val word = match.groupValues[2].lowercase().replace(Regex("\\s+"), " ")
                when (word) {
                    "today", "tod" -> date = now.toLocalDate()
                    "tonight" -> {
                        date = now.toLocalDate()
                        if (time == null) time = LocalTime.of(20, 0)
                    }
                    "tomorrow", "tmrw", "tmr", "tom" -> date = now.toLocalDate().plusDays(1)
                    "next week" -> date = now.toLocalDate()
                        .with(TemporalAdjusters.next(DayOfWeek.MONDAY))
                    else -> {
                        val dow = when (word.take(3)) {
                            "mon" -> DayOfWeek.MONDAY
                            "tue" -> DayOfWeek.TUESDAY
                            "wed" -> DayOfWeek.WEDNESDAY
                            "thu" -> DayOfWeek.THURSDAY
                            "fri" -> DayOfWeek.FRIDAY
                            "sat" -> DayOfWeek.SATURDAY
                            else -> DayOfWeek.SUNDAY
                        }
                        date = now.toLocalDate().with(TemporalAdjusters.next(dow))
                    }
                }
                working = working.removeRange(match.range)
            }
        }

        if (date == null) {
            monthDayRegex.find(working)?.let { match ->
                val monthWord = (match.groupValues[2].ifEmpty { match.groupValues[5] }).lowercase().take(3)
                val dayNum = (match.groupValues[3].ifEmpty { match.groupValues[4] }).toInt()
                val month = months[monthWord]
                if (month != null && dayNum in 1..31) {
                    val candidate = runCatching {
                        LocalDate.of(now.year, month, dayNum)
                    }.getOrNull()
                    if (candidate != null) {
                        date = if (candidate.isBefore(now.toLocalDate())) candidate.plusYears(1) else candidate
                        working = working.removeRange(match.range)
                    }
                }
            }
        }

        timeRegex.find(working)?.let { match ->
            val parsed = parseTimeMatch(match)
            if (parsed != null) {
                time = parsed
                working = working.removeRange(match.range)
            }
        }

        var hasTime = time != null
        var due: ZonedDateTime? = null
        if (date != null || time != null) {
            var day = date ?: now.toLocalDate()
            val t = time ?: LocalTime.MIDNIGHT
            if (date == null && time != null && !time!!.isAfter(now.toLocalTime())) {
                // "at 9am" said at 11am means tomorrow 9am
                day = day.plusDays(1)
            }
            due = day.atTime(t).atZone(now.zone)
        }

        val title = working.replace(Regex("\\s+"), " ").trim()
        return ParsedTask(
            title = title,
            dueAt = due?.toInstant()?.toEpochMilli(),
            hasTime = hasTime,
            priority = priority,
            tags = tags
        )
    }

    private fun parseTimeMatch(match: MatchResult): LocalTime? {
        val text = match.value.lowercase().trim()
        if (text == "noon") return LocalTime.NOON
        if (text == "midnight") return LocalTime.MIDNIGHT

        val g = match.groupValues
        // group layouts: at H(:MM)(ampm) → 1,2,3 | H:MM(ampm) → 4,5,6 | H ampm → 7,8
        val (hourStr, minStr, ampm) = when {
            g[1].isNotEmpty() -> Triple(g[1], g[2], g[3])
            g[4].isNotEmpty() -> Triple(g[4], g[5], g[6])
            else -> Triple(g[7], "", g[8])
        }
        var hour = hourStr.toIntOrNull() ?: return null
        val minute = minStr.ifEmpty { "0" }.toIntOrNull() ?: return null
        if (minute !in 0..59) return null

        when (ampm.lowercase()) {
            "pm" -> if (hour in 1..11) hour += 12
            "am" -> if (hour == 12) hour = 0
            else -> {
                // "at 3" with no am/pm: assume the upcoming reasonable slot — 1..7 means afternoon
                if (hour in 1..7) hour += 12
            }
        }
        if (hour !in 0..23) return null
        return LocalTime.of(hour, minute)
    }
}
