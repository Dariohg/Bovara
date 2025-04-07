package com.example.bovara.core.notification.helper

import com.example.bovara.ganado.data.model.GanadoEntity
import com.example.bovara.ganado.domain.GanadoUseCase
import com.example.bovara.medicamento.data.model.MedicamentoEntity
import com.example.bovara.medicamento.domain.MedicamentoUseCase
import com.example.bovara.pendiente.data.model.PendienteEntity
import com.example.bovara.pendiente.domain.PendienteUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

data class PendienteCompletoConHora(
    val pendiente: PendienteEntity,
    val medicina: MedicamentoEntity?,
    val ganado: GanadoEntity?,
    val horasRestantes: Long // Campo para las horas restantes o pasadas
)

data class PendientesFiltradosPorHora(
    val futuros: List<PendienteCompletoConHora>,
    val pasados: List<PendienteCompletoConHora>,
    val mismoDia: List<PendienteCompletoConHora>
)

class FiltroPendientesPorHoraUseCase(
    private val pendienteUseCase: PendienteUseCase,
    private val medicamentoUseCase: MedicamentoUseCase,
    private val ganadoUseCase: GanadoUseCase
) {

    // Obtener todos los pendientes y asociarlos con su medicamento y ganado
    fun obtenerPendientes(): Flow<List<PendienteCompletoConHora>> {
        return pendienteUseCase.obtenerTodos().map { listaPendientes: List<PendienteEntity> ->
            listaPendientes.map { pendiente: PendienteEntity ->
                val medicamento = medicamentoUseCase
                    .getMedicamentoById(pendiente.idMedicina.toInt())
                    .firstOrNull()

                val ganado = medicamento?.ganadoId?.let { idGanado ->
                    ganadoUseCase.getGanadoById(idGanado).firstOrNull()
                }

                val horasRestantes = calcularHoras(pendiente.fechaProgramada, Calendar.getInstance().time)

                PendienteCompletoConHora(
                    pendiente = pendiente,
                    medicina = medicamento,
                    ganado = ganado,
                    horasRestantes = horasRestantes
                )
            }
        }
    }

    // Filtrar los pendientes por su estado según la hora
    suspend fun filtrarPendientesPorHora(fechaReferencia: Date): PendientesFiltradosPorHora {
        val pendientes: List<PendienteCompletoConHora> = obtenerPendientes().firstOrNull().orEmpty()

        val mismosDia = mutableListOf<PendienteCompletoConHora>()
        val pasados = mutableListOf<PendienteCompletoConHora>()
        val futuros = mutableListOf<PendienteCompletoConHora>()

        pendientes.forEach { pendienteCompleto ->
            val horasRestantes = calcularHoras(pendienteCompleto.pendiente.fechaProgramada, fechaReferencia)

            // Verificamos si horasRestantes es -1, lo cual indica que no es un valor válido
            if (horasRestantes != -1L) {
                val estadoHora = determinarEstadoPorHora(pendienteCompleto, fechaReferencia)

                when (estadoHora) {
                    EstadoPendiente.EN_HORA -> {
                        // Si está en hora, solo lo agregamos a mismosDia
                        mismosDia.add(pendienteCompleto)
                    }
                    EstadoPendiente.PASADO -> {
                        // Si es pasado, lo agregamos a pasados
                        pasados.add(pendienteCompleto)
                    }
                    EstadoPendiente.FUTURO -> {
                        // Si es futuro y no es hora cero, lo agregamos a futuros
                        if (pendienteCompleto.horasRestantes > 0) {
                            futuros.add(pendienteCompleto)
                        }
                    }
                }
            }
        }

        return PendientesFiltradosPorHora(
            futuros = futuros,
            pasados = pasados,
            mismoDia = mismosDia
        )
    }

    // Obtener la fecha del pendiente desde su cadena
    private fun obtenerFechaPendiente(fechaProgramada: String): Date {
        val simpleDateFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH)
        return simpleDateFormat.parse(fechaProgramada) ?: Date() // Devuelve la fecha parseada o la fecha actual por defecto si no se puede parsear
    }

    // Calcular las horas restantes o pasadas entre la fecha del pendiente y la fecha de referencia
    private fun calcularHoras(fechaPendiente: Date, fechaReferencia: Date): Long {
        val diferenciaMillis = fechaPendiente.time - fechaReferencia.time
        val horasRestantes = TimeUnit.MILLISECONDS.toHours(diferenciaMillis)
        val minutosRestantes = TimeUnit.MILLISECONDS.toMinutes(diferenciaMillis) % 60
        val segundosRestantes = TimeUnit.MILLISECONDS.toSeconds(diferenciaMillis) % 60

        // Verificar que las horas sean exactamente 5, 3, 1 o 0 y que no haya minutos o segundos adicionales
        return when {
            horasRestantes == 5L && minutosRestantes in -1..1 && segundosRestantes == 0L -> horasRestantes
            horasRestantes == 3L && minutosRestantes in -1..1 && segundosRestantes == 0L -> horasRestantes
            horasRestantes == 1L && minutosRestantes in -1..1 && segundosRestantes == 0L -> horasRestantes
            horasRestantes == 0L && minutosRestantes in -1..1 && segundosRestantes == 0L -> horasRestantes
            else -> -1 // Retorna -1 si no coincide exactamente con las horas
        }
    }

    // Determinar el estado del pendiente según la hora
    private fun determinarEstadoPorHora(
        pendiente: PendienteCompletoConHora,
        fechaReferencia: Date
    ): EstadoPendiente {
        val calendarioReferencia = Calendar.getInstance().apply { time = fechaReferencia }
        val calendarioPendiente = Calendar.getInstance().apply {
            time = obtenerFechaPendiente(pendiente.pendiente.fechaProgramada.toString())
        }

        // Compara si la fecha de la notificación es el mismo día que la fecha de referencia
        val esMismoDia = esMismoDia(pendiente.pendiente.fechaProgramada, calendarioReferencia)

        return when {
            esMismoDia -> {
                // Verificar si la hora está en el intervalo de horas exactas (5:00, 3:00, 1:00, 0:00)
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

    // Verificar si el pendiente es el mismo día que la fecha de referencia
    private fun esMismoDia(fecha: Date, calendarioReferencia: Calendar): Boolean {
        val calendarioFecha = Calendar.getInstance().apply { time = fecha }
        return calendarioFecha.get(Calendar.YEAR) == calendarioReferencia.get(Calendar.YEAR) &&
                calendarioFecha.get(Calendar.DAY_OF_YEAR) == calendarioReferencia.get(Calendar.DAY_OF_YEAR)
    }

    // Filtrar por horas dentro de un margen de ±1 minuto para horas válidas
    private fun filtrarPorHoras(
        pendiente: PendienteCompletoConHora,
        fechaReferencia: Date
    ): Boolean {
        val horasValidas = listOf(5, 3, 1, 0) // Horas válidas para el mismo día: 5:00, 3:00, 1:00, 0:00

        val calendarioReferencia = Calendar.getInstance().apply { time = fechaReferencia }
        val calendarioPendiente = Calendar.getInstance().apply {
            time = obtenerFechaPendiente(pendiente.pendiente.fechaProgramada.toString())
        }

        // Verificar si la fecha de programación es el mismo día que la referencia
        if (esMismoDia(pendiente.pendiente.fechaProgramada, calendarioReferencia)) {
            val horaPendiente: Int = calendarioPendiente.get(Calendar.HOUR_OF_DAY) // Asegurar tipo Int
            val minutoPendiente: Int = calendarioPendiente.get(Calendar.MINUTE) // Asegurar tipo Int
            val segundoPendiente: Int = calendarioPendiente.get(Calendar.SECOND) // Asegurar tipo Int

            // Comprobar si la hora está dentro de las horas válidas del mismo día (5:00, 3:00, 1:00, 0:00)
            if (horaPendiente in horasValidas && minutoPendiente in -1..1 && segundoPendiente == 0) {
                return true
            }
        }

        return false // Si no coincide, no devolver nada
    }
}

// Enumeración para los estados de los pendientes
enum class EstadoPendiente {
    EN_HORA, FUTURO, PASADO
}

