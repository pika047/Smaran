package com.smaran.core.domain.repository

import com.smaran.core.model.NewNote
import com.smaran.core.model.Note
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/** Read side of note storage. Screens that only display notes depend on this alone. */
interface NoteReader {
    fun observeNotesOn(date: LocalDate): Flow<List<Note>>
}

/** Write side of note storage. */
interface NoteWriter {
    /** Saves [note] and returns its new id. */
    suspend fun add(note: NewNote): Long

    suspend fun setDone(id: Long, done: Boolean)

    suspend fun delete(id: Long)
}
