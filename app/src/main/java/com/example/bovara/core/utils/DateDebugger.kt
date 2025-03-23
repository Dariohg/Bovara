package com.example.bovara.core.utils

import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Utilidad para depurar problemas con fechas
 */
object DateDebugger {
    private const val TAG = "DateDebugger"

    /**
     * Muestra información detallada sobre una fecha para ayudar a identificar problemas
     */
    fun debugDate(label: String, date: Date?) {
        if (date == null) {
            Log.d(TAG, "$label: NULL")
            return
        }

        // Formato en diferentes zonas horarias
        val defaultFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
        val utcFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
        utcFormatter.timeZone = TimeZone.getTimeZone("UTC")

        // Detalles de la fecha
        val defaultFormat = defaultFormatter.format(date)
        val utcFormat = utcFormatter.format(date)
        val timeInMillis = date.time

        // Obtener componentes individuales (para verificación)
        val calendar = java.util.Calendar.getInstance()
        calendar.time = date
        val year = calendar.get(java.util.Calendar.YEAR)
        val month = calendar.get(java.util.Calendar.MONTH) + 1  // Calendar months are 0-based
        val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)
        val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
        val minute = calendar.get(java.util.Calendar.MINUTE)

        // Registrar toda la información
        Log.d(TAG, "─────────────────────────────────────────")
        Log.d(TAG, "📅 DEBUG FECHA: $label")
        Log.d(TAG, "• Local: $defaultFormat")
        Log.d(TAG, "• UTC: $utcFormat")
        Log.d(TAG, "• Milisegundos: $timeInMillis")
        Log.d(TAG, "• Componentes: $year-$month-$day $hour:$minute")
        Log.d(TAG, "• TimeZone local: ${TimeZone.getDefault().id}")
        Log.d(TAG, "• TimeZone offset: ${TimeZone.getDefault().rawOffset / 3600000}h")
        Log.d(TAG, "─────────────────────────────────────────")
    }
}