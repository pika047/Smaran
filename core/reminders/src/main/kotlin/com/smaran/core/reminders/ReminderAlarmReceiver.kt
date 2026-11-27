package com.smaran.core.reminders

import android.content.Context
import android.content.Intent
import com.smaran.core.domain.repository.NoteReader
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/** Fires when a reminder is due. Reads the note fresh, so a deleted or finished note shows nothing. */
@AndroidEntryPoint
internal class ReminderAlarmReceiver : HiltBroadcastReceiver() {
    @Inject lateinit var reader: NoteReader
    @Inject lateinit var notifier: ReminderNotifier

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action != ReminderIntents.ACTION_FIRE) return
        val noteId = ReminderIntents.noteId(intent) ?: return
        runAsync {
            val note = reader.getNote(noteId)
            if (note != null && !note.isDone) notifier.show(note)
        }
    }
}
