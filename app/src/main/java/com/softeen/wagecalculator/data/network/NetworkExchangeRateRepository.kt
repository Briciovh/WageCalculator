package com.softeen.wagecalculator.data.network

import com.softeen.wagecalculator.data.repository.ExchangeRateRepository
import kotlinx.coroutines.CancellationException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkExchangeRateRepository @Inject constructor(
    private val api: ExchangeRateApi
) : ExchangeRateRepository {
    override suspend fun getExchangeRate(base: String, target: String): Result<Double> {
        return try {
            val response = api.getLatestRates(base, target)
            val entry = response.firstOrNull { it.quote == target }
            if (entry != null) {
                Result.success(entry.rate)
            } else {
                Result.failure(Exception("Rate for $target not found in response"))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
