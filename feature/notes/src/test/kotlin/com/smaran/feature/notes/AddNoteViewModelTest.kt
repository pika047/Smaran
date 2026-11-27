package com.smaran.feature.notes

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
import java.time.ZoneOffset

@OptIn(ExperimentalCoroutinesApi::class)
class AddNoteViewModelTest {
    private val saved = mutableListOf<NewNote>()
    private val writer = object : NoteWriter {
        override suspend fun add(note: NewNote): Long = saved.add(note).let { saved.size.toLong() }
        override suspend fun setDone(id: Long, done: Boolean) = Unit
        override suspend fun delete(id: Long) = Unit
    }
    private val clock = Clock.fixed(Instant.parse("2026-11-26T10:00:00Z"), ZoneOffset.UTC)
    private lateinit var viewModel: AddNoteViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = AddNoteViewModel(AddNoteUseCase(writer, clock))
    }

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `saving valid text marks the screen as saved`() {
        viewModel.onTextChange("Buy milk")
        viewModel.save()

        assertTrue(viewModel.uiState.value.isSaved)
        assertEquals("Buy milk", saved.single().text)
    }

    @Test
    fun `saving blank text shows an error and saves nothing`() {
        viewModel.onTextChange("  ")
        viewModel.save()

        assertEquals(AddNoteError.Empty, viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isSaved)
        assertTrue(saved.isEmpty())
    }

    @Test
    fun `typing again clears the error`() {
        viewModel.save()
        viewModel.onTextChange("B")

        assertEquals(null, viewModel.uiState.value.error)
    }
}
