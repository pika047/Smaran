package com.smaran.feature.notes

import com.smaran.core.domain.reminder.ReminderScheduler
import com.smaran.core.domain.repository.NoteWriter
import com.smaran.core.domain.usecase.AddNoteUseCase
import com.smaran.core.model.NewNote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneOffset

@OptIn(ExperimentalCoroutinesApi::class)
class AddNoteViewModelTest {
    private val saved = mutableListOf<NewNote>()
    private val scheduledIds = mutableListOf<Long>()
    private val writer = object : NoteWriter {
        override suspend fun add(note: NewNote): Long = saved.add(note).let { saved.size.toLong() }
        override suspend fun setDone(id: Long, done: Boolean) = Unit
        override suspend fun delete(id: Long) = Unit
    }
    private val scheduler = object : ReminderScheduler {
        override fun schedule(noteId: Long, at: Instant) { scheduledIds += noteId }
        override fun cancel(noteId: Long) = Unit
    }
    // 10:00 UTC; the test uses UTC as the phone's zone.
    private val clock = Clock.fixed(Instant.parse("2026-11-26T10:00:00Z"), ZoneOffset.UTC)
    private lateinit var viewModel: AddNoteViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = AddNoteViewModel(AddNoteUseCase(writer, scheduler, clock), clock)
    }

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `blank text stays on the first step with an error`() {
        viewModel.onTextChange("  ")
        viewModel.onTextDone()

        assertEquals(AddNoteStep.Write, viewModel.uiState.value.step)
        assertEquals(AddNoteError.Empty, viewModel.uiState.value.error)
    }

    @Test
    fun `no reminder saves straight away`() {
        viewModel.onTextChange("Buy milk")
        viewModel.onTextDone()
        assertEquals(AddNoteStep.ChooseDay, viewModel.uiState.value.step)

        viewModel.onNoReminder()

        assertTrue(viewModel.uiState.value.isSaved)
        assertEquals(null, saved.single().remindAt)
        assertTrue(scheduledIds.isEmpty())
    }

    @Test
    fun `tomorrow evening saves and schedules`() {
        viewModel.onTextChange("Call Ravi")
        viewModel.onTextDone()
        viewModel.onDayChosen(viewModel.today.plusDays(1))
        assertEquals(AddNoteStep.ChooseTime, viewModel.uiState.value.step)

        viewModel.onTimeChosen(AddNoteViewModel.EVENING)

        assertTrue(viewModel.uiState.value.isSaved)
        assertEquals(viewModel.today.plusDays(1), saved.single().date)
        assertEquals(listOf(1L), scheduledIds)
    }

    @Test
    fun `a time already passed today shows an error and saves nothing`() {
        viewModel.onTextChange("Too late")
        viewModel.onTextDone()
        viewModel.onDayChosen(viewModel.today)

        viewModel.onTimeChosen(LocalTime.of(9, 0))

        assertEquals(AddNoteError.ReminderInPast, viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isSaved)
        assertTrue(saved.isEmpty())
    }

    @Test
    fun `back goes one step at a time`() {
        viewModel.onTextChange("Note")
        viewModel.onTextDone()
        viewModel.onDayChosen(viewModel.today.plusDays(1))

        viewModel.onBack()
        assertEquals(AddNoteStep.ChooseDay, viewModel.uiState.value.step)
        viewModel.onBack()
        assertEquals(AddNoteStep.Write, viewModel.uiState.value.step)
        assertEquals("Note", viewModel.uiState.value.text)
    }
}
