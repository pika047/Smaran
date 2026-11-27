package com.smaran.core.domain.usecase

import com.smaran.core.domain.reminder.ReminderScheduler
import com.smaran.core.domain.reminder.reminderInstant
import com.smaran.core.domain.repository.NoteReader
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject

/**
 * Re-creates every future reminder. Android forgets alarms on restart,
 * and times must be recalculated when the time zone changes.
 */
class RescheduleRemindersUseCase @Inject constructor(
    private val reader: NoteReader,
    private val scheduler: ReminderScheduler,
    private val clock: Clock,
) {
    /** Returns how many reminders were scheduled. */
    suspend operator fun invoke(): Int {
        val now = Instant.now(clock)
        var count = 0
        reader.getOpenRemindersFrom(LocalDate.now(clock)).forEach { note ->
            val fireAt = note.reminderInstant(clock.zone) ?: return@forEach
            if (fireAt.isAfter(now)) {
                scheduler.schedule(note.id, fireAt)
                count++
            }
        }
        return count
    }
}
