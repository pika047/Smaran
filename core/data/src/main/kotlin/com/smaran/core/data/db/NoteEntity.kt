package com.smaran.core.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.smaran.core.model.NewNote
import com.smaran.core.model.Note
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

@Entity(tableName = "notes", indices = [Index("date")])
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val date: LocalDate,
    val remindAt: LocalTime?,
    val isDone: Boolean,
    val createdAt: Instant,
)

fun NoteEntity.toModel() = Note(id, text, date, remindAt, isDone, createdAt)

fun NewNote.toEntity() = NoteEntity(
    text = text,
    date = date,
    remindAt = remindAt,
    isDone = false,
    createdAt = createdAt,
)
