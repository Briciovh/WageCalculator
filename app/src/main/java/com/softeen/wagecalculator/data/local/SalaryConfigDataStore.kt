package com.softeen.wagecalculator.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.softeen.wagecalculator.data.model.Currency
import com.softeen.wagecalculator.data.model.Frequency
import com.softeen.wagecalculator.data.model.SalaryConfig
import com.softeen.wagecalculator.data.repository.SalaryConfigRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SalaryConfigDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : SalaryConfigRepository {

    companion object {
        val KEY_BASE_CURRENCY   = stringPreferencesKey("base_currency")
        val KEY_TARGET_CURRENCY = stringPreferencesKey("target_currency")
        val KEY_INPUT_AMOUNT    = doublePreferencesKey("input_amount")
        val KEY_INPUT_FREQUENCY = stringPreferencesKey("input_frequency")
        val KEY_HOURS_PER_WEEK  = intPreferencesKey("hours_per_week")
        val KEY_WEEKS_PER_YEAR  = intPreferencesKey("weeks_per_year")
        val KEY_EXCHANGE_RATE   = doublePreferencesKey("exchange_rate")
    }

    override val config: Flow<SalaryConfig> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { prefs ->
            SalaryConfig(
                baseCurrency   = Currency.entries.find { it.name == prefs[KEY_BASE_CURRENCY] }   ?: Currency.USD,
                targetCurrency = Currency.entries.find { it.name == prefs[KEY_TARGET_CURRENCY] } ?: Currency.MXN,
                inputAmount    = prefs[KEY_INPUT_AMOUNT]    ?: 60000.0,
                inputFrequency = Frequency.entries.find { it.name == prefs[KEY_INPUT_FREQUENCY] } ?: Frequency.YEARLY,
                hoursPerWeek   = prefs[KEY_HOURS_PER_WEEK]  ?: 40,
                weeksPerYear   = prefs[KEY_WEEKS_PER_YEAR]  ?: 52,
                exchangeRate   = prefs[KEY_EXCHANGE_RATE]   ?: 1.0
            )
        }

    override suspend fun save(config: SalaryConfig) {
        dataStore.edit { prefs ->
            prefs[KEY_BASE_CURRENCY]   = config.baseCurrency.name
            prefs[KEY_TARGET_CURRENCY] = config.targetCurrency.name
            prefs[KEY_INPUT_AMOUNT]    = config.inputAmount
            prefs[KEY_INPUT_FREQUENCY] = config.inputFrequency.name
            prefs[KEY_HOURS_PER_WEEK]  = config.hoursPerWeek
            prefs[KEY_WEEKS_PER_YEAR]  = config.weeksPerYear
            prefs[KEY_EXCHANGE_RATE]   = config.exchangeRate
        }
    }

    override suspend fun getCachedRate(base: String, quote: String): Double? {
        val key = doublePreferencesKey("rate_${base}_${quote}")
        return dataStore.data.first()[key]
    }

    override suspend fun cacheRate(base: String, quote: String, rate: Double) {
        val key = doublePreferencesKey("rate_${base}_${quote}")
        dataStore.edit { prefs -> prefs[key] = rate }
    }
}
