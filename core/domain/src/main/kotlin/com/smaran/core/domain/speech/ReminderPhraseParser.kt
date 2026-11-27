package com.smaran.core.domain.speech

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/** What a spoken phrase said about when to be reminded. Either part may be missing. */
data class SpokenReminder(val date: LocalDate?, val time: LocalTime?)

/**
 * Reads a spoken phrase such as "tomorrow at 5 pm" in one language.
 * Adding a language means adding one implementation; nothing else changes.
 * Results are only suggestions: the person always confirms on screen.
 */
interface ReminderPhraseParser {
    fun parse(text: String, now: LocalDateTime): SpokenReminder?
}
