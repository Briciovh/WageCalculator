package com.softeen.wagecalculator.data.model

import androidx.annotation.StringRes
import com.softeen.wagecalculator.R

enum class Currency(val symbol: String, val code: String) {
    USD("$",   "USD"), CAD("C$",  "CAD"), MXN("$",   "MXN"),
    BRL("R$",  "BRL"), ARS("$",   "ARS"), COP("$",   "COP"),
    CLP("$",   "CLP"), PEN("S/",  "PEN"), GTQ("Q",   "GTQ"),
    CRC("₡",   "CRC"), DOP("$",   "DOP"), BOB("Bs.", "BOB"),
    UYU("\$U", "UYU"), HNL("L",   "HNL"), TTD("$",   "TTD")
}

enum class Frequency(@param:StringRes val labelRes: Int) {
    YEARLY(R.string.frequency_yearly),
    MONTHLY(R.string.frequency_monthly),
    BI_WEEKLY(R.string.frequency_bi_weekly),
    WEEKLY(R.string.frequency_weekly),
    DAILY(R.string.frequency_daily),
    HOURLY(R.string.frequency_hourly)
}

data class SalaryConfig(
    val baseCurrency: Currency = Currency.USD,
    val targetCurrency: Currency = Currency.MXN,
    val inputAmount: Double = 60000.0,
    val inputFrequency: Frequency = Frequency.YEARLY,
    val hoursPerWeek: Int = 40,
    val weeksPerYear: Int = 52,
    val exchangeRate: Double = 1.0
) {
    val annualHours: Int
        get() = hoursPerWeek * weeksPerYear

    fun calculateYearlyAmount(): Double {
        return when (inputFrequency) {
            Frequency.YEARLY -> inputAmount
            Frequency.MONTHLY -> inputAmount * 12
            Frequency.BI_WEEKLY -> inputAmount * 26
            Frequency.WEEKLY -> inputAmount * 52
            Frequency.DAILY -> inputAmount * 5 * weeksPerYear // Assuming 5 days a week
            Frequency.HOURLY -> inputAmount * annualHours
        }
    }
}

data class SalaryResults(
    val hourly: CurrencyPair,
    val yearly: CurrencyPair,
    val monthly: CurrencyPair,
    val biWeekly: CurrencyPair,
    val weekly: CurrencyPair,
    val daily: CurrencyPair,
    val annualHours: Int
)

data class CurrencyPair(
    val base: Double,
    val target: Double
)
