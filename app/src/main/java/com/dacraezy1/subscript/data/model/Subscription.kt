package com.dacraezy1.subscript.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Database entity representing a user's subscription or recurring cost.
 */
@Entity(tableName = "subscriptions")
data class Subscription(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "cost")
    val cost: Double,

    @ColumnInfo(name = "currency")
    val currency: String = "$",

    @ColumnInfo(name = "billingCycle")
    val billingCycle: String, // "Weekly", "Monthly", or "Yearly"

    @ColumnInfo(name = "nextDueDate")
    val nextDueDate: Long, // Epoch timestamp in milliseconds

    @ColumnInfo(name = "isNotificationEnabled")
    val isNotificationEnabled: Boolean = true
) {
    /**
     * Returns the parsed [BillingCycle] enum object.
     */
    val cycleEnum: BillingCycle
        get() = BillingCycle.fromString(billingCycle)

    /**
     * Calculates the normalized monthly cost equivalent.
     */
    val monthlyEquivalentCost: Double
        get() = cycleEnum.calculateMonthlyCost(cost)
}
