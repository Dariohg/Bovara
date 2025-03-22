package com.example.bovara.core.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateUtils {

    fun formatDate(date: Date?): String {
        if (date == null) return "No definida"

        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return formatter.format(date)
    }

    fun formatDateTime(date: Date?): String {
        if (date == null) return "No definida"

        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return formatter.format(date)
    }

    fun parseDate(dateString: String): Date? {
        return try {
            val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            formatter.parse(dateString)
        } catch (e: Exception) {
            null
        }
    }

    fun getCurrentDate(): Date {
        return Calendar.getInstance().time
    }

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

    /**
     * Corrige el problema de zona horaria al seleccionar una fecha del DatePicker
     * Asegura que la fecha mantenga el día seleccionado independientemente de la zona horaria
     */
    fun normalizeDateToLocalMidnight(date: Date): Date {
        // En vez de establecer la hora a mediodía, mantenemos la fecha tal cual está en el calendario
        val calendar = Calendar.getInstance()
        calendar.time = date

        // Obtener los componentes de la fecha sin modificar
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // Crear un nuevo calendario y establecer la fecha con la hora a mediodía
        // Esto evita problemas con los cambios de zona horaria
        val newCalendar = Calendar.getInstance()
        newCalendar.set(year, month, day, 12, 0, 0)
        newCalendar.set(Calendar.MILLISECOND, 0)

        return newCalendar.time
    }

    fun fixDatePickerDate(milliseconds: Long?): Date? {
        if (milliseconds == null) return null

        // Crear la fecha a partir de los milisegundos
        val date = Date(milliseconds)

        // Obtener los componentes de la fecha
        val calendar = Calendar.getInstance()
        calendar.time = date

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // Crear una nueva fecha con la hora fijada a mediodía para evitar problemas de zona horaria
        val fixedCalendar = Calendar.getInstance()
        fixedCalendar.set(year, month, day, 12, 0, 0)
        fixedCalendar.set(Calendar.MILLISECOND, 0)

        // Esta es la fecha correcta que el usuario seleccionó
        return fixedCalendar.time
    }
}