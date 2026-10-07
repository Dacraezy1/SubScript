package com.dacraezy1.subscript

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.dacraezy1.subscript.data.local.AppDatabase
import com.dacraezy1.subscript.data.repository.SubscriptionRepository
import com.dacraezy1.subscript.notification.SubscriptionAlarmScheduler

/**
 * Application class initializing local database, notification channels,
 * and service dependencies without any cloud or proprietary SDKs.
 */
class SubScriptApp : Application() {

    companion object {
        const val CHANNEL_ID_SUBSCRIPTIONS = "subscription_reminders"
        lateinit var instance: SubScriptApp
            private set
    }

    val database: AppDatabase by lazy {
        AppDatabase.getDatabase(this)
    }

    val alarmScheduler: SubscriptionAlarmScheduler by lazy {
        SubscriptionAlarmScheduler(this)
    }

    val repository: SubscriptionRepository by lazy {
        SubscriptionRepository(database.subscriptionDao(), alarmScheduler)
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.notification_channel_name)
            val descriptionText = getString(R.string.notification_channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH

            val channel = NotificationChannel(CHANNEL_ID_SUBSCRIPTIONS, name, importance).apply {
                description = descriptionText
                enableVibration(true)
                setShowBadge(true)
            }

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
