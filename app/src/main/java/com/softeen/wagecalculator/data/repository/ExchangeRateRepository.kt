package com.softeen.wagecalculator.data.repository

interface ExchangeRateRepository {
    suspend fun getExchangeRate(base: String, target: String): Result<Double>
}
