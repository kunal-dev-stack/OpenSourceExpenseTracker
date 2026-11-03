package com.zenith.expensetracker.domain.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.zenith.expensetracker.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddMoneyReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val db = AppDatabase.getInstance(context)
        CoroutineScope(Dispatchers.IO).launch {
            val accounts = db.accountDao().getAllAccountsSync()
            for (acc in accounts) {
                if (acc.balance < acc.lowBalanceThreshold) {
                    NotificationHelper.sendLowBalanceNotification(
                        context = context,
                        accountName = acc.name,
                        currentBalance = acc.balance,
                        threshold = acc.lowBalanceThreshold,
                        currency = acc.currency
                    )
                }
            }
        }
    }
}
