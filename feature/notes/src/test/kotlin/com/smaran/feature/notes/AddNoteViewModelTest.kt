package com.smaran.feature.notes

import com.smaran.core.domain.reminder.ReminderScheduler
import com.smaran.core.domain.repository.NoteWriter
import com.smaran.core.domain.speech.AppLanguage
import com.smaran.core.domain.speech.EnglishReminderPhraseParser
import com.smaran.core.domain.speech.SpeechError
import com.smaran.core.domain.speech.SpeechEvent
import com.smaran.core.domain.speech.SpeechToText
import com.smaran.core.domain.usecase.AddNoteUseCase
import com.smaran.core.domain.usecase.ParseSpokenReminderUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
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
    private var nextSpeech: List<SpeechEvent> = emptyList()
    private val speech = object : SpeechToText {
        override fun isAvailable() = true
        override fun listen(languageTag: String): Flow<SpeechEvent> = flowOf(*nextSpeech.toTypedArray())
    }
    private val language = object : AppLanguage {
        override val speechLanguageTag = "en-IN"
    }
    // 10:00 UTC; the test uses UTC as the phone's zone.
    private val clock = Clock.fixed(Instant.parse("2026-11-26T10:00:00Z"), ZoneOffset.UTC)
    private lateinit var viewModel: AddNoteViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = AddNoteViewModel(
            AddNoteUseCase(writer, scheduler, clock),
            ParseSpokenReminderUseCase(EnglishReminderPhraseParser(), clock),
            speech,
            language,
            clock,
        )
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

    @Test
    fun `dictation is added after typed text`() {
        viewModel.onTextChange("Buy")
        nextSpeech = listOf(SpeechEvent.Listening, SpeechEvent.Partial("mi"), SpeechEvent.Result("milk and bread"))

        viewModel.startListeningForNote()

        assertEquals("Buy milk and bread", viewModel.uiState.value.text)
        assertEquals(null, viewModel.uiState.value.listeningFor)
    }

    @Test
    fun `saying when moves to the time step with a suggestion but saves nothing`() {
        viewModel.onTextChange("Call Ravi")
        viewModel.onTextDone()
        nextSpeech = listOf(SpeechEvent.Result("tomorrow at 5 pm"))

        viewModel.startListeningForWhen()

        val state = viewModel.uiState.value
        assertEquals(AddNoteStep.ChooseTime, state.step)
        assertEquals(viewModel.today.plusDays(1), state.reminderDate)
        assertEquals(LocalTime.of(17, 0), state.suggestedTime)
        assertTrue(saved.isEmpty())
    }

    @Test
    fun `unclear when shows a message and stays on the day step`() {
        viewModel.onTextChange("Call Ravi")
        viewModel.onTextDone()
        nextSpeech = listOf(SpeechEvent.Result("whenever"))

        viewModel.startListeningForWhen()

        assertEquals(AddNoteStep.ChooseDay, viewModel.uiState.value.step)
        assertEquals(AddNoteError.WhenNotUnderstood, viewModel.uiState.value.error)
    }

    @Test
    fun `speech failure is shown`() {
        nextSpeech = listOf(SpeechEvent.Failed(SpeechError.NothingHeard))

        viewModel.startListeningForNote()

        assertEquals(AddNoteError.Speech(SpeechError.NothingHeard), viewModel.uiState.value.error)
    }
}
