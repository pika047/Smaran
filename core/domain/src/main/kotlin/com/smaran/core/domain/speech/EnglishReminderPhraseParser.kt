package com.smaran.core.domain.speech

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Month
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

/**
 * Understands everyday English (including Indian English) phrases, e.g.
 * "tomorrow at 5 pm", "on Monday morning", "28th November at half past 10",
 * "in 2 hours", "day after tomorrow evening".
 */
class EnglishReminderPhraseParser @Inject constructor() : ReminderPhraseParser {

    override fun parse(text: String, now: LocalDateTime): SpokenReminder? {
        val phrase = normalize(text)
        if (phrase.isBlank()) return null

        relative(phrase, now)?.let { return it }

        val date = parseDate(phrase, now.toLocalDate())
        val time = parseTime(phrase)
        return if (date == null && time == null) null else SpokenReminder(date, time)
    }

    private fun normalize(text: String): String {
        var s = " " + text.lowercase() + " "
        s = s.replace("p.m.", "pm").replace("a.m.", "am")
            .replace(Regex("[,!?]"), " ")
            .replace(Regex("(\\d)\\.(\\d\\d)"), "$1:$2") // "5.30" → "5:30"
            .replace(Regex("(\\d)(am|pm)\\b"), "$1 $2") // "5pm" → "5 pm"
        NUMBER_WORDS.forEach { (word, n) -> s = s.replace(Regex("\\b$word\\b"), n.toString()) }
        return s.replace(Regex("\\s+"), " ")
    }

    /** "in 2 hours", "in 30 minutes", "after 1 hour". */
    private fun relative(s: String, now: LocalDateTime): SpokenReminder? {
        val match = Regex("\\b(?:in|after) (\\d{1,3}|an?|half an?) (hour|hours|minute|minutes|min|mins)\\b").find(s)
            ?: return null
        val amountWord = match.groupValues[1]
        val unit = match.groupValues[2]
        val minutes = when {
            amountWord.startsWith("half") -> 30L
            unit.startsWith("hour") -> (amountWord.toLongOrNull() ?: 1L) * 60
            else -> amountWord.toLongOrNull() ?: 1L
        }
        val at = now.plusMinutes(minutes).withSecond(0).withNano(0)
        return SpokenReminder(at.toLocalDate(), at.toLocalTime())
    }

    private fun parseDate(s: String, today: LocalDate): LocalDate? {
        if (" day after tomorrow " in s) return today.plusDays(2)
        if (" tomorrow " in s) return today.plusDays(1)
        if (" today " in s || " tonight " in s) return today

        Regex("\\bin (\\d{1,2}) days?\\b").find(s)?.let { return today.plusDays(it.groupValues[1].toLong()) }

        DAYS.forEach { (name, day) ->
            if (Regex("\\b$name\\b").containsMatchIn(s)) {
                return today.with(TemporalAdjusters.next(day)) // "Monday" said on a Monday means next week.
            }
        }

        return parseCalendarDate(s, today)
    }

    /** "28 november", "28th of november", "november 28th". Past dates roll to next year. */
    private fun parseCalendarDate(s: String, today: LocalDate): LocalDate? {
        val monthPattern = MONTHS.keys.joinToString("|")
        val dayFirst = Regex("\\b(\\d{1,2})(?:st|nd|rd|th)?(?: of)? ($monthPattern)\\b").find(s)
        val monthFirst = Regex("\\b($monthPattern) (\\d{1,2})(?:st|nd|rd|th)?\\b").find(s)
        val (day, month) = when {
            dayFirst != null -> dayFirst.groupValues[1].toInt() to MONTHS.getValue(dayFirst.groupValues[2])
            monthFirst != null -> monthFirst.groupValues[2].toInt() to MONTHS.getValue(monthFirst.groupValues[1])
            else -> return null
        }
        val thisYear = runCatching { LocalDate.of(today.year, month, day) }.getOrNull() ?: return null
        return if (thisYear.isBefore(today)) thisYear.plusYears(1) else thisYear
    }

