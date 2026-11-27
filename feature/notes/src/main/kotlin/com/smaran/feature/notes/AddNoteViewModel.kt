package com.smaran.feature.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smaran.core.domain.speech.AppLanguage
import com.smaran.core.domain.speech.SpeechError
import com.smaran.core.domain.speech.SpeechEvent
import com.smaran.core.domain.speech.SpeechToText
import com.smaran.core.domain.usecase.AddNoteUseCase
import com.smaran.core.domain.usecase.ParseSpokenReminderUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

/** The three short steps of adding a note: write it, pick a day, pick a time. */
enum class AddNoteStep { Write, ChooseDay, ChooseTime }

/** What the microphone is currently filling in. */
enum class ListeningFor { Note, When }

data class AddNoteUiState(
    val step: AddNoteStep = AddNoteStep.Write,
    val text: String = "",
    val reminderDate: LocalDate? = null,
    /** A time heard in speech, offered as the first choice on the time step. */
    val suggestedTime: LocalTime? = null,
    val error: AddNoteError? = null,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val speechAvailable: Boolean = false,
    val listeningFor: ListeningFor? = null,
)

sealed interface AddNoteError {
    data object Empty : AddNoteError
    data class TooLong(val maxLength: Int) : AddNoteError
    data object ReminderInPast : AddNoteError
    data class Speech(val reason: SpeechError) : AddNoteError
    data object WhenNotUnderstood : AddNoteError
}

@HiltViewModel
class AddNoteViewModel @Inject constructor(
    private val addNote: AddNoteUseCase,
    private val parseSpokenReminder: ParseSpokenReminderUseCase,
    private val speechToText: SpeechToText,
    private val language: AppLanguage,
    private val clock: Clock,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddNoteUiState(speechAvailable = speechToText.isAvailable()))
    val uiState: StateFlow<AddNoteUiState> = _uiState.asStateFlow()

    private var listeningJob: Job? = null

    val today: LocalDate get() = LocalDate.now(clock)

    fun onTextChange(text: String) {
        _uiState.update { it.copy(text = text, error = null) }
    }

    /** Step 1 → 2. Checks the text before moving on so mistakes show where they were made. */
    fun onTextDone() {
        stopListening()
        val cleaned = _uiState.value.text.trim()
        val error = when {
            cleaned.isEmpty() -> AddNoteError.Empty
            cleaned.length > AddNoteUseCase.MAX_LENGTH -> AddNoteError.TooLong(AddNoteUseCase.MAX_LENGTH)
            else -> null
        }
        _uiState.update { if (error != null) it.copy(error = error) else it.copy(step = AddNoteStep.ChooseDay) }
    }

    fun onNoReminder() = save(date = null, time = null)

    fun onDayChosen(date: LocalDate) {
        stopListening()
        _uiState.update {
            it.copy(reminderDate = date, suggestedTime = null, step = AddNoteStep.ChooseTime, error = null)
        }
    }

    fun onTimeChosen(time: LocalTime) = save(_uiState.value.reminderDate, time)

    fun onBack() {
        stopListening()
        _uiState.update {
            when (it.step) {
                AddNoteStep.Write -> it
                AddNoteStep.ChooseDay -> it.copy(step = AddNoteStep.Write, error = null)
                AddNoteStep.ChooseTime -> it.copy(step = AddNoteStep.ChooseDay, suggestedTime = null, error = null)
            }
        }
    }

    // --- Voice ---

    fun onMicrophoneDenied() {
        _uiState.update { it.copy(error = AddNoteError.Speech(SpeechError.NoPermission)) }
    }

    /** Dictates the note. Spoken words are added after anything already typed. */
    fun startListeningForNote() {
        val before = _uiState.value.text.trimEnd()
        listen(ListeningFor.Note) { event ->
            when (event) {
                is SpeechEvent.Partial -> _uiState.update { it.copy(text = join(before, event.text)) }
                is SpeechEvent.Result -> _uiState.update { it.copy(text = join(before, event.text)) }
                else -> Unit
            }
        }
    }

    /** Hears "tomorrow at 5" and moves to the time step with that day, suggesting that time. */
    fun startListeningForWhen() {
        listen(ListeningFor.When) { event ->
            if (event !is SpeechEvent.Result) return@listen
            val heard = parseSpokenReminder(event.text)
            val date = heard?.date
            if (date == null) {
                _uiState.update { it.copy(error = AddNoteError.WhenNotUnderstood) }
            } else {
                _uiState.update {
                    it.copy(
                        reminderDate = date,
                        suggestedTime = heard.time,
                        step = AddNoteStep.ChooseTime,
                        error = null,
                    )
                }
            }
        }
    }

    fun stopListening() {
        listeningJob?.cancel()
        listeningJob = null
        _uiState.update { it.copy(listeningFor = null) }
    }

    private fun listen(target: ListeningFor, onEvent: (SpeechEvent) -> Unit) {
        stopListening()
        _uiState.update { it.copy(listeningFor = target, error = null) }
        listeningJob = viewModelScope.launch {
            speechToText.listen(language.speechLanguageTag).collect { event ->
                if (event is SpeechEvent.Failed) {
                    _uiState.update { it.copy(error = AddNoteError.Speech(event.error)) }
                }
                onEvent(event)
            }
            _uiState.update { it.copy(listeningFor = null) }
        }
    }

    private fun join(before: String, spoken: String): String =
        if (before.isEmpty()) spoken.trim() else "$before ${spoken.trim()}"

    // --- Saving ---

    private fun save(date: LocalDate?, time: LocalTime?) {
        if (_uiState.value.isSaving) return
        stopListening()
        _uiState.update { it.copy(isSaving = true, error = null) }
        viewModelScope.launch {
            val result = addNote(_uiState.value.text, date, time)
            _uiState.update { state ->
                when (result) {
                    is AddNoteUseCase.Result.Added -> state.copy(isSaving = false, isSaved = true)
                    AddNoteUseCase.Result.EmptyText ->
                        state.copy(isSaving = false, step = AddNoteStep.Write, error = AddNoteError.Empty)
                    is AddNoteUseCase.Result.TooLong ->
                        state.copy(isSaving = false, step = AddNoteStep.Write, error = AddNoteError.TooLong(result.maxLength))
                    AddNoteUseCase.Result.ReminderInPast ->
                        state.copy(isSaving = false, error = AddNoteError.ReminderInPast)
                }
            }
        }
    }

    override fun onCleared() {
        listeningJob?.cancel()
    }

    companion object {
        val MORNING: LocalTime = LocalTime.of(9, 0)
        val AFTERNOON: LocalTime = LocalTime.of(14, 0)
        val EVENING: LocalTime = LocalTime.of(18, 0)
    }
}
