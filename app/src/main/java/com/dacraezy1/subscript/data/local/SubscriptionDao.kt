package com.dacraezy1.subscript.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.dacraezy1.subscript.data.model.Subscription
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for the subscriptions table.
 */
@Dao
interface SubscriptionDao {

    @Query("SELECT * FROM subscriptions ORDER BY nextDueDate ASC")
    fun getAllSubscriptions(): Flow<List<Subscription>>

    @Query("SELECT * FROM subscriptions WHERE id = :id")
    fun getSubscriptionById(id: Int): Flow<Subscription?>

    @Query("SELECT * FROM subscriptions WHERE id = :id")
    suspend fun getSubscriptionByIdDirect(id: Int): Subscription?

    @Query("SELECT * FROM subscriptions WHERE isNotificationEnabled = 1 ORDER BY nextDueDate ASC")
    suspend fun getActiveSubscriptionsWithNotifications(): List<Subscription>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubscription(subscription: Subscription): Long

    @Update
    suspend fun updateSubscription(subscription: Subscription)

    @Delete
    suspend fun deleteSubscription(subscription: Subscription)

    @Query("DELETE FROM subscriptions WHERE id = :id")
    suspend fun deleteSubscriptionById(id: Int)
}
