package com.smaran.core.domain.usecase

import com.smaran.core.domain.repository.NoteReader
import com.smaran.core.model.Note
import kotlinx.coroutines.flow.Flow
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

/** Today's notes, open ones first. Ordering is done by the data layer. */
class ObserveTodayNotesUseCase @Inject constructor(
    private val reader: NoteReader,
    private val clock: Clock,
) {
    operator fun invoke(): Flow<List<Note>> = reader.observeNotesOn(LocalDate.now(clock))
}
