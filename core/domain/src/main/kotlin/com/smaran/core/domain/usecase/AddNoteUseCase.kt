package com.smaran.core.domain.usecase

import com.smaran.core.domain.reminder.ReminderScheduler
import com.smaran.core.domain.reminder.reminderInstant
import com.smaran.core.domain.repository.NoteWriter
import com.smaran.core.model.NewNote
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

/**
 * Validates and saves a new note, and schedules its reminder if it has one.
 * Defaults to today when no date is given.
 */
class AddNoteUseCase @Inject constructor(
    private val writer: NoteWriter,
    private val scheduler: ReminderScheduler,
    private val clock: Clock,
) {
    suspend operator fun invoke(
        text: String,
        date: LocalDate? = null,
        remindAt: LocalTime? = null,
    ): Result {
        val cleaned = text.trim()
        if (cleaned.isEmpty()) return Result.EmptyText
        if (cleaned.length > MAX_LENGTH) return Result.TooLong(MAX_LENGTH)

        val noteDate = date ?: LocalDate.now(clock)
        val fireAt = remindAt?.let { reminderInstant(noteDate, it, clock.zone) }
        if (fireAt != null && !fireAt.isAfter(Instant.now(clock))) return Result.ReminderInPast

        val id = writer.add(
            NewNote(
                text = cleaned,
                date = noteDate,
                remindAt = remindAt,
                createdAt = Instant.now(clock),
            ),
        )
        fireAt?.let { scheduler.schedule(id, it) }
        return Result.Added(id)
    }

    sealed interface Result {
        data class Added(val id: Long) : Result
        data object EmptyText : Result
        data class TooLong(val maxLength: Int) : Result
        data object ReminderInPast : Result
    }

    companion object {
        const val MAX_LENGTH = 2_000
    }
}
