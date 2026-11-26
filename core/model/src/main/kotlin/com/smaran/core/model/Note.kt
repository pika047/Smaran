package com.smaran.core.model

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

/**
 * A note filed under a [date]. If [remindAt] is set, the person is reminded
 * at that time on that date.
 */
data class Note(
    val id: Long,
    val text: String,
    val date: LocalDate,
    val remindAt: LocalTime?,
    val isDone: Boolean,
    val createdAt: Instant,
)

/** A note that has not been saved yet, so it has no id. */
data class NewNote(
    val text: String,
    val date: LocalDate,
    val remindAt: LocalTime?,
    val createdAt: Instant,
)
