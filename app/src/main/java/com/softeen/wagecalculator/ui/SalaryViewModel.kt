package com.softeen.wagecalculator.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.softeen.wagecalculator.data.model.CurrencyPair
import com.softeen.wagecalculator.data.model.SalaryConfig
import com.softeen.wagecalculator.data.model.SalaryResults
import com.softeen.wagecalculator.data.repository.SalaryConfigRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SalaryViewModel @Inject constructor(
    private val repository: SalaryConfigRepository
) : ViewModel() {

    private val _config = MutableStateFlow(SalaryConfig())
    val config: StateFlow<SalaryConfig> = _config.asStateFlow()

    private val _results = MutableStateFlow(calculateResults(_config.value))
    val results: StateFlow<SalaryResults> = _results.asStateFlow()

    init {
        viewModelScope.launch {
            val saved = repository.config.first()
            _config.value = saved
            _results.value = calculateResults(saved)
        }
    }

    fun updateConfig(update: (SalaryConfig) -> SalaryConfig) {
        _config.update {
            val new = update(it)
            val pairChanged = new.baseCurrency != it.baseCurrency || new.targetCurrency != it.targetCurrency
            val final = if (pairChanged) new.copy(exchangeRate = 1.0) else new
            _results.value = calculateResults(final)
            viewModelScope.launch { repository.save(final) }
            final
        }
    }

    private fun calculateResults(config: SalaryConfig): SalaryResults {
        val yearlyBase = config.calculateYearlyAmount()
        val yearlyTarget = yearlyBase * config.exchangeRate

        return SalaryResults(
            hourly   = calculatePair(yearlyBase, yearlyTarget) { it / config.annualHours },
            yearly   = CurrencyPair(yearlyBase, yearlyTarget),
            monthly  = calculatePair(yearlyBase, yearlyTarget) { it / 12 },
            biWeekly = calculatePair(yearlyBase, yearlyTarget) { it / 26 },
            weekly   = calculatePair(yearlyBase, yearlyTarget) { it / 52 },
            daily    = calculatePair(yearlyBase, yearlyTarget) { it / (config.weeksPerYear * 5) },
            annualHours = config.annualHours
        )
    }

    private fun calculatePair(base: Double, target: Double, transform: (Double) -> Double): CurrencyPair =
        CurrencyPair(transform(base), transform(target))
}
