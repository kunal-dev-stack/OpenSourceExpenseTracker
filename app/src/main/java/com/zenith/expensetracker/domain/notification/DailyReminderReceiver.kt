package com.zenith.expensetracker.domain.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.zenith.expensetracker.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DailyReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        NotificationHelper.sendDailyExpenseReminder(context)

        // Reschedule for next day using saved preferences
        val db = AppDatabase.getInstance(context)
        CoroutineScope(Dispatchers.IO).launch {
            val settings = db.reminderDao().getReminderSettingsSync()
            if (settings?.dailyReminderEnabled == true) {
                AlarmScheduler.scheduleDailyReminder(
                    context,
                    settings.dailyReminderHour,
                    settings.dailyReminderMinute
                )
            }
        }
    }
}
