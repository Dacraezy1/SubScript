package com.dacraezy1.subscript.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.dacraezy1.subscript.data.model.Subscription
import java.util.concurrent.TimeUnit

/**
 * Helper class to schedule and cancel local native alarms for upcoming subscription bills.
 */
class SubscriptionAlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    companion object {
        private const val TAG = "SubscriptionAlarmSched"
        const val ACTION_SUBSCRIPTION_REMINDER = "com.dacraezy1.subscript.ACTION_SUBSCRIPTION_REMINDER"

        const val EXTRA_SUBSCRIPTION_ID = "extra_subscription_id"
        const val EXTRA_SUBSCRIPTION_NAME = "extra_subscription_name"
        const val EXTRA_SUBSCRIPTION_COST = "extra_subscription_cost"
        const val EXTRA_SUBSCRIPTION_CURRENCY = "extra_subscription_currency"
        const val EXTRA_SUBSCRIPTION_CYCLE = "extra_subscription_cycle"
        const val EXTRA_SUBSCRIPTION_DUE_DATE = "extra_subscription_due_date"

        // Reminder offset: exactly 24 hours prior to due date
        val REMINDER_OFFSET_MILLIS = TimeUnit.HOURS.toMillis(24)
    }

    /**
     * Schedules an alarm 24 hours prior to [Subscription.nextDueDate].
     * If the 24-hour mark has already passed but the due date is still in the future,
     * triggers an immediate alert.
     */
    fun scheduleReminder(subscription: Subscription) {
        if (!subscription.isNotificationEnabled) {
            cancelReminder(subscription.id)
            return
        }

        val currentTime = System.currentTimeMillis()
        val calculatedReminderTime = subscription.nextDueDate - REMINDER_OFFSET_MILLIS

        val triggerTime = when {
            // Calculated 24-hour warning is in the future: schedule normally
            calculatedReminderTime > currentTime -> calculatedReminderTime

            // Already within the 24-hour window before due date: alert user shortly (e.g., 5 seconds)
            subscription.nextDueDate > currentTime -> currentTime + 5_000L

            // Due date is already in the past: do not trigger outdated alarm
            else -> {
                Log.d(TAG, "Subscription ${subscription.name} due date is in the past, skipping alarm.")
                return
            }
        }

        val intent = Intent(context, SubscriptionBroadcastReceiver::class.java).apply {
            action = ACTION_SUBSCRIPTION_REMINDER
            putExtra(EXTRA_SUBSCRIPTION_ID, subscription.id)
            putExtra(EXTRA_SUBSCRIPTION_NAME, subscription.name)
            putExtra(EXTRA_SUBSCRIPTION_COST, subscription.cost)
            putExtra(EXTRA_SUBSCRIPTION_CURRENCY, subscription.currency)
            putExtra(EXTRA_SUBSCRIPTION_CYCLE, subscription.billingCycle)
            putExtra(EXTRA_SUBSCRIPTION_DUE_DATE, subscription.nextDueDate)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            subscription.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
            Log.d(TAG, "Scheduled alarm for ${subscription.name} (ID: ${subscription.id}) at $triggerTime")
        } catch (e: SecurityException) {
            Log.w(TAG, "Permission denied for exact alarm, falling back to inexact: ${e.message}")
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }

    /**
     * Cancels an existing alarm for the specified subscription ID.
     */
    fun cancelReminder(subscriptionId: Int) {
        val intent = Intent(context, SubscriptionBroadcastReceiver::class.java).apply {
            action = ACTION_SUBSCRIPTION_REMINDER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            subscriptionId,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d(TAG, "Cancelled alarm for subscription ID: $subscriptionId")
        }
    }

    /**
     * Reschedules reminders for all given subscriptions.
     */
    fun rescheduleAll(subscriptions: List<Subscription>) {
        subscriptions.filter { it.isNotificationEnabled }.forEach { scheduleReminder(it) }
    }
}
