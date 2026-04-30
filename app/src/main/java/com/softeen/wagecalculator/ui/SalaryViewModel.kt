package com.softeen.wagecalculator.ui

import androidx.lifecycle.ViewModel
import com.softeen.wagecalculator.data.model.Currency
import com.softeen.wagecalculator.data.model.CurrencyPair
import com.softeen.wagecalculator.data.model.Frequency
import com.softeen.wagecalculator.data.model.SalaryConfig
import com.softeen.wagecalculator.data.model.SalaryResults
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SalaryViewModel : ViewModel() {
    private val _config = MutableStateFlow(SalaryConfig())
    val config: StateFlow<SalaryConfig> = _config.asStateFlow()

    private val _results = MutableStateFlow(calculateResults(_config.value))
    val results: StateFlow<SalaryResults> = _results.asStateFlow()

    fun updateConfig(update: (SalaryConfig) -> SalaryConfig) {
        _config.update {
            val newConfig = update(it)
            _results.value = calculateResults(newConfig)
            newConfig
        }
    }

    private fun calculateResults(config: SalaryConfig): SalaryResults {
        val yearlyBase = config.calculateYearlyAmount()
        
        val yearlyUsd = if (config.baseCurrency == Currency.USD) yearlyBase else yearlyBase / config.exchangeRate
        val yearlyMxn = if (config.baseCurrency == Currency.MXN) yearlyBase else yearlyBase * config.exchangeRate

        return SalaryResults(
            hourly = calculatePair(yearlyUsd, yearlyMxn) { it / config.annualHours },
            yearly = CurrencyPair(yearlyUsd, yearlyMxn),
            monthly = calculatePair(yearlyUsd, yearlyMxn) { it / 12 },
            biWeekly = calculatePair(yearlyUsd, yearlyMxn) { it / 26 },
            weekly = calculatePair(yearlyUsd, yearlyMxn) { it / 52 },
            daily = calculatePair(yearlyUsd, yearlyMxn) { it / (config.weeksPerYear * 5) }, // Assuming 5 days a week
            annualHours = config.annualHours
        )
    }

    private fun calculatePair(usd: Double, mxn: Double, transform: (Double) -> Double): CurrencyPair {
        return CurrencyPair(transform(usd), transform(mxn))
    }
}
