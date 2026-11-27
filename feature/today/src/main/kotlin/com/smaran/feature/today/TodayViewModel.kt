package com.smaran.feature.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smaran.core.domain.usecase.ObserveTodayNotesUseCase
import com.smaran.core.domain.usecase.SetNoteDoneUseCase
import com.smaran.core.model.Note
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

data class TodayUiState(
    val date: LocalDate,
    val notes: List<Note> = emptyList(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class TodayViewModel @Inject constructor(
    observeTodayNotes: ObserveTodayNotesUseCase,
    private val setNoteDone: SetNoteDoneUseCase,
    private val clock: Clock,
) : ViewModel() {

    val uiState: StateFlow<TodayUiState> = observeTodayNotes()
        .map { notes -> TodayUiState(LocalDate.now(clock), notes, isLoading = false) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = TodayUiState(LocalDate.now(clock)),
        )

    fun setDone(id: Long, done: Boolean) {
        viewModelScope.launch { setNoteDone(id, done) }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
