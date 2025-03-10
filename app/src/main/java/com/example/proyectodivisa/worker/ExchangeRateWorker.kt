package com.example.proyectodivisa.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.proyectodivisa.api.ExchangeRateApi
import com.example.proyectodivisa.database.DatabaseHelper
import com.example.proyectodivisa.models.ExchangeRate
import com.example.proyectodivisa.models.ExchangeRateDetail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ExchangeRateWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://v6.exchangerate-api.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(ExchangeRateApi::class.java)
        val dbHelper = DatabaseHelper(applicationContext)

        try {
            val response = api.getLatestRates()
            if (response.isSuccessful) {
                response.body()?.let { rateResponse ->
                    // Inserta los datos principales de tipo de cambio
                    val exchangeRate = ExchangeRate(
                        rateResponse.time_last_update_unix,
                        rateResponse.time_next_update_unix,
                        rateResponse.base_code
                    )
                    val tipoCambioId = dbHelper.insertExchangeRate(exchangeRate)

                    // Inserta los detalles de la conversión
                    rateResponse.conversion_rates.forEach { (currency, rate) ->
                        val detail = ExchangeRateDetail(
                            tipoCambioId,
                            currency,
                            rate
                        )
                        dbHelper.insertExchangeRateDetail(detail)
                    }
                }
                Result.success()
            } else {
                Result.failure()
            }
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
