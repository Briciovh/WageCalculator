package com.softeen.wagecalculator.data.model

enum class Currency(val symbol: String, val code: String) {
    USD("$", "USD"),
    MXN("$", "MXN")
}

enum class Frequency(val label: String) {
    YEARLY("yearly"),
    MONTHLY("monthly"),
    BI_WEEKLY("bi-weekly"),
    WEEKLY("weekly"),
    DAILY("daily"),
    HOURLY("hourly")
}

data class SalaryConfig(
    val baseCurrency: Currency = Currency.USD,
    val inputAmount: Double = 60000.0,
    val inputFrequency: Frequency = Frequency.YEARLY,
    val hoursPerWeek: Int = 40,
    val weeksPerYear: Int = 52,
    val exchangeRate: Double = 18.5 // 1 USD to MXN
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
    val usd: Double,
    val mxn: Double
)
