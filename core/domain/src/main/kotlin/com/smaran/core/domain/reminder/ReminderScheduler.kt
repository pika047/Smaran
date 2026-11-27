package com.smaran.core.domain.reminder

import java.time.Instant

/**
 * Wakes the app at a given moment to remind the person about a note.
 * Scheduling again for the same note replaces the earlier time.
 */
interface ReminderScheduler {
    fun schedule(noteId: Long, at: Instant)

    /** Cancels the alarm and removes any notification already showing for the note. */
    fun cancel(noteId: Long)
}
