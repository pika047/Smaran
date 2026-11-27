package com.smaran.core.reminders

import android.app.AlarmManager
import android.content.Context
import androidx.core.content.getSystemService
import com.smaran.core.domain.reminder.ReminderPermissions
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal class AndroidReminderPermissions @Inject constructor(
    @ApplicationContext private val context: Context,
) : ReminderPermissions {
    override fun canPostNotifications(): Boolean = canNotify(context)

    override fun canScheduleExactAlarms(): Boolean =
        canScheduleExact(requireNotNull(context.getSystemService<AlarmManager>()))
}
