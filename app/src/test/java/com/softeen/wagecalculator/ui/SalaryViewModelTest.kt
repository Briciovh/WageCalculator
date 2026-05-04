package com.softeen.wagecalculator.ui

import com.softeen.wagecalculator.data.model.Currency
import com.softeen.wagecalculator.data.model.Frequency
import com.softeen.wagecalculator.data.model.SalaryConfig
import com.softeen.wagecalculator.fake.FakeExchangeRateRepository
import com.softeen.wagecalculator.fake.FakeSalaryConfigRepository
import com.softeen.wagecalculator.util.MainDispatcherRule
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SalaryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repo: FakeSalaryConfigRepository
    private lateinit var exchangeRepo: FakeExchangeRateRepository
    private lateinit var viewModel: SalaryViewModel

    @Before
    fun setup() {
        repo = FakeSalaryConfigRepository()
        exchangeRepo = FakeExchangeRateRepository()
        viewModel = SalaryViewModel(repo, exchangeRepo)
    }

    @Test
    fun init_loadsConfigFromRepository() = runTest {
        val custom = SalaryConfig(inputAmount = 120_000.0)
        repo.setInitialConfig(custom)
        val vm = SalaryViewModel(repo, exchangeRepo)
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
    fun currencyChange_triggersExchangeRateFetch() = runTest {
        exchangeRepo.rateToReturn = 17.5
        viewModel.updateConfig { it.copy(baseCurrency = Currency.CAD, targetCurrency = Currency.MXN) }
        
        // Use TestCoroutineScheduler directly via advanceUntilIdle
        advanceUntilIdle()
        
        assertEquals(17.5, viewModel.config.value.exchangeRate, 0.001)
        assertEquals("CAD", exchangeRepo.lastBase)
        assertEquals("MXN", exchangeRepo.lastTarget)
    }

    @Test
    fun sameCurrency_setsExchangeRateToOneWithoutFetch() = runTest {
        viewModel.updateConfig { it.copy(baseCurrency = Currency.USD, targetCurrency = Currency.USD) }
        advanceUntilIdle()
        
        assertEquals(1.0, viewModel.config.value.exchangeRate, 0.001)
        // lastBase/lastTarget should still be null if no fetch was triggered
        assertEquals(null, exchangeRepo.lastBase)
    }

    @Test
    fun nonCurrencyChange_keepsExchangeRate() {
        viewModel.updateConfig { it.copy(exchangeRate = 18.5) }
        viewModel.updateConfig { it.copy(inputAmount = 70_000.0) }
        assertEquals(18.5, viewModel.config.value.exchangeRate, 0.001)
    }

    @Test
    fun currencyChange_setsLoadingTrue() = runTest {
        exchangeRepo.pendingResponse = CompletableDeferred()
        viewModel.updateConfig { it.copy(baseCurrency = Currency.CAD, targetCurrency = Currency.MXN) }
        assertTrue(viewModel.isLoadingRate.value)
        exchangeRepo.pendingResponse!!.complete(Unit)
        advanceUntilIdle()
        assertFalse(viewModel.isLoadingRate.value)
    }

    @Test
    fun successfulFetch_clearsLoadingAndError() = runTest {
        exchangeRepo.rateToReturn = 17.5
        viewModel.updateConfig { it.copy(baseCurrency = Currency.CAD, targetCurrency = Currency.MXN) }
        advanceUntilIdle()
        assertFalse(viewModel.isLoadingRate.value)
        assertNull(viewModel.rateError.value)
    }

    @Test
    fun failedFetch_clearsLoadingAndSetsError() = runTest {
        exchangeRepo.shouldFail = true
        viewModel.updateConfig { it.copy(baseCurrency = Currency.CAD, targetCurrency = Currency.MXN) }
        advanceUntilIdle()
        assertFalse(viewModel.isLoadingRate.value)
        assertNotNull(viewModel.rateError.value)
    }

    @Test
    fun newFetch_clearsErrorFromPreviousFetch() = runTest {
        exchangeRepo.shouldFail = true
        viewModel.updateConfig { it.copy(baseCurrency = Currency.CAD, targetCurrency = Currency.MXN) }
        advanceUntilIdle()
        assertNotNull(viewModel.rateError.value)

        exchangeRepo.shouldFail = false
        exchangeRepo.rateToReturn = 1.3
        viewModel.updateConfig { it.copy(baseCurrency = Currency.USD, targetCurrency = Currency.CAD) }
        advanceUntilIdle()
        assertNull(viewModel.rateError.value)
    }

    @Test
    fun cachedRate_appliedImmediatelyBeforeFetchCompletes() = runTest {
        repo.cacheRate("CAD", "MXN", 13.5)
        exchangeRepo.pendingResponse = CompletableDeferred()
        viewModel.updateConfig { it.copy(baseCurrency = Currency.CAD, targetCurrency = Currency.MXN) }
        advanceUntilIdle()
        assertEquals(13.5, viewModel.config.value.exchangeRate, 0.001)
        exchangeRepo.pendingResponse!!.complete(Unit)
        advanceUntilIdle()
    }

    @Test
    fun cachedRate_noLoadingIndicator() = runTest {
        repo.cacheRate("CAD", "MXN", 13.5)
        exchangeRepo.pendingResponse = CompletableDeferred()
        viewModel.updateConfig { it.copy(baseCurrency = Currency.CAD, targetCurrency = Currency.MXN) }
        advanceUntilIdle()
        assertFalse(viewModel.isLoadingRate.value)
        exchangeRepo.pendingResponse!!.complete(Unit)
        advanceUntilIdle()
    }

    @Test
    fun successfulFetch_storesRateInCache() = runTest {
        exchangeRepo.rateToReturn = 17.5
        viewModel.updateConfig { it.copy(baseCurrency = Currency.CAD, targetCurrency = Currency.MXN) }
        advanceUntilIdle()
        assertEquals(17.5, repo.getCachedRate("CAD", "MXN")!!, 0.001)
    }

    @Test
    fun results_calculatedCorrectlyForAllFrequencies() {
        viewModel.updateConfig {
            it.copy(
                inputAmount = 60_000.0,
                inputFrequency = Frequency.YEARLY,
                hoursPerWeek = 40,
                weeksPerYear = 52,
                exchangeRate = 20.0
            )
        }
        val res = viewModel.results.value

        // Yearly
        assertEquals(60000.0, res.yearly.base, 0.01)
        assertEquals(1200000.0, res.yearly.target, 0.01)

        // Monthly
        assertEquals(5000.0, res.monthly.base, 0.01)
        assertEquals(100000.0, res.monthly.target, 0.01)

        // Bi-weekly (60000 / 26 = 2307.69)
        assertEquals(2307.69, res.biWeekly.base, 0.01)

        // Weekly (60000 / 52 = 1153.84)
        assertEquals(1153.84, res.weekly.base, 0.01)

        // Daily (60000 / (52 * 5) = 230.76)
        assertEquals(230.76, res.daily.base, 0.01)

        // Hourly (60000 / 2080 = 28.84)
        assertEquals(28.84, res.hourly.base, 0.01)

        assertEquals(2080, res.annualHours)
    }
}
