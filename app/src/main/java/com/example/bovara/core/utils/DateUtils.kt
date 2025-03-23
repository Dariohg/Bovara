package com.example.bovara.core.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Utilidades para el manejo de fechas en la aplicación
 */
object DateUtils {

    /**
     * Formatea una fecha en formato dd/MM/yyyy
     */
    fun formatDate(date: Date?): String {
        if (date == null) return "No definida"

        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return formatter.format(date)
    }

    /**
     * Formatea una fecha y hora en formato dd/MM/yyyy HH:mm
     */
    fun formatDateTime(date: Date?): String {
        if (date == null) return "No definida"

        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return formatter.format(date)
    }

    /**
     * Analiza una cadena de texto para convertirla en objeto Date
     */
    fun parseDate(dateString: String): Date? {
        return try {
            val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            formatter.parse(dateString)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Obtiene la fecha actual
     */
    fun getCurrentDate(): Date {
        return Calendar.getInstance().time
    }

    /**
     * Calcula la edad a partir de una fecha de nacimiento
     */
    fun calculateAge(birthDate: Date?): String {
        if (birthDate == null) return "Desconocida"

        val calendar1 = Calendar.getInstance()
        val calendar2 = Calendar.getInstance()
        calendar1.time = birthDate
        calendar2.time = Date()

        var years = calendar2.get(Calendar.YEAR) - calendar1.get(Calendar.YEAR)
        var months = calendar2.get(Calendar.MONTH) - calendar1.get(Calendar.MONTH)

        if (calendar2.get(Calendar.DAY_OF_MONTH) < calendar1.get(Calendar.DAY_OF_MONTH)) {
            months--
        }

        if (months < 0) {
            years--
            months += 12
        }

        return when {
            years > 0 -> "$years ${if (years == 1) "año" else "años"}"
            months > 0 -> "$months ${if (months == 1) "mes" else "meses"}"
            else -> "Menos de 1 mes"
        }
    }

    fun normalizeDateToLocalMidnight(date: Date, isUserSelection: Boolean = true): Date {
        val calendar = Calendar.getInstance(TimeZone.getDefault())
        calendar.time = date

        if (isUserSelection) {
            // Para selecciones de usuario, añadir un día para compensar
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        } else {
            // Para inicializaciones, restar un día adicional
            calendar.add(Calendar.DAY_OF_MONTH, -1)
        }

        // Configurar hora a mediodía
        calendar.set(Calendar.HOUR_OF_DAY, 12)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        return calendar.time
    }

    fun fixDatePickerDate(milliseconds: Long?): Date? {
        if (milliseconds == null) return null

        // Crear la fecha a partir de los milisegundos
        val date = Date(milliseconds)

        // Aplicar la normalización que corrige el problema
        return normalizeDateToLocalMidnight(date)
    }
}