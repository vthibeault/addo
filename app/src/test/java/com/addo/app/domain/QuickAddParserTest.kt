package com.addo.app.domain

import com.addo.app.data.PRIORITY_HIGH
import com.addo.app.data.PRIORITY_LOW
import com.addo.app.data.PRIORITY_MEDIUM
import com.addo.app.data.PRIORITY_NONE
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

class QuickAddParserTest {

    private val zone = ZoneId.of("America/New_York")
    // A fixed Wednesday, 11:00 AM
    private val now: ZonedDateTime =
        ZonedDateTime.of(2026, 7, 8, 11, 0, 0, 0, zone)

    private fun parse(input: String) = QuickAddParser.parse(input, now)

    private fun localDateTime(dueAt: Long?): LocalDateTime =
        Instant.ofEpochMilli(dueAt!!).atZone(zone).toLocalDateTime()

    @Test
    fun `plain text has no date and no priority`() {
        val p = parse("buy milk")
        assertEquals("buy milk", p.title)
        assertNull(p.dueAt)
        assertEquals(PRIORITY_NONE, p.priority)
        assertTrue(p.tags.isEmpty())
    }

    @Test
    fun `numbers in text are not mistaken for times`() {
        val p = parse("buy 2 apples")
        assertEquals("buy 2 apples", p.title)
        assertNull(p.dueAt)
    }

    @Test
    fun `tomorrow with pm time`() {
        val p = parse("call mom tomorrow 3pm")
        assertEquals("call mom", p.title)
        assertTrue(p.hasTime)
        assertEquals(LocalDateTime.of(2026, 7, 9, 15, 0), localDateTime(p.dueAt))
    }

    @Test
    fun `today keyword`() {
        val p = parse("today gym")
        assertEquals("gym", p.title)
        assertFalse(p.hasTime)
        assertEquals(LocalDate.of(2026, 7, 8), localDateTime(p.dueAt).toLocalDate())
    }

    @Test
    fun `tonight sets evening time`() {
        val p = parse("read tonight")
        assertEquals("read", p.title)
        assertTrue(p.hasTime)
        assertEquals(LocalDateTime.of(2026, 7, 8, 20, 0), localDateTime(p.dueAt))
    }

    @Test
    fun `weekday name picks next occurrence`() {
        val p = parse("dentist friday")
        assertEquals("dentist", p.title)
        assertEquals(LocalDate.of(2026, 7, 10), localDateTime(p.dueAt).toLocalDate())
    }

    @Test
    fun `same weekday name means next week`() {
        val p = parse("review wednesday")
        assertEquals(LocalDate.of(2026, 7, 15), localDateTime(p.dueAt).toLocalDate())
    }

    @Test
    fun `relative minutes carries time`() {
        val p = parse("stretch in 30 min")
        assertEquals("stretch", p.title)
        assertTrue(p.hasTime)
        assertEquals(LocalDateTime.of(2026, 7, 8, 11, 30), localDateTime(p.dueAt))
    }

    @Test
    fun `relative days is all day`() {
        val p = parse("follow up in 3d")
        assertEquals("follow up", p.title)
        assertFalse(p.hasTime)
        assertEquals(LocalDate.of(2026, 7, 11), localDateTime(p.dueAt).toLocalDate())
    }

    @Test
    fun `bang priorities`() {
        assertEquals(PRIORITY_LOW, parse("water plants !").priority)
        assertEquals(PRIORITY_MEDIUM, parse("water plants !!").priority)
        assertEquals(PRIORITY_HIGH, parse("water plants !!!").priority)
        assertEquals("water plants", parse("water plants !!!").title)
    }

    @Test
    fun `word priorities`() {
        assertEquals(PRIORITY_HIGH, parse("taxes !high").priority)
        assertEquals(PRIORITY_MEDIUM, parse("taxes !med").priority)
        assertEquals(PRIORITY_LOW, parse("taxes !low").priority)
    }

    @Test
    fun `exclamation inside sentence is kept`() {
        val p = parse("email boss now!")
        assertEquals("email boss now!", p.title)
        assertEquals(PRIORITY_NONE, p.priority)
    }

    @Test
    fun `tags are extracted and lowercased`() {
        val p = parse("mow lawn #Home #chores")
        assertEquals("mow lawn", p.title)
        assertEquals(listOf("home", "chores"), p.tags)
    }

    @Test
    fun `time only in the past rolls to tomorrow`() {
        val p = parse("meds at 9am")
        assertEquals("meds", p.title)
        assertEquals(LocalDateTime.of(2026, 7, 9, 9, 0), localDateTime(p.dueAt))
    }

    @Test
    fun `time only in the future stays today`() {
        val p = parse("meds at 9pm")
        assertEquals(LocalDateTime.of(2026, 7, 8, 21, 0), localDateTime(p.dueAt))
    }

    @Test
    fun `bare hour with at gets afternoon assumption`() {
        val p = parse("pick up kids at 3")
        assertEquals("pick up kids", p.title)
        assertEquals(LocalDateTime.of(2026, 7, 8, 15, 0), localDateTime(p.dueAt))
    }

    @Test
    fun `colon time without ampm`() {
        val p = parse("standup tomorrow 9:15am")
        assertEquals("standup", p.title)
        assertEquals(LocalDateTime.of(2026, 7, 9, 9, 15), localDateTime(p.dueAt))
    }

    @Test
    fun `month day format`() {
        val p = parse("renew passport jul 20")
        assertEquals("renew passport", p.title)
        assertEquals(LocalDate.of(2026, 7, 20), localDateTime(p.dueAt).toLocalDate())
    }

    @Test
    fun `past month day rolls to next year`() {
        val p = parse("party jan 5")
        assertEquals(LocalDate.of(2027, 1, 5), localDateTime(p.dueAt).toLocalDate())
    }

    @Test
    fun `day month order too`() {
        val p = parse("pay rent 1 aug")
        assertEquals("pay rent", p.title)
        assertEquals(LocalDate.of(2026, 8, 1), localDateTime(p.dueAt).toLocalDate())
    }

    @Test
    fun `next week goes to monday`() {
        val p = parse("plan sprint next week")
        assertEquals("plan sprint", p.title)
        assertEquals(LocalDate.of(2026, 7, 13), localDateTime(p.dueAt).toLocalDate())
    }

    @Test
    fun `everything combined`() {
        val p = parse("call mom tomorrow 3pm !! #family")
        assertEquals("call mom", p.title)
        assertEquals(PRIORITY_MEDIUM, p.priority)
        assertEquals(listOf("family"), p.tags)
        assertTrue(p.hasTime)
        assertEquals(LocalDateTime.of(2026, 7, 9, 15, 0), localDateTime(p.dueAt))
    }

    @Test
    fun `noon and midnight words`() {
        assertEquals(
            LocalDateTime.of(2026, 7, 8, 12, 0),
            localDateTime(parse("lunch noon").dueAt)
        )
        assertEquals("lunch", parse("lunch noon").title)
    }

    @Test
    fun `unparseable stays as title - never lose a task`() {
        val p = parse("that thing with the stuff???")
        assertEquals("that thing with the stuff???", p.title)
        assertNull(p.dueAt)
    }
}
