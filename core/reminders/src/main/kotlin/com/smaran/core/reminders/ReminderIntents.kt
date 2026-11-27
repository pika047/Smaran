package com.smaran.core.reminders

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * Builds the intents for alarms and notification buttons. The note id travels in the
 * intent's data URI, which also keeps each note's PendingIntent distinct.
 * No note text is ever put in an intent.
 */
internal object ReminderIntents {
    const val ACTION_FIRE = "com.smaran.reminders.FIRE"
    const val ACTION_DONE = "com.smaran.reminders.DONE"
    const val ACTION_SNOOZE = "com.smaran.reminders.SNOOZE"

    private const val SCHEME = "smaran-reminder"

    fun alarm(context: Context, noteId: Long): PendingIntent =
        broadcast(context, ReminderAlarmReceiver::class.java, ACTION_FIRE, noteId)

    fun done(context: Context, noteId: Long): PendingIntent =
        broadcast(context, ReminderActionReceiver::class.java, ACTION_DONE, noteId)

    fun snooze(context: Context, noteId: Long): PendingIntent =
        broadcast(context, ReminderActionReceiver::class.java, ACTION_SNOOZE, noteId)

    fun noteId(intent: Intent): Long? = intent.data?.schemeSpecificPart?.toLongOrNull()

    private fun broadcast(context: Context, receiver: Class<*>, action: String, noteId: Long): PendingIntent {
        val intent = Intent(context, receiver)
            .setAction(action)
            .setData(Uri.fromParts(SCHEME, noteId.toString(), null))
        return PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }
}
