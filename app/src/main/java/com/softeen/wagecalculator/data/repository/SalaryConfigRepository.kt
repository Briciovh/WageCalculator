package com.softeen.wagecalculator.data.repository

import com.softeen.wagecalculator.data.model.SalaryConfig
import kotlinx.coroutines.flow.Flow

interface SalaryConfigRepository {
    val config: Flow<SalaryConfig>
    suspend fun save(config: SalaryConfig)
    suspend fun getCachedRate(base: String, quote: String): Double?
    suspend fun cacheRate(base: String, quote: String, rate: Double)
}
