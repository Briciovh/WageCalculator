package com.softeen.wagecalculator.fake

import com.softeen.wagecalculator.data.repository.ExchangeRateRepository
import kotlinx.coroutines.CompletableDeferred

class FakeExchangeRateRepository : ExchangeRateRepository {
    var rateToReturn: Double = 1.0
    var shouldFail: Boolean = false
    var lastBase: String? = null
    var lastTarget: String? = null
    var pendingResponse: CompletableDeferred<Unit>? = null

    override suspend fun getExchangeRate(base: String, target: String): Result<Double> {
        lastBase = base
        lastTarget = target
        pendingResponse?.await()
        return if (shouldFail) {
            Result.failure(Exception("Fake failure"))
        } else {
            Result.success(rateToReturn)
        }
    }
}
