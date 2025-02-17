package com.example.proyectodivisa

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.work.*
import com.example.proyectodivisa.ui.theme.ProyectoDivisaTheme
import com.example.proyectodivisa.worker.ExchangeRateWorker
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Ejecutar sincronización inmediata y programar la periódica
        executeOneTimeSync()
        schedulePeriodicSync()

        enableEdgeToEdge()
        setContent {
            ProyectoDivisaTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    private fun executeOneTimeSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val oneTimeWork = OneTimeWorkRequestBuilder<ExchangeRateWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(applicationContext)
            .enqueue(oneTimeWork)
            .state.observe(this) { state ->
                Log.d(TAG, "Estado de la sincronización: $state")
            }
    }

    private fun schedulePeriodicSync() {

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val periodicWork = PeriodicWorkRequestBuilder<ExchangeRateWorker>(
            1, TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .setInitialDelay(5, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(applicationContext)
            .enqueueUniquePeriodicWork(
                "exchange_rate_sync",
                ExistingPeriodicWorkPolicy.REPLACE,
                periodicWork
            )
    }
}

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(text = "Tipo de Cambio")
        Text(text = "La sincronización está activa y se ejecuta cada hora")
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    ProyectoDivisaTheme {
        MainScreen()
    }
}