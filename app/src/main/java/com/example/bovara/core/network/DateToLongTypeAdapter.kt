package com.example.bovara.core.network

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.io.IOException

class DateToLongTypeAdapter : TypeAdapter<Long>() {
    @Throws(IOException::class)
    override fun write(out: JsonWriter?, value: Long?) {
        out?.value(value)
    }

    @Throws(IOException::class)
    override fun read(`in`: JsonReader?): Long {
        val dateString = `in`?.nextString()
        return dateString?.let {
            try {
                // Si el valor es un timestamp con decimales, lo manejamos dividiendo entre 1000
                val timestamp = it.toDouble()
                (timestamp.toLong()) // Convertir el timestamp a Long sin decimales
            } catch (e: NumberFormatException) {
                0L // Si el formato no es correcto, devolver 0 como fallback
            }
        } ?: 0L
    }
}
