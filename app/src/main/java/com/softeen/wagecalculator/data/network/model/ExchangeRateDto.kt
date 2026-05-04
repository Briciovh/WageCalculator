package com.softeen.wagecalculator.data.network.model

import kotlinx.serialization.Serializable

@Serializable
data class ExchangeRateDto(
    val date: String,
    val base: String,
    val quote: String,
    val rate: Double
)
