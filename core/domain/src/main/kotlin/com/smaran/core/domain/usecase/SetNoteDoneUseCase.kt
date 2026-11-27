package com.smaran.core.domain.usecase

import com.smaran.core.domain.repository.NoteWriter
import javax.inject.Inject

/** Marks a note done, or undoes that. */
class SetNoteDoneUseCase @Inject constructor(
    private val writer: NoteWriter,
) {
    suspend operator fun invoke(id: Long, done: Boolean) = writer.setDone(id, done)
}
