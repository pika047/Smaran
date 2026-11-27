package com.smaran.core.domain.reminder

import com.smaran.core.model.Note
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

/** The moment a reminder for [date] at [time] fires in [zone]. */
fun reminderInstant(date: LocalDate, time: LocalTime, zone: ZoneId): Instant =
    LocalDateTime.of(date, time).atZone(zone).toInstant()

fun Note.reminderInstant(zone: ZoneId): Instant? =
    remindAt?.let { reminderInstant(date, it, zone) }
