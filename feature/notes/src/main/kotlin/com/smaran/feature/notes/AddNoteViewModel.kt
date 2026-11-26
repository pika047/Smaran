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
import javax.inject.Inject

data class AddNoteUiState(
    val text: String = "",
    val error: AddNoteError? = null,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
)

sealed interface AddNoteError {
    data object Empty : AddNoteError
    data class TooLong(val maxLength: Int) : AddNoteError
}

@HiltViewModel
class AddNoteViewModel @Inject constructor(
    private val addNote: AddNoteUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddNoteUiState())
    val uiState: StateFlow<AddNoteUiState> = _uiState.asStateFlow()

    fun onTextChange(text: String) {
        _uiState.update { it.copy(text = text, error = null) }
    }

    fun save() {
        if (_uiState.value.isSaving) return
        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            _uiState.update { state ->
                when (val result = addNote(state.text)) {
                    is AddNoteUseCase.Result.Added -> state.copy(isSaving = false, isSaved = true)
                    AddNoteUseCase.Result.EmptyText -> state.copy(isSaving = false, error = AddNoteError.Empty)
                    is AddNoteUseCase.Result.TooLong ->
                        state.copy(isSaving = false, error = AddNoteError.TooLong(result.maxLength))
                }
            }
        }
    }
}
