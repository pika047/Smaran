package com.smaran.feature.notes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smaran.core.ui.component.BigButton
import com.smaran.core.ui.theme.Dimens

@Composable
internal fun AddNoteRoute(
    onFinished: () -> Unit,
    viewModel: AddNoteViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.isSaved) {
        if (state.isSaved) onFinished()
    }
    AddNoteScreen(
        state = state,
        onTextChange = viewModel::onTextChange,
        onSave = viewModel::save,
        onCancel = onFinished,
    )
}

@Composable
internal fun AddNoteScreen(
    state: AddNoteUiState,
    onTextChange: (String) -> Unit,
    onSave: () -> Unit,
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
                text = stringResource(R.string.add_note_save),
                onClick = onSave,
                icon = Icons.Filled.Check,
                enabled = !state.isSaving,
            )
            BigButton(
                text = stringResource(R.string.add_note_cancel),
                onClick = onCancel,
                icon = Icons.Filled.Close,
                primary = false,
            )
        }
    }
}

@Composable
private fun AddNoteError.message(): String = when (this) {
    AddNoteError.Empty -> stringResource(R.string.add_note_error_empty)
    is AddNoteError.TooLong -> stringResource(R.string.add_note_error_too_long, maxLength)
}
