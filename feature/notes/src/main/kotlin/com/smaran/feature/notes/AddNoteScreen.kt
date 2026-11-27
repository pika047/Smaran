package com.smaran.feature.notes

import android.Manifest
import android.os.Build
import android.text.format.DateFormat
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smaran.core.ui.component.BigButton
import com.smaran.core.ui.theme.Dimens
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
internal fun AddNoteRoute(
    onFinished: () -> Unit,
    viewModel: AddNoteViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.isSaved) {
        if (state.isSaved) onFinished()
    }
    BackHandler(enabled = state.step != AddNoteStep.Write) { viewModel.onBack() }

    // Ask for notification permission the moment a reminder day is picked (Android 13+).
    // Whatever the answer, the note is still saved; Today shows a banner if reminders can't show.
    val notificationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { /* No action needed: the result is read from system state when Today is shown. */ }
    val onDayChosen: (LocalDate) -> Unit = { date ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        viewModel.onDayChosen(date)
    }

    AddNoteScreen(
        state = state,
        today = viewModel.today,
        onTextChange = viewModel::onTextChange,
        onTextDone = viewModel::onTextDone,
        onNoReminder = viewModel::onNoReminder,
        onDayChosen = onDayChosen,
        onTimeChosen = viewModel::onTimeChosen,
        onBack = viewModel::onBack,
        onCancel = onFinished,
    )
}

@Composable
internal fun AddNoteScreen(
    state: AddNoteUiState,
    today: LocalDate,
    onTextChange: (String) -> Unit,
    onTextDone: () -> Unit,
    onNoReminder: () -> Unit,
    onDayChosen: (LocalDate) -> Unit,
    onTimeChosen: (LocalTime) -> Unit,
    onBack: () -> Unit,
    onCancel: () -> Unit,
) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(Dimens.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.ItemSpacing),
        ) {
            when (state.step) {
                AddNoteStep.Write -> WriteStep(state, onTextChange, onTextDone, onCancel)
                AddNoteStep.ChooseDay -> ChooseDayStep(state, today, onNoReminder, onDayChosen, onBack)
                AddNoteStep.ChooseTime -> ChooseTimeStep(state, onTimeChosen, onBack)
            }
        }
    }
}

@Composable
private fun ColumnScope.WriteStep(
    state: AddNoteUiState,
    onTextChange: (String) -> Unit,
    onTextDone: () -> Unit,
    onCancel: () -> Unit,
) {
    Text(stringResource(R.string.add_note_title), style = MaterialTheme.typography.headlineLarge)
    OutlinedTextField(
        value = state.text,
        onValueChange = onTextChange,
        label = { Text(stringResource(R.string.add_note_field_label)) },
        textStyle = MaterialTheme.typography.bodyLarge,
        minLines = 4,
        isError = state.error != null,
        supportingText = state.error?.let { error -> { Text(error.message()) } },
        modifier = Modifier.fillMaxWidth(),
    )
    BigButton(
        text = stringResource(R.string.add_note_next),
        onClick = onTextDone,
        icon = Icons.AutoMirrored.Filled.ArrowForward,
    )
    BigButton(
        text = stringResource(R.string.add_note_cancel),
        onClick = onCancel,
        icon = Icons.Filled.Close,
        primary = false,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ColumnScope.ChooseDayStep(
    state: AddNoteUiState,
    today: LocalDate,
    onNoReminder: () -> Unit,
    onDayChosen: (LocalDate) -> Unit,
    onBack: () -> Unit,
) {
    var showDatePicker by rememberSaveable { mutableStateOf(false) }

    Text(stringResource(R.string.add_note_when_title), style = MaterialTheme.typography.headlineMedium)
    Text(state.text.trim(), style = MaterialTheme.typography.bodyLarge)

    BigButton(stringResource(R.string.add_note_today), { onDayChosen(today) }, icon = Icons.Filled.Notifications)
    BigButton(stringResource(R.string.add_note_tomorrow), { onDayChosen(today.plusDays(1)) }, icon = Icons.Filled.Notifications)
    BigButton(stringResource(R.string.add_note_pick_date), { showDatePicker = true }, icon = Icons.Filled.DateRange)
    BigButton(
        text = stringResource(R.string.add_note_no_reminder),
        onClick = onNoReminder,
        primary = false,
        enabled = !state.isSaving,
    )
    BigButton(stringResource(R.string.add_note_back), onBack, icon = Icons.AutoMirrored.Filled.ArrowBack, primary = false)

    if (showDatePicker) {
        val todayUtcMillis = today.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = todayUtcMillis,
            selectableDates = remember(todayUtcMillis) {
                object : SelectableDates {
                    override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis >= todayUtcMillis
                    override fun isSelectableYear(year: Int) = year >= today.year
                }
            },
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showDatePicker = false
                    pickerState.selectedDateMillis?.let { millis ->
                        // The picker reports midnight UTC for the chosen day.
                        onDayChosen(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate())
                    }
                }) { Text(stringResource(R.string.dialog_ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.dialog_cancel)) }
            },
        ) {
            DatePicker(state = pickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ColumnScope.ChooseTimeStep(
    state: AddNoteUiState,
    onTimeChosen: (LocalTime) -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    var showTimePicker by rememberSaveable { mutableStateOf(false) }
    val date = state.reminderDate ?: return
    val dateText = date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL))
    val timeFormat = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)

    Text(stringResource(R.string.add_note_time_title, dateText), style = MaterialTheme.typography.headlineMedium)
    state.error?.let { Text(it.message(), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyLarge) }

    BigButton(
        stringResource(R.string.add_note_morning, AddNoteViewModel.MORNING.format(timeFormat)),
        { onTimeChosen(AddNoteViewModel.MORNING) },
        enabled = !state.isSaving,
    )
    BigButton(
        stringResource(R.string.add_note_afternoon, AddNoteViewModel.AFTERNOON.format(timeFormat)),
        { onTimeChosen(AddNoteViewModel.AFTERNOON) },
        enabled = !state.isSaving,
    )
    BigButton(
        stringResource(R.string.add_note_evening, AddNoteViewModel.EVENING.format(timeFormat)),
        { onTimeChosen(AddNoteViewModel.EVENING) },
        enabled = !state.isSaving,
    )
    BigButton(stringResource(R.string.add_note_pick_time), { showTimePicker = true }, primary = false, enabled = !state.isSaving)
    BigButton(stringResource(R.string.add_note_back), onBack, icon = Icons.AutoMirrored.Filled.ArrowBack, primary = false)

    if (showTimePicker) {
        val pickerState = rememberTimePickerState(
            initialHour = AddNoteViewModel.MORNING.hour,
            initialMinute = 0,
            is24Hour = DateFormat.is24HourFormat(context),
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showTimePicker = false
                    onTimeChosen(LocalTime.of(pickerState.hour, pickerState.minute))
                }) { Text(stringResource(R.string.dialog_ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text(stringResource(R.string.dialog_cancel)) }
            },
            text = { TimePicker(state = pickerState) },
        )
    }
}

@Composable
private fun AddNoteError.message(): String = when (this) {
    AddNoteError.Empty -> stringResource(R.string.add_note_error_empty)
    is AddNoteError.TooLong -> stringResource(R.string.add_note_error_too_long, maxLength)
    AddNoteError.ReminderInPast -> stringResource(R.string.add_note_error_past)
}
