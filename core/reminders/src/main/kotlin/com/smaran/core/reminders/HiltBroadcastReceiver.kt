package com.smaran.core.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.annotation.CallSuper

/**
 * Hilt injects receivers inside onReceive, so subclasses must call super.onReceive.
 * BroadcastReceiver.onReceive is abstract; this gives it a body that can be called.
 */
internal abstract class HiltBroadcastReceiver : BroadcastReceiver() {
    @CallSuper
    override fun onReceive(context: Context, intent: Intent) = Unit
}
