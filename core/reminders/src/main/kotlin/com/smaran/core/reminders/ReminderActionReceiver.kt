package com.smaran.core.reminders

import android.content.Context
import android.content.Intent
import com.smaran.core.domain.usecase.SetNoteDoneUseCase
import com.smaran.core.domain.usecase.SnoozeReminderUseCase
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/** Handles the Done and "Remind me in 1 hour" buttons on a reminder notification. */
@AndroidEntryPoint
internal class ReminderActionReceiver : HiltBroadcastReceiver() {
    @Inject lateinit var setNoteDone: SetNoteDoneUseCase
    @Inject lateinit var snooze: SnoozeReminderUseCase
    @Inject lateinit var notifier: ReminderNotifier

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val noteId = ReminderIntents.noteId(intent) ?: return
        when (intent.action) {
            ReminderIntents.ACTION_DONE -> runAsync { setNoteDone(noteId, true) }
            ReminderIntents.ACTION_SNOOZE -> {
                notifier.dismiss(noteId)
                runAsync { snooze(noteId) }
            }
        }
    }
}
