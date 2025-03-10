package com.example.proyectodivisa.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.proyectodivisa.models.ExchangeRate
import com.example.proyectodivisa.models.ExchangeRateDetail

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    private val TAG = "DatabaseHelper"

    companion object {
        private const val DATABASE_NAME = "exchange_rates.db"
        private const val DATABASE_VERSION = 1

        private const val TABLE_TIPO_CAMBIO = "TipoCambio"
        private const val TABLE_TIPO_CAMBIO_DETALLE = "TipoCambioDetalle"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTipoCambioTable = """
            CREATE TABLE $TABLE_TIPO_CAMBIO (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                time_last_update INTEGER NOT NULL,
                time_next_update INTEGER NOT NULL,
                base_code TEXT NOT NULL
            )
        """.trimIndent()

        val createTipoCambioDetalleTable = """
            CREATE TABLE $TABLE_TIPO_CAMBIO_DETALLE (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                idTipoCambio INTEGER NOT NULL,
                codigoDeMoneda TEXT NOT NULL,
                valor REAL NOT NULL,
                FOREIGN KEY (idTipoCambio) REFERENCES $TABLE_TIPO_CAMBIO(id)
            )
        """.trimIndent()

        db.execSQL(createTipoCambioTable)
        db.execSQL(createTipoCambioDetalleTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Log.d(TAG, "Actualizando base de datos de versión $oldVersion a $newVersion")
        onCreate(db)
    }

    fun getLastUpdate(): Long? {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_TIPO_CAMBIO,
            arrayOf("time_last_update"),
            null,
            null,
            null,
            null,
            "time_last_update DESC",
            "1"
        )

        return if (cursor.moveToFirst()) {
            cursor.getLong(0)
        } else {
            null
        }.also {
            cursor.close()
        }
    }

    fun insertExchangeRate(rate: ExchangeRate): Long {
        val lastUpdate = getLastUpdate()

        // Si ya existe una actualización reciente (menos de 1 hora), no insertar
        if (lastUpdate != null && (rate.time_last_update - lastUpdate) < 3600) {
            Log.d(TAG, "Actualización reciente encontrada, saltando inserción")
            return -1
        }

        Log.d(TAG, "Insertando nuevo tipo de cambio: ${rate.base_code}")
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("time_last_update", rate.time_last_update)
            put("time_next_update", rate.time_next_update)
            put("base_code", rate.base_code)
        }
        val id = db.insert(TABLE_TIPO_CAMBIO, null, values)
        Log.d(TAG, "Tipo de cambio insertado con ID: $id")
        return id
    }

    fun insertExchangeRateDetail(detail: ExchangeRateDetail) {
        // Solo insertar si el idTipoCambio es válido
        if (detail.idTipoCambio == -1L) {
            Log.d(TAG, "Saltando inserción de detalle por ID inválido")
            return
        }

        Log.d(TAG, "Insertando detalle para moneda: ${detail.codigoDeMoneda}")
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("idTipoCambio", detail.idTipoCambio)
            put("codigoDeMoneda", detail.codigoDeMoneda)
            put("valor", detail.valor)
        }
        val id = db.insert(TABLE_TIPO_CAMBIO_DETALLE, null, values)
        Log.d(TAG, "Detalle insertado con ID: $id")
    }

}