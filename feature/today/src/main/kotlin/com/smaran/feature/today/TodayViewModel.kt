package com.smaran.feature.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smaran.core.domain.reminder.ReminderPermissions
import com.smaran.core.domain.usecase.ObserveTodayNotesUseCase
import com.smaran.core.domain.usecase.SetNoteDoneUseCase
import com.smaran.core.model.Note
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

/** A problem that stops reminders working as expected, shown as a banner with a fix button. */
enum class ReminderWarning { NotificationsOff, ExactAlarmsOff }

data class TodayUiState(
    val date: LocalDate,
    val notes: List<Note> = emptyList(),
    val isLoading: Boolean = true,
    val warning: ReminderWarning? = null,
)

@HiltViewModel
class TodayViewModel @Inject constructor(
    observeTodayNotes: ObserveTodayNotesUseCase,
    private val setNoteDone: SetNoteDoneUseCase,
    private val permissions: ReminderPermissions,
    private val clock: Clock,
) : ViewModel() {

    private val currentWarning = MutableStateFlow(readWarning())

    val uiState: StateFlow<TodayUiState> =
        combine(observeTodayNotes(), currentWarning) { notes, warning ->
            TodayUiState(LocalDate.now(clock), notes, isLoading = false, warning = warning)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = TodayUiState(LocalDate.now(clock)),
        )

    /** Called when the screen comes back into view, e.g. after the person visits system settings. */
    fun refreshPermissions() {
        currentWarning.value = readWarning()
    }

    fun setDone(id: Long, done: Boolean) {
        viewModelScope.launch { setNoteDone(id, done) }
    }

    private fun readWarning(): ReminderWarning? = when {
        !permissions.canPostNotifications() -> ReminderWarning.NotificationsOff
        !permissions.canScheduleExactAlarms() -> ReminderWarning.ExactAlarmsOff
        else -> null
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
