package com.smaran.core.data.repository

import com.smaran.core.data.db.NoteDao
import com.smaran.core.data.db.toEntity
import com.smaran.core.data.db.toModel
import com.smaran.core.domain.repository.NoteReader
import com.smaran.core.domain.repository.NoteWriter
import com.smaran.core.model.NewNote
import com.smaran.core.model.Note
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/** Stores notes only on this phone, in the encrypted database. */
@Singleton
internal class OfflineNoteRepository @Inject constructor(
    private val dao: NoteDao,
) : NoteReader, NoteWriter {

    override fun observeNotesOn(date: LocalDate): Flow<List<Note>> =
        dao.observeByDate(date).map { list -> list.map { it.toModel() } }

    override suspend fun add(note: NewNote): Long = dao.insert(note.toEntity())

    override suspend fun setDone(id: Long, done: Boolean) = dao.setDone(id, done)

    override suspend fun delete(id: Long) = dao.delete(id)
}
