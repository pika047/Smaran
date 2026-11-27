package com.smaran.feature.today

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smaran.core.model.Note
import com.smaran.core.ui.component.BigButton
import com.smaran.core.ui.theme.Dimens
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
internal fun TodayRoute(
    onAddNote: () -> Unit,
    viewModel: TodayViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LifecycleResumeEffect(Unit) {
        viewModel.refreshPermissions()
        onPauseOrDispose { }
    }
    TodayScreen(state = state, onAddNote = onAddNote, onDoneChange = viewModel::setDone)
}

@Composable
internal fun TodayScreen(
    state: TodayUiState,
    onAddNote: () -> Unit,
    onDoneChange: (id: Long, done: Boolean) -> Unit,
) {
    // Locale-aware, so the date reads naturally once more languages are added.
    val dateText = state.date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL))

    Scaffold(
        bottomBar = {
            BigButton(
                text = stringResource(R.string.today_add_note),
                onClick = onAddNote,
                icon = Icons.Filled.Add,
                modifier = Modifier.padding(Dimens.ScreenPadding),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = Dimens.ScreenPadding),
        ) {
            Spacer(Modifier.height(Dimens.ItemSpacing))
            Text(stringResource(R.string.today_title), style = MaterialTheme.typography.headlineLarge)
            Text(dateText, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(Dimens.ItemSpacing))

            state.warning?.let { warning ->
                ReminderWarningBanner(warning)
                Spacer(Modifier.height(Dimens.ItemSpacing))
            }

            when {
                state.isLoading -> Unit
                state.notes.isEmpty() -> Text(
                    stringResource(R.string.today_empty),
                    style = MaterialTheme.typography.bodyLarge,
                )
                else -> LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(Dimens.ItemSpacing),
                    contentPadding = PaddingValues(bottom = Dimens.ItemSpacing),
                ) {
                    items(state.notes, key = { it.id }) { note ->
                        NoteCard(note = note, onDoneChange = onDoneChange)
                    }
                }
            }
        }
    }
}

@Composable
private fun NoteCard(note: Note, onDoneChange: (Long, Boolean) -> Unit) {
    val doneState = stringResource(R.string.today_note_done_state)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { if (note.isDone) stateDescription = doneState },
    ) {
        Column(Modifier.padding(Dimens.ScreenPadding)) {
            Text(
                text = note.text,
                style = MaterialTheme.typography.bodyLarge,
                textDecoration = if (note.isDone) TextDecoration.LineThrough else null,
                color = if (note.isDone) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            )
            note.remindAt?.let { time ->
                val timeText = time.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
                Text(
                    stringResource(R.string.today_remind_at, timeText),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Spacer(Modifier.height(12.dp))
            BigButton(
                text = stringResource(if (note.isDone) R.string.today_undo_done else R.string.today_mark_done),
                onClick = { onDoneChange(note.id, !note.isDone) },
                icon = if (note.isDone) Icons.Filled.Refresh else Icons.Filled.Check,
                primary = !note.isDone,
            )
        }
    }
}
