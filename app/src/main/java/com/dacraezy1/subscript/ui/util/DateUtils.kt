package com.dacraezy1.subscript.ui.util

import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

/**
 * Utility functions for date calculations, relative day counts, and currency formatting.
 */
object DateUtils {

    private val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")

    /**
     * Formats epoch milliseconds into a human-readable string (e.g. "Oct 15, 2026").
     */
    fun formatDate(timestampMillis: Long): String {
        return try {
            val localDate = Instant.ofEpochMilli(timestampMillis)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            localDate.format(dateFormatter)
        } catch (_: Exception) {
            "Invalid Date"
        }
    }

    /**
     * Calculates the number of calendar days until the given timestamp from today.
     * Negative values indicate overdue.
     */
    fun getDaysUntil(timestampMillis: Long): Long {
        return try {
            val today = LocalDate.now(ZoneId.systemDefault())
            val target = Instant.ofEpochMilli(timestampMillis)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            ChronoUnit.DAYS.between(today, target)
        } catch (_: Exception) {
            0L
        }
    }

    /**
     * Returns a human-friendly due status label.
     */
    fun getDueStatusText(timestampMillis: Long): String {
        val days = getDaysUntil(timestampMillis)
        return when {
            days < 0 -> "Overdue by ${-days}d"
            days == 0L -> "Due today"
            days == 1L -> "Due tomorrow"
            else -> "Due in $days days"
        }
    }

    /**
     * Formats an amount with currency symbol (e.g. "$14.99").
     */
    fun formatCost(amount: Double, currency: String): String {
        val formattedNumber = String.format(Locale.getDefault(), "%.2f", amount)
        return "$currency$formattedNumber"
    }

    /**
     * Returns epoch milliseconds for today at 00:00 in system timezone.
     */
    fun getTodayStartMillis(): Long {
        return LocalDate.now(ZoneId.systemDefault())
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }

    /**
     * Returns epoch milliseconds for tomorrow at 00:00 in system timezone.
     */
    fun getTomorrowStartMillis(): Long {
        return LocalDate.now(ZoneId.systemDefault())
            .plusDays(1)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }
}
