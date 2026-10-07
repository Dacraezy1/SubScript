package com.dacraezy1.subscript.data.model

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Supported subscription billing cycles.
 */
enum class BillingCycle(val displayName: String) {
    WEEKLY("Weekly") {
        override fun calculateMonthlyCost(cost: Double): Double = (cost * 52.0) / 12.0

        override fun calculateNextDueDate(currentDueDateMillis: Long): Long {
            val zoneId = ZoneId.systemDefault()
            val localDate = Instant.ofEpochMilli(currentDueDateMillis).atZone(zoneId).toLocalDate()
            val nextDate = localDate.plusWeeks(1)
            return nextDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
        }
    },
    MONTHLY("Monthly") {
        override fun calculateMonthlyCost(cost: Double): Double = cost

        override fun calculateNextDueDate(currentDueDateMillis: Long): Long {
            val zoneId = ZoneId.systemDefault()
            val localDate = Instant.ofEpochMilli(currentDueDateMillis).atZone(zoneId).toLocalDate()
            val nextDate = localDate.plusMonths(1)
            return nextDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
        }
    },
    YEARLY("Yearly") {
        override fun calculateMonthlyCost(cost: Double): Double = cost / 12.0

        override fun calculateNextDueDate(currentDueDateMillis: Long): Long {
            val zoneId = ZoneId.systemDefault()
            val localDate = Instant.ofEpochMilli(currentDueDateMillis).atZone(zoneId).toLocalDate()
            val nextDate = localDate.plusYears(1)
            return nextDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
        }
    };

    abstract fun calculateMonthlyCost(cost: Double): Double
    abstract fun calculateNextDueDate(currentDueDateMillis: Long): Long

    companion object {
        fun fromString(value: String): BillingCycle {
            return entries.firstOrNull { 
                it.name.equals(value, ignoreCase = true) || it.displayName.equals(value, ignoreCase = true)
            } ?: MONTHLY
        }
    }
}
