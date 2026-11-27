package com.smaran.core.domain.usecase

import com.smaran.core.domain.reminder.ReminderScheduler
import com.smaran.core.domain.repository.NoteReader
import java.time.Clock
import java.time.Duration
import java.time.Instant
import javax.inject.Inject

/** "Remind me later": fires the reminder again after [SNOOZE]. */
class SnoozeReminderUseCase @Inject constructor(
    private val reader: NoteReader,
    private val scheduler: ReminderScheduler,
    private val clock: Clock,
) {
    suspend operator fun invoke(id: Long) {
        val note = reader.getNote(id) ?: return
        if (note.isDone) return
        scheduler.schedule(id, Instant.now(clock).plus(SNOOZE))
    }

    companion object {
        val SNOOZE: Duration = Duration.ofHours(1)
    }
}
