package com.example.proyectodivisa.worker


import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.proyectodivisa.api.ExchangeRateApi
import com.example.proyectodivisa.database.DatabaseHelper
import com.example.proyectodivisa.models.ExchangeRate
import com.example.proyectodivisa.models.ExchangeRateDetail
import kotlinx.coroutines.runBlocking
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ExchangeRateWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://v6.exchangerate-api.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(ExchangeRateApi::class.java)
        val dbHelper = DatabaseHelper(applicationContext)

        runBlocking {
            try {
                val response = api.getLatestRates()
                if (response.isSuccessful) {
                    response.body()?.let { rateResponse ->
                        // Insert main exchange rate data
                        val exchangeRate = ExchangeRate(
                            rateResponse.time_last_update_unix,
                            rateResponse.time_next_update_unix,
                            rateResponse.base_code
                        )
                        val tipoCambioId = dbHelper.insertExchangeRate(exchangeRate)

                        // Insert exchange rate details
                        rateResponse.conversion_rates.forEach { (currency, rate) ->
                            val detail = ExchangeRateDetail(
                                tipoCambioId,
                                currency,
                                rate
                            )
                            dbHelper.insertExchangeRateDetail(detail)
                        }
                    }
                    return@runBlocking Result.success()
                }
            } catch (e: Exception) {
                return@runBlocking Result.failure()
            }
        }
        return Result.success()
    }
}