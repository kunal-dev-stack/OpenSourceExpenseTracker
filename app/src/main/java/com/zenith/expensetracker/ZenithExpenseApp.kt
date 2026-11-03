package com.zenith.expensetracker

import android.app.Application
import com.zenith.expensetracker.data.local.AppDatabase
import com.zenith.expensetracker.domain.notification.AlarmScheduler
import com.zenith.expensetracker.domain.notification.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ZenithExpenseApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // 1. Initialize Notification Channels (Android 8.0+)
        NotificationHelper.createNotificationChannels(this)

        // 2. Pre-initialize Room Database
        val db = AppDatabase.getInstance(this)

        // 3. Register Daily Reminder Alarm
        CoroutineScope(Dispatchers.IO).launch {
            val settings = db.reminderDao().getReminderSettingsSync()
            if (settings == null || settings.dailyReminderEnabled) {
                val hour = settings?.dailyReminderHour ?: 20
                val minute = settings?.dailyReminderMinute ?: 30
                AlarmScheduler.scheduleDailyReminder(this@ZenithExpenseApp, hour, minute)
            }
        }
    }
}

