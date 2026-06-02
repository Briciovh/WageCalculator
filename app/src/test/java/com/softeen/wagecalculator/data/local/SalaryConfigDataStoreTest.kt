package com.softeen.wagecalculator.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.softeen.wagecalculator.data.model.Currency
import com.softeen.wagecalculator.data.model.Frequency
import com.softeen.wagecalculator.data.model.SalaryConfig
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class SalaryConfigDataStoreTest {

    @get:Rule
    val tmpFolder = TemporaryFolder()

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher + Job())

    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var repository: SalaryConfigDataStore

    @Before
    fun setup() {
        dataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { File(tmpFolder.newFolder(), "test.preferences_pb") }
        )
        repository = SalaryConfigDataStore(dataStore)
    }

    @Test
    fun saveAndReadConfig() = runTest(testDispatcher) {
        val original = SalaryConfig(
            baseCurrency = Currency.CAD,
            targetCurrency = Currency.BRL,
            inputAmount = 75000.0,
            inputFrequency = Frequency.MONTHLY,
            hoursPerWeek = 35,
            weeksPerYear = 48,
            exchangeRate = 4.5
        )

        repository.save(original)
        val loaded = repository.config.first()

        assertEquals(original, loaded)
    }

    @Test
    fun readDefaultConfigWhenEmpty() = runTest(testDispatcher) {
        val loaded = repository.config.first()
        val default = SalaryConfig()

        assertEquals(default.baseCurrency, loaded.baseCurrency)
        assertEquals(default.inputAmount, loaded.inputAmount, 0.01)
    }

    @Test
    fun handleInvalidEnumStrings_fallsBackToDefaults() = runTest(testDispatcher) {
        dataStore.edit { prefs ->
            prefs[SalaryConfigDataStore.KEY_BASE_CURRENCY]   = "INVALID_CURRENCY"
            prefs[SalaryConfigDataStore.KEY_INPUT_FREQUENCY] = "INVALID_FREQUENCY"
        }
        val loaded = repository.config.first()
        assertEquals(Currency.USD, loaded.baseCurrency)
        assertEquals(Frequency.YEARLY, loaded.inputFrequency)
    }

    @Test
    fun readDefaultConfigWhenEmpty_allFields() = runTest(testDispatcher) {
        val loaded = repository.config.first()
        val default = SalaryConfig()
        assertEquals(default.baseCurrency,   loaded.baseCurrency)
        assertEquals(default.targetCurrency, loaded.targetCurrency)
        assertEquals(default.inputAmount,    loaded.inputAmount,   0.01)
        assertEquals(default.inputFrequency, loaded.inputFrequency)
        assertEquals(default.hoursPerWeek,   loaded.hoursPerWeek)
        assertEquals(default.weeksPerYear,   loaded.weeksPerYear)
        assertEquals(default.exchangeRate,   loaded.exchangeRate,  0.001)
    }

    @Test
    fun getCachedRate_returnsNullWhenMissing() = runTest(testDispatcher) {
        val cached = repository.getCachedRate("USD", "MXN")
        assertEquals(null, cached)
    }

    @Test
    fun cacheRate_storesAndRetrievesValue() = runTest(testDispatcher) {
        repository.cacheRate("USD", "MXN", 17.5)
        assertEquals(17.5, repository.getCachedRate("USD", "MXN")!!, 0.001)
    }

    @Test
    fun cacheRate_differentPairsStoredIndependently() = runTest(testDispatcher) {
        repository.cacheRate("USD", "MXN", 17.5)
        repository.cacheRate("USD", "CAD", 1.35)
        assertEquals(17.5, repository.getCachedRate("USD", "MXN")!!, 0.001)
        assertEquals(1.35, repository.getCachedRate("USD", "CAD")!!, 0.001)
    }
}
