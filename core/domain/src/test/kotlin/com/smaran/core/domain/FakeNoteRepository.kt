package com.smaran.core.domain

import com.smaran.core.domain.repository.NoteReader
import com.smaran.core.domain.repository.NoteWriter
import com.smaran.core.model.NewNote
import com.smaran.core.model.Note
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.time.LocalDate

class FakeNoteRepository : NoteReader, NoteWriter {
    private val notes = MutableStateFlow<List<Note>>(emptyList())
    private var nextId = 1L

    val all: List<Note> get() = notes.value

    override fun observeNotesOn(date: LocalDate): Flow<List<Note>> =
        notes.map { list -> list.filter { it.date == date } }

    override suspend fun getNote(id: Long): Note? = notes.value.firstOrNull { it.id == id }

    override suspend fun getOpenRemindersFrom(from: LocalDate): List<Note> =
        notes.value.filter { !it.isDone && it.remindAt != null && !it.date.isBefore(from) }

    override suspend fun add(note: NewNote): Long {
        val id = nextId++
        notes.update {
            it + Note(id, note.text, note.date, note.remindAt, isDone = false, createdAt = note.createdAt)
        }
        return id
    }

    override suspend fun setDone(id: Long, done: Boolean) {
        notes.update { list -> list.map { if (it.id == id) it.copy(isDone = done) else it } }
    }

    override suspend fun delete(id: Long) {
        notes.update { list -> list.filterNot { it.id == id } }
    }
}
