package com.smaran.core.domain

import com.smaran.core.domain.usecase.AddNoteUseCase
import com.smaran.core.domain.usecase.RescheduleRemindersUseCase
import com.smaran.core.domain.usecase.SetNoteDoneUseCase
import com.smaran.core.domain.usecase.SnoozeReminderUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class ReminderUseCasesTest {
    private val zone = ZoneId.of("Asia/Kolkata")
    private val now = Instant.parse("2026-11-26T10:00:00Z") // 15:30 in India
    private val clock = Clock.fixed(now, zone)
    private val repo = FakeNoteRepository()
    private val scheduler = FakeReminderScheduler()
    private val addNote = AddNoteUseCase(repo, scheduler, clock)
    private val setDone = SetNoteDoneUseCase(repo, repo, scheduler, clock)
    private val snooze = SnoozeReminderUseCase(repo, scheduler, clock)
    private val today = LocalDate.of(2026, 11, 26)

    private suspend fun addWithReminder(text: String, date: LocalDate, time: LocalTime): Long =
        (addNote(text, date, time) as AddNoteUseCase.Result.Added).id

    @Test
    fun `done cancels the reminder and undo brings it back`() = runTest {
        val id = addWithReminder("Doctor", today, LocalTime.of(18, 0))

        setDone(id, true)
        assertFalse(scheduler.scheduled.containsKey(id))

        setDone(id, false)
        assertEquals(Instant.parse("2026-11-26T12:30:00Z"), scheduler.scheduled[id])
    }

    @Test
    fun `undo does not bring back a reminder whose time has passed`() = runTest {
        val id = addWithReminder("Doctor", today, LocalTime.of(18, 0))
        setDone(id, true)

        val later = Clock.fixed(Instant.parse("2026-11-26T13:00:00Z"), zone)
        SetNoteDoneUseCase(repo, repo, scheduler, later)(id, false)

        assertFalse(scheduler.scheduled.containsKey(id))
    }

    @Test
    fun `snooze reschedules one hour from now`() = runTest {
        val id = addWithReminder("Tablet", today, LocalTime.of(16, 0))

        snooze(id)

        assertEquals(now.plusSeconds(3_600), scheduler.scheduled[id])
    }

    @Test
    fun `snooze does nothing for a finished note`() = runTest {
        val id = addWithReminder("Tablet", today, LocalTime.of(16, 0))
        setDone(id, true)

        snooze(id)

        assertFalse(scheduler.scheduled.containsKey(id))
    }

    @Test
    fun `reschedule after restart keeps only future open reminders`() = runTest {
        val future = addWithReminder("Tomorrow walk", today.plusDays(1), LocalTime.of(7, 0))
        val done = addWithReminder("Done one", today, LocalTime.of(20, 0))
        setDone(done, true)
        addNote("No reminder")
        scheduler.scheduled.clear() // the phone restarted and forgot every alarm

        val count = RescheduleRemindersUseCase(repo, scheduler, clock)()

        assertEquals(1, count)
        assertEquals(setOf(future), scheduler.scheduled.keys)
        assertTrue(scheduler.scheduled.getValue(future).isAfter(now))
    }
}
