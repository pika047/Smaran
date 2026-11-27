package com.smaran.core.reminders

import android.app.AlarmManager
import android.content.Context
import android.os.Build
import androidx.core.content.getSystemService
import com.smaran.core.domain.reminder.ReminderScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class AlarmManagerReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notifier: ReminderNotifier,
) : ReminderScheduler {

    private val alarmManager: AlarmManager = requireNotNull(context.getSystemService())

    override fun schedule(noteId: Long, at: Instant) {
        val pendingIntent = ReminderIntents.alarm(context, noteId)
        val triggerAt = at.toEpochMilli()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()) {
            try {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
                return
            } catch (e: SecurityException) {
                // Permission was withdrawn between the check and the call; fall through.
            }
        }
        // Without permission, ask Android to fire within a 10-minute window instead.
        alarmManager.setWindow(AlarmManager.RTC_WAKEUP, triggerAt, INEXACT_WINDOW_MS, pendingIntent)
    }

    override fun cancel(noteId: Long) {
        alarmManager.cancel(ReminderIntents.alarm(context, noteId))
        notifier.dismiss(noteId)
    }

    private companion object {
        const val INEXACT_WINDOW_MS = 10 * 60 * 1000L
    }
}

internal fun canScheduleExact(alarmManager: AlarmManager): Boolean =
    Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()
