package com.smaran.core.domain.usecase

import com.smaran.core.domain.reminder.ReminderScheduler
import com.smaran.core.domain.reminder.reminderInstant
import com.smaran.core.domain.repository.NoteReader
import com.smaran.core.domain.repository.NoteWriter
import java.time.Clock
import java.time.Instant
import javax.inject.Inject

/**
 * Marks a note done (cancelling its reminder), or undoes that
 * (bringing the reminder back if its time has not passed).
 */
class SetNoteDoneUseCase @Inject constructor(
    private val reader: NoteReader,
    private val writer: NoteWriter,
    private val scheduler: ReminderScheduler,
    private val clock: Clock,
) {
    suspend operator fun invoke(id: Long, done: Boolean) {
        writer.setDone(id, done)
        if (done) {
            scheduler.cancel(id)
            return
        }
        val fireAt = reader.getNote(id)?.reminderInstant(clock.zone) ?: return
        if (fireAt.isAfter(Instant.now(clock))) scheduler.schedule(id, fireAt)
    }
}
