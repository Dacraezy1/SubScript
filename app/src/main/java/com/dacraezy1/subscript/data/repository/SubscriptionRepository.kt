package com.dacraezy1.subscript.data.repository

import com.dacraezy1.subscript.data.local.SubscriptionDao
import com.dacraezy1.subscript.data.model.Subscription
import com.dacraezy1.subscript.notification.SubscriptionAlarmScheduler
import kotlinx.coroutines.flow.Flow

/**
 * Repository coordinating local Room database operations and AlarmManager scheduling.
 */
class SubscriptionRepository(
    private val subscriptionDao: SubscriptionDao,
    private val alarmScheduler: SubscriptionAlarmScheduler
) {

    /**
     * Observable stream of all subscriptions sorted by upcoming due date.
     */
    val allSubscriptions: Flow<List<Subscription>> = subscriptionDao.getAllSubscriptions()

    /**
     * Observable single subscription by ID.
     */
    fun getSubscriptionById(id: Int): Flow<Subscription?> = subscriptionDao.getSubscriptionById(id)

    /**
     * Inserts a new subscription into the local database and schedules a 24-hour reminder alarm.
     */
    suspend fun addSubscription(subscription: Subscription): Long {
        val insertedId = subscriptionDao.insertSubscription(subscription)
        val savedSubscription = subscription.copy(id = insertedId.toInt())

        if (savedSubscription.isNotificationEnabled) {
            alarmScheduler.scheduleReminder(savedSubscription)
        }
        return insertedId
    }

    /**
     * Updates an existing subscription and updates its reminder alarm.
     */
    suspend fun updateSubscription(subscription: Subscription) {
        subscriptionDao.updateSubscription(subscription)
        if (subscription.isNotificationEnabled) {
            alarmScheduler.scheduleReminder(subscription)
        } else {
            alarmScheduler.cancelReminder(subscription.id)
        }
    }

    /**
     * Deletes a subscription and cancels any pending reminder alarm.
     */
    suspend fun deleteSubscription(subscription: Subscription) {
        alarmScheduler.cancelReminder(subscription.id)
        subscriptionDao.deleteSubscription(subscription)
    }

    /**
     * Reschedules all alarms for active subscriptions.
     */
    suspend fun rescheduleAllAlarms() {
        val activeSubscriptions = subscriptionDao.getActiveSubscriptionsWithNotifications()
        alarmScheduler.rescheduleAll(activeSubscriptions)
    }
}
