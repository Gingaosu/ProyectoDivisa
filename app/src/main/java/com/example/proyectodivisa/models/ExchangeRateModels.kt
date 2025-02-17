package com.example.proyectodivisa.models

data class ExchangeRate(
    val time_last_update: Long,
    val time_next_update: Long,
    val base_code: String
)

data class ExchangeRateDetail(
    val idTipoCambio: Long,
    val codigoDeMoneda: String,
    val valor: Double
)

data class ExchangeRateResponse(
    val time_last_update_unix: Long,
    val time_next_update_unix: Long,
    val base_code: String,
    val conversion_rates: Map<String, Double>
)