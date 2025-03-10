package com.example.proyectodivisa

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import com.example.proyectodivisa.database.DatabaseHelper

class ExchangeRateContentProvider : ContentProvider() {

    companion object {
        // Define la autoridad y la ruta para los detalles
        const val AUTHORITY = "com.example.proyectodivisa.provider"
        const val PATH_DETAILS = "exchange_rate_details"
        val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY/$PATH_DETAILS")

        // Códigos para el UriMatcher
        private const val DETAILS = 1
        private const val DETAIL_ID = 2
    }

    // Configura el UriMatcher para distinguir entre consultas generales y por ID
    private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
        addURI(AUTHORITY, PATH_DETAILS, DETAILS)
        addURI(AUTHORITY, "$PATH_DETAILS/#", DETAIL_ID)
    }

    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(): Boolean {
        context?.let {
            dbHelper = DatabaseHelper(it)
            return true
        } ?: return false
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        val db = dbHelper.readableDatabase
        return when (uriMatcher.match(uri)) {
            DETAILS -> {
                // Consulta todos los registros de la tabla de detalles
                db.query("TipoCambioDetalle", projection, selection, selectionArgs, null, null, sortOrder)
            }
            DETAIL_ID -> {
                // Consulta un registro en particular (por ejemplo, por id)
                val id = uri.lastPathSegment
                db.query("TipoCambioDetalle", projection, "id = ?", arrayOf(id), null, null, sortOrder)
            }
            else -> throw IllegalArgumentException("URI no soportada: $uri")
        }
    }

    override fun getType(uri: Uri): String? {
        return when (uriMatcher.match(uri)) {
            DETAILS -> "vnd.android.cursor.dir/$AUTHORITY.$PATH_DETAILS"
            DETAIL_ID -> "vnd.android.cursor.item/$AUTHORITY.$PATH_DETAILS"
            else -> throw IllegalArgumentException("URI no soportada: $uri")
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        throw UnsupportedOperationException("Insert no soportado")
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        throw UnsupportedOperationException("Delete no soportado")
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int {
        throw UnsupportedOperationException("Update no soportado")
    }
}
