package com.dacraezy1.subscript.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.dacraezy1.subscript.MainActivity
import com.dacraezy1.subscript.R
import com.dacraezy1.subscript.SubScriptApp
import com.dacraezy1.subscript.data.local.AppDatabase
import com.dacraezy1.subscript.data.model.BillingCycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * BroadcastReceiver triggered by AlarmManager 24 hours before a subscription bill is due.
 * Posts a local native notification and rolls the schedule forward for recurring cycles.
 */
class SubscriptionBroadcastReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "SubscriptionReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != SubscriptionAlarmScheduler.ACTION_SUBSCRIPTION_REMINDER) {
            return
        }

        val subscriptionId = intent.getIntExtra(SubscriptionAlarmScheduler.EXTRA_SUBSCRIPTION_ID, -1)
        val name = intent.getStringExtra(SubscriptionAlarmScheduler.EXTRA_SUBSCRIPTION_NAME) ?: "Subscription"
        val cost = intent.getDoubleExtra(SubscriptionAlarmScheduler.EXTRA_SUBSCRIPTION_COST, 0.0)
        val currency = intent.getStringExtra(SubscriptionAlarmScheduler.EXTRA_SUBSCRIPTION_CURRENCY) ?: "$"
        val cycle = intent.getStringExtra(SubscriptionAlarmScheduler.EXTRA_SUBSCRIPTION_CYCLE) ?: "Monthly"
        val nextDueDate = intent.getLongExtra(SubscriptionAlarmScheduler.EXTRA_SUBSCRIPTION_DUE_DATE, 0L)

        Log.d(TAG, "Received reminder alarm for $name (ID: $subscriptionId, Due: $nextDueDate)")

        // Display Notification to user
        showNotification(context, subscriptionId, name, cost, currency)

        // Advance to next billing cycle in local Room database
        if (subscriptionId != -1) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getDatabase(context)
                    val subscriptionDao = db.subscriptionDao()
                    val existing = subscriptionDao.getSubscriptionByIdDirect(subscriptionId)

                    if (existing != null && existing.isNotificationEnabled) {
                        val billingCycle = BillingCycle.fromString(existing.billingCycle)
                        val nextCycleDueDate = billingCycle.calculateNextDueDate(existing.nextDueDate)

                        val updated = existing.copy(nextDueDate = nextCycleDueDate)
                        subscriptionDao.updateSubscription(updated)

                        // Schedule the next cycle's alarm
                        val scheduler = SubscriptionAlarmScheduler(context)
                        scheduler.scheduleReminder(updated)
                        Log.d(TAG, "Updated $name to next due date: $nextCycleDueDate and rescheduled")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating subscription next due date: ${e.message}", e)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }

    private fun showNotification(
        context: Context,
        notificationId: Int,
        name: String,
        cost: Double,
        currency: String
    ) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create intent to open MainActivity when tapped
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(SubscriptionAlarmScheduler.EXTRA_SUBSCRIPTION_ID, notificationId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val formattedCost = String.format(Locale.getDefault(), "%.2f", cost)
        val title = context.getString(R.string.notification_title_upcoming, name)
        val message = context.getString(R.string.notification_content_upcoming, name, currency, cost)

        val notification = NotificationCompat.Builder(context, SubScriptApp.CHANNEL_ID_SUBSCRIPTIONS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$message. Check your account or payment method to avoid interruption.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(
            if (notificationId != -1) notificationId else System.currentTimeMillis().toInt(),
            notification
        )
    }
}
