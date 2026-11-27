package com.smaran.feature.today

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.smaran.core.ui.component.BigButton
import com.smaran.core.ui.theme.Dimens

@Composable
internal fun ReminderWarningBanner(warning: ReminderWarning, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val (message, action) = when (warning) {
        ReminderWarning.NotificationsOff ->
            R.string.today_notifications_off to R.string.today_turn_on_notifications
        ReminderWarning.ExactAlarmsOff ->
            R.string.today_exact_alarms_off to R.string.today_allow_on_time
    }
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
    ) {
        Column(
            modifier = Modifier.padding(Dimens.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.ItemSpacing),
        ) {
            Text(stringResource(message), style = MaterialTheme.typography.bodyLarge)
            BigButton(
                text = stringResource(action),
                onClick = { openSettingsFor(context, warning) },
                icon = Icons.Filled.Notifications,
            )
        }
    }
}

private fun openSettingsFor(context: Context, warning: ReminderWarning) {
    val intent = when (warning) {
        ReminderWarning.NotificationsOff ->
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        ReminderWarning.ExactAlarmsOff ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:${context.packageName}"))
            } else {
                return
            }
    }
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        // Some phones hide these screens; fall back to the app's main settings page.
        context.startActivity(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}")),
        )
    }
}
