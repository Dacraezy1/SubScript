package com.dacraezy1.subscript

import com.dacraezy1.subscript.data.model.BillingCycle
import com.dacraezy1.subscript.data.model.Subscription
import com.dacraezy1.subscript.ui.util.DateUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class BillingCycleTest {

    @Test
    fun testMonthlyCostCalculations() {
        val monthlySub = Subscription(
            id = 1,
            name = "Music Streaming",
            cost = 10.0,
            currency = "$",
            billingCycle = "Monthly",
            nextDueDate = System.currentTimeMillis()
        )
        assertEquals(10.0, monthlySub.monthlyEquivalentCost, 0.001)

        val yearlySub = Subscription(
            id = 2,
            name = "Cloud Backup",
            cost = 120.0,
            currency = "$",
            billingCycle = "Yearly",
            nextDueDate = System.currentTimeMillis()
        )
        assertEquals(10.0, yearlySub.monthlyEquivalentCost, 0.001)

        val weeklySub = Subscription(
            id = 3,
            name = "Weekly Magazine",
            cost = 5.0,
            currency = "$",
            billingCycle = "Weekly",
            nextDueDate = System.currentTimeMillis()
        )
        // 5 * 52 / 12 = 21.6666...
        assertEquals((5.0 * 52.0) / 12.0, weeklySub.monthlyEquivalentCost, 0.001)
    }

    @Test
    fun testBillingCycleFromString() {
        assertEquals(BillingCycle.WEEKLY, BillingCycle.fromString("weekly"))
        assertEquals(BillingCycle.WEEKLY, BillingCycle.fromString("Weekly"))
        assertEquals(BillingCycle.MONTHLY, BillingCycle.fromString("Monthly"))
        assertEquals(BillingCycle.YEARLY, BillingCycle.fromString("Yearly"))
        // Fallback default
        assertEquals(BillingCycle.MONTHLY, BillingCycle.fromString("Unknown"))
    }

    @Test
    fun testNextDueDateCalculation() {
        val zoneId = ZoneId.systemDefault()
        val initialDate = LocalDate.of(2026, 1, 1).atStartOfDay(zoneId).toInstant().toEpochMilli()

        val nextWeek = BillingCycle.WEEKLY.calculateNextDueDate(initialDate)
        val nextWeekDate = LocalDate.ofInstant(java.time.Instant.ofEpochMilli(nextWeek), zoneId)
        assertEquals(LocalDate.of(2026, 1, 8), nextWeekDate)

        val nextMonth = BillingCycle.MONTHLY.calculateNextDueDate(initialDate)
        val nextMonthDate = LocalDate.ofInstant(java.time.Instant.ofEpochMilli(nextMonth), zoneId)
        assertEquals(LocalDate.of(2026, 2, 1), nextMonthDate)

        val nextYear = BillingCycle.YEARLY.calculateNextDueDate(initialDate)
        val nextYearDate = LocalDate.ofInstant(java.time.Instant.ofEpochMilli(nextYear), zoneId)
        assertEquals(LocalDate.of(2027, 1, 1), nextYearDate)
    }

    @Test
    fun testDateUtilsFormatCost() {
        assertEquals("$15.00", DateUtils.formatCost(15.0, "$"))
        assertEquals("€12.50", DateUtils.formatCost(12.5, "€"))
    }
}
