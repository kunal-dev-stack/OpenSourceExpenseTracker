package com.zenith.expensetracker.domain.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.zenith.expensetracker.MainActivity
import com.zenith.expensetracker.R

object NotificationHelper {

    const val CHANNEL_DAILY_REMINDER_ID = "zenith_daily_reminders"
    const val CHANNEL_ALERTS_ID = "zenith_account_alerts"

    const val NOTIFICATION_DAILY_REMINDER_ID = 1001
    const val NOTIFICATION_LOW_BALANCE_ID = 1002
    const val NOTIFICATION_BUDGET_ALERT_ID = 1003

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Daily Reminder Channel
            val dailyChannel = NotificationChannel(
                CHANNEL_DAILY_REMINDER_ID,
                context.getString(R.string.channel_daily_reminders_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.channel_daily_reminders_desc)
                enableVibration(true)
            }

            // Alerts Channel
            val alertChannel = NotificationChannel(
                CHANNEL_ALERTS_ID,
                context.getString(R.string.channel_alerts_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.channel_alerts_desc)
                enableVibration(true)
            }

            notificationManager.createNotificationChannel(dailyChannel)
            notificationManager.createNotificationChannel(alertChannel)
        }
    }

    fun sendDailyExpenseReminder(context: Context) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "add_transaction")
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_DAILY_REMINDER_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Time to log your daily expenses! 💰")
            .setContentText("Keep your finances on track. Tap here to record today's spending in seconds.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_input_add, "Add Expense", pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_DAILY_REMINDER_ID, notification)
        } catch (_: SecurityException) {
            // Android 13+ permission not yet granted by user
        }
    }

    fun sendLowBalanceNotification(
        context: Context,
        accountName: String,
        currentBalance: Double,
        threshold: Double,
        currency: String
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "accounts")
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val formattedBalance = String.format("%.2f", currentBalance)
        val formattedThreshold = String.format("%.2f", threshold)

        val notification = NotificationCompat.Builder(context, CHANNEL_ALERTS_ID)
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setContentTitle("⚠️ Add Money Alert: $accountName is Low!")
            .setContentText("Current balance is $currency$formattedBalance (under minimum $currency$formattedThreshold). Tap to add funds.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_menu_upload, "Add Money", pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_LOW_BALANCE_ID + accountName.hashCode(), notification)
        } catch (_: SecurityException) {
            // Permission not granted
        }
    }

    fun sendBudgetAlertNotification(
        context: Context,
        categoryName: String,
        spent: Double,
        limit: Double,
        currency: String
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "budgets")
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            2,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val percentage = ((spent / limit) * 100).toInt()
        val formattedSpent = String.format("%.2f", spent)
        val formattedLimit = String.format("%.2f", limit)

        val title = if (percentage >= 100) {
            "🚨 Budget Exceeded: $categoryName"
        } else {
            "⚠️ Budget Warning: $categoryName at $percentage%"
        }

        val message = "Spent $currency$formattedSpent of $currency$formattedLimit monthly limit."

        val notification = NotificationCompat.Builder(context, CHANNEL_ALERTS_ID)
            .setSmallIcon(android.R.drawable.stat_notify_more)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_BUDGET_ALERT_ID + categoryName.hashCode(), notification)
        } catch (_: SecurityException) {
            // Permission not granted
        }
    }
}
