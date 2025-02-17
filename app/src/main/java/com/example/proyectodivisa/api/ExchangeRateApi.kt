package com.example.proyectodivisa.api

import com.example.proyectodivisa.models.ExchangeRateResponse
import retrofit2.Response
import retrofit2.http.GET

interface ExchangeRateApi {
    @GET("v6/c05011d4ff8ab7037ac5946b/latest/USD")
    suspend fun getLatestRates(): Response<ExchangeRateResponse>
}