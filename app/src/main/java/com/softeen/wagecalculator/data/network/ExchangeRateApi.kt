package com.softeen.wagecalculator.data.network

import com.softeen.wagecalculator.data.network.model.ExchangeRateDto
import retrofit2.http.GET
import retrofit2.http.Query

interface ExchangeRateApi {
    @GET("v2/rates")
    suspend fun getLatestRates(
        @Query("base") base: String,
        @Query("quotes") target: String
    ): List<ExchangeRateDto>
}
