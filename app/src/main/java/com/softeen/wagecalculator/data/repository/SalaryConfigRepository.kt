package com.softeen.wagecalculator.data.repository

import com.softeen.wagecalculator.data.model.SalaryConfig
import kotlinx.coroutines.flow.Flow

interface SalaryConfigRepository {
    val config: Flow<SalaryConfig>
    suspend fun save(config: SalaryConfig)
}
