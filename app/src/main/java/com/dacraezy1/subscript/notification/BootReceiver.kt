package com.dacraezy1.subscript.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.dacraezy1.subscript.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * BroadcastReceiver triggered when the device boots or app is updated.
 * Restores all active subscription alarms since AlarmManager alarms are cleared on system restart.
 */
class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            Log.d(TAG, "Device booted or package replaced ($action). Rescheduling active subscription alarms...")

            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getDatabase(context)
                    val subscriptions = db.subscriptionDao().getActiveSubscriptionsWithNotifications()
                    val scheduler = SubscriptionAlarmScheduler(context)

                    scheduler.rescheduleAll(subscriptions)
                    Log.d(TAG, "Successfully rescheduled ${subscriptions.size} subscription alarms.")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to restore subscription alarms on boot: ${e.message}", e)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
