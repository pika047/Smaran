package com.smaran.feature.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smaran.core.domain.usecase.AddNoteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
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

data class AddNoteUiState(
    val step: AddNoteStep = AddNoteStep.Write,
    val text: String = "",
    val reminderDate: LocalDate? = null,
    val error: AddNoteError? = null,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
)

sealed interface AddNoteError {
    data object Empty : AddNoteError
    data class TooLong(val maxLength: Int) : AddNoteError
    data object ReminderInPast : AddNoteError
}

@HiltViewModel
class AddNoteViewModel @Inject constructor(
    private val addNote: AddNoteUseCase,
    private val clock: Clock,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddNoteUiState())
    val uiState: StateFlow<AddNoteUiState> = _uiState.asStateFlow()

    val today: LocalDate get() = LocalDate.now(clock)

    fun onTextChange(text: String) {
        _uiState.update { it.copy(text = text, error = null) }
    }

    /** Step 1 → 2. Checks the text before moving on so mistakes show where they were made. */
    fun onTextDone() {
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
        _uiState.update { it.copy(reminderDate = date, step = AddNoteStep.ChooseTime, error = null) }
    }

    fun onTimeChosen(time: LocalTime) = save(_uiState.value.reminderDate, time)

    fun onBack() {
        _uiState.update {
            when (it.step) {
                AddNoteStep.Write -> it
                AddNoteStep.ChooseDay -> it.copy(step = AddNoteStep.Write, error = null)
                AddNoteStep.ChooseTime -> it.copy(step = AddNoteStep.ChooseDay, error = null)
            }
        }
    }

    private fun save(date: LocalDate?, time: LocalTime?) {
        if (_uiState.value.isSaving) return
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

    companion object {
        val MORNING: LocalTime = LocalTime.of(9, 0)
        val AFTERNOON: LocalTime = LocalTime.of(14, 0)
        val EVENING: LocalTime = LocalTime.of(18, 0)
    }
}
