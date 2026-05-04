package com.softeen.wagecalculator.fake

import com.softeen.wagecalculator.data.model.SalaryConfig
import com.softeen.wagecalculator.data.repository.SalaryConfigRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeSalaryConfigRepository : SalaryConfigRepository {
    private val _config = MutableStateFlow(SalaryConfig())
    override val config: Flow<SalaryConfig> = _config

    var lastSaved: SalaryConfig? = null
        private set

    fun setInitialConfig(config: SalaryConfig) { _config.value = config }

    override suspend fun save(config: SalaryConfig) {
        lastSaved = config
        _config.value = config
    }
}
