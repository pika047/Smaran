package com.smaran.core.domain

import com.smaran.core.domain.reminder.ReminderScheduler
import java.time.Instant

class FakeReminderScheduler : ReminderScheduler {
    val scheduled = mutableMapOf<Long, Instant>()

    override fun schedule(noteId: Long, at: Instant) {
        scheduled[noteId] = at
    }

    override fun cancel(noteId: Long) {
        scheduled.remove(noteId)
    }
}
