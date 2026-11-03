package com.zenith.expensetracker.domain.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.zenith.expensetracker.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED || intent?.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
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
}
