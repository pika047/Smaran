package com.smaran.core.domain

import com.smaran.core.domain.usecase.AddNoteUseCase
import com.smaran.core.domain.usecase.ObserveTodayNotesUseCase
import com.smaran.core.domain.usecase.SetNoteDoneUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class TodayNotesUseCaseTest {
    // 00:30 on 27 Nov in India, while UTC is still on 26 Nov.
    private val clock = Clock.fixed(Instant.parse("2026-11-26T19:00:00Z"), ZoneId.of("Asia/Kolkata"))
    private val repo = FakeNoteRepository()
    private val scheduler = FakeReminderScheduler()
    private val addNote = AddNoteUseCase(repo, scheduler, clock)
    private val observeToday = ObserveTodayNotesUseCase(repo, clock)
    private val setDone = SetNoteDoneUseCase(repo, repo, scheduler, clock)

    @Test
    fun `shows only notes for today in the phone's time zone`() = runTest {
        addNote("Today's note")
        addNote("Next week", LocalDate.of(2026, 12, 3))

        val today = observeToday().first()

        assertEquals(listOf("Today's note"), today.map { it.text })
        assertEquals(LocalDate.of(2026, 11, 27), today.single().date)
    }

    @Test
    fun `marking done can be undone`() = runTest {
        val id = (addNote("Water plants") as AddNoteUseCase.Result.Added).id

        setDone(id, true)
        assertTrue(observeToday().first().single().isDone)

        setDone(id, false)
        assertEquals(false, observeToday().first().single().isDone)
    }
}
