package com.example.bovara.core.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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
}