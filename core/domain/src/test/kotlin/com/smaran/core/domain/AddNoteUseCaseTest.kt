package com.smaran.core.domain

import com.smaran.core.domain.usecase.AddNoteUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class AddNoteUseCaseTest {
    private val zone = ZoneId.of("Asia/Kolkata")
    private val now = Instant.parse("2026-11-26T10:00:00Z")
    private val clock = Clock.fixed(now, zone)
    private val repo = FakeNoteRepository()
    private val addNote = AddNoteUseCase(repo, clock)

    @Test
    fun `saves trimmed text under today's date by default`() = runTest {
        val result = addNote("  Take blood pressure tablet  ")

        assertTrue(result is AddNoteUseCase.Result.Added)
        val saved = repo.all.single()
        assertEquals("Take blood pressure tablet", saved.text)
        assertEquals(LocalDate.of(2026, 11, 26), saved.date)
        assertEquals(now, saved.createdAt)
    }

    @Test
    fun `keeps a chosen date and reminder time`() = runTest {
        val date = LocalDate.of(2026, 12, 1)
        val time = LocalTime.of(17, 0)

        addNote("Call Ravi", date, time)

        val saved = repo.all.single()
        assertEquals(date, saved.date)
        assertEquals(time, saved.remindAt)
    }

    @Test
    fun `rejects blank text without saving`() = runTest {
        val result = addNote("   ")

        assertEquals(AddNoteUseCase.Result.EmptyText, result)
        assertTrue(repo.all.isEmpty())
    }

    @Test
    fun `rejects text over the limit without saving`() = runTest {
        val result = addNote("a".repeat(AddNoteUseCase.MAX_LENGTH + 1))

        assertEquals(AddNoteUseCase.Result.TooLong(AddNoteUseCase.MAX_LENGTH), result)
        assertTrue(repo.all.isEmpty())
    }
}
