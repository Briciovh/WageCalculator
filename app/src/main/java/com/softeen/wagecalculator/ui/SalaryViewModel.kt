package com.softeen.wagecalculator.ui

import androidx.lifecycle.ViewModel
import com.softeen.wagecalculator.data.model.CurrencyPair
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.softeen.wagecalculator.data.model.SalaryConfig
import com.softeen.wagecalculator.data.model.SalaryResults
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@HiltViewModel
class SalaryViewModel @Inject constructor() : ViewModel() {
    private val _config = MutableStateFlow(SalaryConfig())
    val config: StateFlow<SalaryConfig> = _config.asStateFlow()

    private val _results = MutableStateFlow(calculateResults(_config.value))
    val results: StateFlow<SalaryResults> = _results.asStateFlow()

    fun updateConfig(update: (SalaryConfig) -> SalaryConfig) {
        _config.update {
            val new = update(it)
            val pairChanged = new.baseCurrency != it.baseCurrency || new.targetCurrency != it.targetCurrency
            val final = if (pairChanged) new.copy(exchangeRate = 1.0) else new
            _results.value = calculateResults(final)
            final
        }
    }

    private fun calculateResults(config: SalaryConfig): SalaryResults {
        val yearlyBase = config.calculateYearlyAmount()
        val yearlyTarget = yearlyBase * config.exchangeRate

        return SalaryResults(
            hourly = calculatePair(yearlyBase, yearlyTarget) { it / config.annualHours },
            yearly = CurrencyPair(yearlyBase, yearlyTarget),
            monthly = calculatePair(yearlyBase, yearlyTarget) { it / 12 },
            biWeekly = calculatePair(yearlyBase, yearlyTarget) { it / 26 },
            weekly = calculatePair(yearlyBase, yearlyTarget) { it / 52 },
            daily = calculatePair(yearlyBase, yearlyTarget) { it / (config.weeksPerYear * 5) },
            annualHours = config.annualHours
        )
    }

    private fun calculatePair(base: Double, target: Double, transform: (Double) -> Double): CurrencyPair {
        return CurrencyPair(transform(base), transform(target))
    }
}
