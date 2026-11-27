package com.smaran.core.reminders

import android.content.Context
import android.content.Intent
import com.smaran.core.domain.usecase.RescheduleRemindersUseCase
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/** Sets every future reminder again after a restart, app update, or clock / time zone change. */
@AndroidEntryPoint
internal class RescheduleRemindersReceiver : HiltBroadcastReceiver() {
    @Inject lateinit var reschedule: RescheduleRemindersUseCase

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action !in HANDLED_ACTIONS) return
        runAsync { reschedule() }
    }

    private companion object {
        val HANDLED_ACTIONS = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            "android.app.action.SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED",
        )
    }
}
