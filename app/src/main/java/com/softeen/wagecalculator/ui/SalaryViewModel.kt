package com.softeen.wagecalculator.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.softeen.wagecalculator.data.model.CurrencyPair
import com.softeen.wagecalculator.data.model.SalaryConfig
import com.softeen.wagecalculator.data.model.SalaryResults
import com.softeen.wagecalculator.data.repository.ExchangeRateRepository
import com.softeen.wagecalculator.data.repository.SalaryConfigRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SalaryViewModel @Inject constructor(
    private val repository: SalaryConfigRepository,
    private val exchangeRateRepository: ExchangeRateRepository
) : ViewModel() {

    private val _config = MutableStateFlow(SalaryConfig())
    val config: StateFlow<SalaryConfig> = _config.asStateFlow()

    private val _results = MutableStateFlow(calculateResults(_config.value))
    val results: StateFlow<SalaryResults> = _results.asStateFlow()

    private val _isLoadingRate = MutableStateFlow(false)
    val isLoadingRate: StateFlow<Boolean> = _isLoadingRate.asStateFlow()

    private val _rateError = MutableStateFlow<String?>(null)
    val rateError: StateFlow<String?> = _rateError.asStateFlow()

    private var rateFetchJob: Job? = null

    init {
        viewModelScope.launch {
            val saved = repository.config.first()
            _config.value = saved
            _results.value = calculateResults(saved)
        }
    }

    fun updateConfig(update: (SalaryConfig) -> SalaryConfig) {
        var fetchArgs: Pair<String, String>? = null
        _config.update { current ->
            val new = update(current)
            val pairChanged = new.baseCurrency != current.baseCurrency || new.targetCurrency != current.targetCurrency

            if (pairChanged) {
                if (new.baseCurrency == new.targetCurrency) {
                    val final = new.copy(exchangeRate = 1.0)
                    _results.value = calculateResults(final)
                    viewModelScope.launch { repository.save(final) }
                    final
                } else {
                    val configWithResetRate = new.copy(exchangeRate = 1.0)
                    _results.value = calculateResults(configWithResetRate)
                    viewModelScope.launch { repository.save(configWithResetRate) }
                    fetchArgs = Pair(new.baseCurrency.code, new.targetCurrency.code)
                    configWithResetRate
                }
            } else {
                _results.value = calculateResults(new)
                viewModelScope.launch { repository.save(new) }
                new
            }
        }
        fetchArgs?.let { (base, target) -> fetchExchangeRate(base, target) }
    }

    private fun fetchExchangeRate(base: String, target: String) {
        rateFetchJob?.cancel()
        _isLoadingRate.value = true
        _rateError.value = null
        rateFetchJob = viewModelScope.launch {
            exchangeRateRepository.getExchangeRate(base, target)
                .onSuccess { rate ->
                    _config.update { current ->
                        if (current.baseCurrency.code == base && current.targetCurrency.code == target) {
                            val final = current.copy(exchangeRate = rate)
                            _results.value = calculateResults(final)
                            viewModelScope.launch { repository.save(final) }
                            final
                        } else current
                    }
                    _isLoadingRate.value = false
                }
                .onFailure { e ->
                    _isLoadingRate.value = false
                    _rateError.value = e.message ?: "Failed to fetch exchange rate"
                }
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
