package com.smaran.core.domain.usecase

import com.smaran.core.domain.speech.ReminderPhraseParser
import com.smaran.core.domain.speech.SpokenReminder
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * Turns a spoken phrase into a suggested reminder day and time.
 * A time with no day means today if it is still ahead, otherwise tomorrow.
 */
class ParseSpokenReminderUseCase @Inject constructor(
    private val parser: ReminderPhraseParser,
    private val clock: Clock,
) {
    operator fun invoke(text: String): SpokenReminder? {
        val now = LocalDateTime.now(clock)
        val parsed = parser.parse(text, now) ?: return null
        if (parsed.date != null || parsed.time == null) return parsed
        val today = LocalDate.now(clock)
        val date = if (parsed.time.isAfter(now.toLocalTime())) today else today.plusDays(1)
        return parsed.copy(date = date)
    }
}
