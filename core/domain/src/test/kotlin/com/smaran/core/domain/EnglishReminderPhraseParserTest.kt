package com.smaran.core.domain

import com.smaran.core.domain.speech.EnglishReminderPhraseParser
import com.smaran.core.domain.speech.SpokenReminder
import com.smaran.core.domain.usecase.ParseSpokenReminderUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class EnglishReminderPhraseParserTest {
    private val parser = EnglishReminderPhraseParser()

    // Thursday 26 Nov 2026, 3:30 PM
    private val now = LocalDateTime.of(2026, 11, 26, 15, 30)
    private val today = now.toLocalDate()

    private fun parse(text: String) = parser.parse(text, now)
    private fun t(h: Int, m: Int = 0) = LocalTime.of(h, m)

    @Test fun `tomorrow at 5 pm`() = assertEquals(SpokenReminder(today.plusDays(1), t(17)), parse("Tomorrow at 5 p.m."))
    @Test fun `5pm without space`() = assertEquals(t(17), parse("tomorrow 5pm")?.time)
    @Test fun `half past ten in the morning`() = assertEquals(t(10, 30), parse("half past ten in the morning")?.time)
    @Test fun `quarter to six`() = assertEquals(t(17, 45), parse("quarter to six")?.time)
    @Test fun `quarter past nine am`() = assertEquals(t(9, 15), parse("quarter past 9 am")?.time)
    @Test fun `24 hour clock`() = assertEquals(t(17, 0), parse("today 17:00")?.time)
    @Test fun `dotted time`() = assertEquals(t(17, 30), parse("5.30 pm")?.time)
    @Test fun `bare small hour means afternoon`() = assertEquals(t(17), parse("at 5")?.time)
    @Test fun `bare hour with morning means am`() = assertEquals(t(8), parse("tomorrow morning at 8")?.time)
    @Test fun `bare hour with evening means pm`() = assertEquals(t(19), parse("evening at 7")?.time)
    @Test fun `part of day alone`() = assertEquals(SpokenReminder(today.plusDays(1), t(9)), parse("tomorrow morning"))
    @Test fun `tonight`() = assertEquals(SpokenReminder(today, t(20)), parse("tonight"))
    @Test fun `noon`() = assertEquals(t(12), parse("tomorrow noon")?.time)
    @Test fun `day after tomorrow`() = assertEquals(today.plusDays(2), parse("day after tomorrow evening")?.date)
    @Test fun `weekday is the next one`() = assertEquals(LocalDate.of(2026, 11, 30), parse("on Monday")?.date)
    @Test fun `same weekday means next week`() = assertEquals(LocalDate.of(2026, 12, 3), parse("thursday")?.date)
    @Test fun `in three days`() = assertEquals(today.plusDays(3), parse("in three days")?.date)
    @Test fun `date with ordinal`() = assertEquals(LocalDate.of(2026, 12, 28), parse("28th of December at 10 am")?.date)
    @Test fun `month first`() = assertEquals(LocalDate.of(2026, 12, 5), parse("December 5")?.date)
    @Test fun `past date rolls to next year`() = assertEquals(LocalDate.of(2027, 1, 2), parse("2nd january")?.date)
    @Test fun `in two hours`() = assertEquals(SpokenReminder(today, t(17, 30)), parse("in two hours"))
    @Test fun `in half an hour`() = assertEquals(t(16, 0), parse("remind me in half an hour")?.time)
    @Test fun `in an hour late at night crosses midnight`() =
        assertEquals(SpokenReminder(today.plusDays(1), t(0, 10)), parser.parse("in an hour", LocalDateTime.of(2026, 11, 26, 23, 10)))
    @Test fun `nothing about time`() = assertNull(parse("buy some milk"))
    @Test fun `invalid date is ignored`() = assertNull(parse("31st february"))

    @Test
    fun `time only means today if still ahead, otherwise tomorrow`() {
        val clock = Clock.fixed(now.atZone(ZoneId.of("Asia/Kolkata")).toInstant(), ZoneId.of("Asia/Kolkata"))
        val useCase = ParseSpokenReminderUseCase(parser, clock)

        assertEquals(SpokenReminder(today, t(17)), useCase("5 pm"))
        assertEquals(SpokenReminder(today.plusDays(1), t(9)), useCase("9 am"))
    }
}
