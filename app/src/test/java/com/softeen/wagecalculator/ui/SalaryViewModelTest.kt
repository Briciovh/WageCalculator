package com.softeen.wagecalculator.ui

import com.softeen.wagecalculator.data.model.Currency
import com.softeen.wagecalculator.data.model.Frequency
import com.softeen.wagecalculator.data.model.SalaryConfig
import com.softeen.wagecalculator.fake.FakeSalaryConfigRepository
import com.softeen.wagecalculator.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SalaryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repo: FakeSalaryConfigRepository
    private lateinit var viewModel: SalaryViewModel

    @Before
    fun setup() {
        repo = FakeSalaryConfigRepository()
        viewModel = SalaryViewModel(repo)
    }

    @Test
    fun init_loadsConfigFromRepository() = runTest {
        val custom = SalaryConfig(inputAmount = 120_000.0)
        repo.setInitialConfig(custom)
        val vm = SalaryViewModel(repo)
        advanceUntilIdle()
        assertEquals(120_000.0, vm.config.value.inputAmount, 0.01)
    }

    @Test
    fun updateConfig_updatesStateFlow() {
        viewModel.updateConfig { it.copy(inputAmount = 90_000.0) }
        assertEquals(90_000.0, viewModel.config.value.inputAmount, 0.01)
    }

    @Test
    fun updateConfig_persistsToRepository() = runTest {
        viewModel.updateConfig { it.copy(inputAmount = 80_000.0) }
        advanceUntilIdle()
        assertEquals(80_000.0, repo.lastSaved?.inputAmount ?: 0.0, 0.01)
    }

    @Test
    fun currencyChange_baseCurrency_resetsExchangeRate() {
        viewModel.updateConfig { it.copy(exchangeRate = 18.5) }
        viewModel.updateConfig { it.copy(baseCurrency = Currency.BRL) }
        assertEquals(1.0, viewModel.config.value.exchangeRate, 0.001)
    }

    @Test
    fun currencyChange_targetCurrency_resetsExchangeRate() {
        viewModel.updateConfig { it.copy(exchangeRate = 18.5) }
        viewModel.updateConfig { it.copy(targetCurrency = Currency.CAD) }
        assertEquals(1.0, viewModel.config.value.exchangeRate, 0.001)
    }

    @Test
    fun nonCurrencyChange_keepsExchangeRate() {
        viewModel.updateConfig { it.copy(exchangeRate = 18.5) }
        viewModel.updateConfig { it.copy(inputAmount = 70_000.0) }
        assertEquals(18.5, viewModel.config.value.exchangeRate, 0.001)
    }

    @Test
    fun results_targetIsBaseTimesExchangeRate() {
        viewModel.updateConfig {
            it.copy(inputAmount = 60_000.0, inputFrequency = Frequency.YEARLY, exchangeRate = 2.0)
        }
        val yearly = viewModel.results.value.yearly
        assertEquals(yearly.base * 2.0, yearly.target, 0.01)
    }

    @Test
    fun results_monthlyIsYearlyDividedBy12() {
        viewModel.updateConfig { it.copy(inputAmount = 60_000.0, inputFrequency = Frequency.YEARLY) }
        val results = viewModel.results.value
        assertEquals(results.yearly.base / 12, results.monthly.base, 0.01)
    }

    @Test
    fun results_weeklyIsYearlyDividedBy52() {
        viewModel.updateConfig { it.copy(inputAmount = 60_000.0, inputFrequency = Frequency.YEARLY) }
        val results = viewModel.results.value
        assertEquals(results.yearly.base / 52, results.weekly.base, 0.01)
    }
}