    private fun parseTime(s: String): LocalTime? {
        val partOfDay = PARTS_OF_DAY.entries.firstOrNull { (word, _) -> Regex("\\b$word\\b").containsMatchIn(s) }

        clockTime(s, partOfDay?.key)?.let { return it }

        if (Regex("\\b(noon|midday)\\b").containsMatchIn(s)) return LocalTime.NOON
        return partOfDay?.value
    }

    private fun clockTime(s: String, partOfDay: String?): LocalTime? {
        // "half past 10", "quarter past 5", "quarter to 6"
        Regex("\\b(half|quarter) past (\\d{1,2})\\b").find(s)?.let {
            val minute = if (it.groupValues[1] == "half") 30 else 15
            return toTime(it.groupValues[2].toInt(), minute, meridiem(s, it.range.last), partOfDay)
        }
        Regex("\\bquarter to (\\d{1,2})\\b").find(s)?.let {
            val hour = it.groupValues[1].toInt() - 1
            return toTime(if (hour == 0) 12 else hour, 45, meridiem(s, it.range.last), partOfDay)
        }
        // "5:30 pm", "17:00", "5 pm", "at 5", "5 o'clock"
        Regex("\\b(\\d{1,2}):(\\d{2})\\b").find(s)?.let {
            return toTime(it.groupValues[1].toInt(), it.groupValues[2].toInt(), meridiem(s, it.range.last), partOfDay)
        }
        Regex("\\b(\\d{1,2}) ?(am|pm|o'clock|oclock)\\b").find(s)?.let {
            val suffix = it.groupValues[2].takeIf { m -> m == "am" || m == "pm" }
            return toTime(it.groupValues[1].toInt(), 0, suffix, partOfDay)
        }
        Regex("\\bat (\\d{1,2})\\b").find(s)?.let {
            return toTime(it.groupValues[1].toInt(), 0, meridiem(s, it.range.last), partOfDay)
        }
        return null
    }

    private fun meridiem(s: String, after: Int): String? =
        Regex("^ ?(am|pm)\\b").find(s.substring(minOf(after + 1, s.length)))?.groupValues?.get(1)

    /**
     * Without am/pm: morning words mean am; afternoon/evening/night mean pm;
     * otherwise 1–6 is taken as pm (people rarely set reminders for 3 at night) and 7–11 as am.
     */
    private fun toTime(hour: Int, minute: Int, meridiem: String?, partOfDay: String?): LocalTime? {
        if (minute !in 0..59) return null
        if (meridiem == null && partOfDay == null && hour in 13..23) return LocalTime.of(hour, minute)
        if (hour == 0 && meridiem == null) return LocalTime.of(0, minute)
        if (hour !in 1..12) return null
        val pm = when (meridiem) {
            "pm" -> true
            "am" -> false
            else -> when (partOfDay) {
                "morning" -> false
                "afternoon", "evening", "night", "tonight" -> true
                else -> hour in 1..6 || hour == 12
            }
        }
        val h24 = when {
            pm && hour < 12 -> hour + 12
            !pm && hour == 12 -> 0
            else -> hour
        }
        return LocalTime.of(h24, minute)
    }

    private companion object {
        val NUMBER_WORDS = listOf(
            "twelve" to 12, "eleven" to 11, "ten" to 10, "nine" to 9, "eight" to 8, "seven" to 7,
            "six" to 6, "five" to 5, "four" to 4, "three" to 3, "two" to 2, "one" to 1,
        )
        val DAYS = DayOfWeek.entries.map { it.name.lowercase() to it }
        val MONTHS: Map<String, Month> = Month.entries.associateBy { it.name.lowercase() } +
            Month.entries.associateBy { it.name.lowercase().take(3) }.filterKeys { it != "may" }
        val PARTS_OF_DAY = linkedMapOf(
            "morning" to LocalTime.of(9, 0),
            "afternoon" to LocalTime.of(14, 0),
            "evening" to LocalTime.of(18, 0),
            "tonight" to LocalTime.of(20, 0),
            "night" to LocalTime.of(20, 0),
        )
    }
}
