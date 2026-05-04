package com.softeen.wagecalculator.data.model

import org.junit.Assert.assertEquals
import org.junit.Test

class SalaryConfigTest {

    @Test
    fun yearly_inputPassesThrough() {
        val config = SalaryConfig(inputAmount = 60_000.0, inputFrequency = Frequency.YEARLY)
        assertEquals(60_000.0, config.calculateYearlyAmount(), 0.01)
    }

    @Test
    fun monthly_multipliesBy12() {
        val config = SalaryConfig(inputAmount = 5_000.0, inputFrequency = Frequency.MONTHLY)
        assertEquals(60_000.0, config.calculateYearlyAmount(), 0.01)
    }

    @Test
    fun biWeekly_multipliesBy26() {
        val config = SalaryConfig(inputAmount = 2_307.69, inputFrequency = Frequency.BI_WEEKLY)
        assertEquals(60_000.0, config.calculateYearlyAmount(), 1.0)
    }

    @Test
    fun weekly_multipliesBy52() {
        val config = SalaryConfig(inputAmount = 1_153.84, inputFrequency = Frequency.WEEKLY)
        assertEquals(60_000.0, config.calculateYearlyAmount(), 1.0)
    }

    @Test
    fun daily_multipliesBy5TimesWeeks() {
        val config = SalaryConfig(inputAmount = 100.0, inputFrequency = Frequency.DAILY, weeksPerYear = 52)
        assertEquals(26_000.0, config.calculateYearlyAmount(), 0.01)
    }

    @Test
    fun hourly_multipliesByAnnualHours() {
        val config = SalaryConfig(inputAmount = 28.85, inputFrequency = Frequency.HOURLY,
            hoursPerWeek = 40, weeksPerYear = 52)
        // 28.85 * 2080 = 60_008.0
        assertEquals(60_008.0, config.calculateYearlyAmount(), 1.0)
    }

    @Test
    fun annualHours_isProductOfHoursAndWeeks() {
        assertEquals(2080, SalaryConfig(hoursPerWeek = 40, weeksPerYear = 52).annualHours)
    }

    @Test
    fun annualHours_zeroHoursReturnsZero() {
        assertEquals(0, SalaryConfig(hoursPerWeek = 0).annualHours)
    }
}
