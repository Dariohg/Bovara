package com.example.bovara.core.notification.helper

import com.example.bovara.ganado.domain.GanadoUseCase
import com.example.bovara.medicamento.domain.MedicamentoUseCase
import com.example.bovara.pendiente.domain.PendienteUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class PendientesFiltradosPorHora(
    val futuros: List<PendienteCompleto>,
    val pasados: List<PendienteCompleto>,
    val mismoDia: List<PendienteCompleto>
)

class FiltroPendientesPorHoraUseCase(
    private val pendienteUseCase: PendienteUseCase,
    private val medicamentoUseCase: MedicamentoUseCase,
    private val ganadoUseCase: GanadoUseCase
) {

    fun obtenerPendientes(): Flow<List<PendienteCompleto>> {
        return pendienteUseCase.obtenerTodos().map { listaPendientes ->
            listaPendientes.map { pendiente ->
                val medicamento = medicamentoUseCase
                    .getMedicamentoById(pendiente.idMedicina.toInt())
                    .firstOrNull()

                val ganado = medicamento?.ganadoId?.let { idGanado ->
                    ganadoUseCase.getGanadoById(idGanado).firstOrNull()
                }

                PendienteCompleto(
                    pendiente = pendiente,
                    medicina = medicamento,
                    ganado = ganado
                )
            }
        }
    }

    suspend fun filtrarPendientesPorHora(fechaReferencia: Date): PendientesFiltradosPorHora {
        val pendientes = obtenerPendientes().firstOrNull().orEmpty()
        val calendarioReferencia = Calendar.getInstance().apply { time = fechaReferencia }

        val mismosDia = mutableListOf<PendienteCompleto>()
        val pasados = mutableListOf<PendienteCompleto>()
        val futuros = mutableListOf<PendienteCompleto>()

        pendientes.forEach { pendienteCompleto ->
            val fechaPendiente = obtenerFechaPendiente(pendienteCompleto.pendiente.fechaProgramada.toString())
            val estadoHora = determinarEstadoPorHora(pendienteCompleto, fechaReferencia)

            when (estadoHora) {
                EstadoPendiente.EN_HORA -> mismosDia.add(pendienteCompleto)
                EstadoPendiente.PASADO -> pasados.add(pendienteCompleto)
                EstadoPendiente.FUTURO -> futuros.add(pendienteCompleto)
            }
        }

        return PendientesFiltradosPorHora(
            futuros = futuros,
            pasados = pasados,
            mismoDia = mismosDia
        )
    }

    private fun obtenerFechaPendiente(fechaProgramada: String): Date {
        val simpleDateFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH)
        return simpleDateFormat.parse(fechaProgramada) ?: Date() // Devuelve la fecha parseada o la fecha actual por defecto si no se puede parsear
    }

    private fun determinarEstadoPorHora(
        pendiente: PendienteCompleto,
        fechaReferencia: Date
    ): EstadoPendiente {
        val calendarioReferencia = Calendar.getInstance().apply { time = fechaReferencia }
        val calendarioPendiente = Calendar.getInstance().apply {
            time = obtenerFechaPendiente(pendiente.pendiente.fechaProgramada.toString())
        }

        val horaPendiente = calendarioPendiente.get(Calendar.HOUR_OF_DAY)
        val minutoPendiente = calendarioPendiente.get(Calendar.MINUTE)

        return when {
            esMismoDia(pendiente.pendiente.fechaProgramada, calendarioReferencia) -> {
                if (filtrarPorHoras(pendiente, fechaReferencia)) {
                    EstadoPendiente.EN_HORA
                } else if (calendarioPendiente.before(calendarioReferencia)) {
                    EstadoPendiente.PASADO
                } else {
                    EstadoPendiente.FUTURO
                }
            }
            calendarioPendiente.after(calendarioReferencia) -> EstadoPendiente.FUTURO
            else -> EstadoPendiente.PASADO
        }
    }

    private fun esMismoDia(fecha: Date, calendarioReferencia: Calendar): Boolean {
        val calendarioFecha = Calendar.getInstance().apply { time = fecha }
        return calendarioFecha.get(Calendar.YEAR) == calendarioReferencia.get(Calendar.YEAR) &&
                calendarioFecha.get(Calendar.DAY_OF_YEAR) == calendarioReferencia.get(Calendar.DAY_OF_YEAR)
    }

    private fun filtrarPorHoras(
        pendiente: PendienteCompleto,
        fechaReferencia: Date
    ): Boolean {
        val horasValidas = listOf(5, 3, 1) // 5:00, 3:00, 1:00
        val sesgoMinutos = 3 // Sesgo de 3 minutos

        val calendarioReferencia = Calendar.getInstance().apply { time = fechaReferencia }
        val calendarioPendiente = Calendar.getInstance().apply {
            time = obtenerFechaPendiente(pendiente.pendiente.fechaProgramada.toString())
        }

        // Verificar si la fecha de programación es el mismo día que la referencia
        if (esMismoDia(pendiente.pendiente.fechaProgramada, calendarioReferencia)) {
            val horaPendiente = calendarioPendiente.get(Calendar.HOUR_OF_DAY)
            val minutoPendiente = calendarioPendiente.get(Calendar.MINUTE)

            // Comprobar si la hora está dentro de las horas válidas con el sesgo
            horasValidas.forEach { horaValida ->
                if (horaPendiente == horaValida) {
                    val rangoInicio = horaValida * 60
                    val rangoFin = rangoInicio + sesgoMinutos // Se considera 3 minutos de margen

                    val minutoTotal = horaPendiente * 60 + minutoPendiente
                    if (minutoTotal in rangoInicio..rangoFin) {
                        return true
                    }
                }
            }
        }

        return false
    }
}

enum class EstadoPendiente {
    EN_HORA, FUTURO, PASADO
}
