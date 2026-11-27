package com.smaran.core.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface NoteDao {
    /** Open notes first; within each group, timed reminders by time, then untimed notes by creation. */
    @Query(
        """
        SELECT * FROM notes WHERE date = :date
        ORDER BY isDone ASC, remindAt IS NULL ASC, remindAt ASC, createdAt ASC
        """,
    )
    fun observeByDate(date: LocalDate): Flow<List<NoteEntity>>

    @Insert
    suspend fun insert(note: NoteEntity): Long

    @Query("UPDATE notes SET isDone = :done WHERE id = :id")
    suspend fun setDone(id: Long, done: Boolean)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun delete(id: Long)
}
