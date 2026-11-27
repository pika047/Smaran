package com.smaran.core.reminders.di

import com.smaran.core.domain.reminder.ReminderPermissions
import com.smaran.core.domain.reminder.ReminderScheduler
import com.smaran.core.reminders.AlarmManagerReminderScheduler
import com.smaran.core.reminders.AndroidReminderPermissions
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class RemindersModule {
    @Binds abstract fun bindScheduler(impl: AlarmManagerReminderScheduler): ReminderScheduler
    @Binds abstract fun bindPermissions(impl: AndroidReminderPermissions): ReminderPermissions
}
