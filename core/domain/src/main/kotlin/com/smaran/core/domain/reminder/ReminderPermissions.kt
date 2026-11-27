package com.smaran.core.domain.reminder

/** What the phone currently allows. Both can be changed by the person in system settings. */
interface ReminderPermissions {
    fun canPostNotifications(): Boolean

    /** False means reminders may arrive up to a few minutes late. */
    fun canScheduleExactAlarms(): Boolean
}
