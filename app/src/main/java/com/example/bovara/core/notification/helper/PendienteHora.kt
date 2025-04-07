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
import android.util.Log

data class PendienteCompletoConHora(
    val pendiente: PendienteEntity,
    val medicina: MedicamentoEntity?,
    val ganado: GanadoEntity?,
    val horasRestantes: Long
)

data class PendientesFiltradosPorHora(
    val futuros: MutableList<PendienteCompletoConHora> = mutableListOf(),
    val pasados: MutableList<PendienteCompletoConHora> = mutableListOf(),
    val mismoDia: MutableList<PendienteCompletoConHora> = mutableListOf()
)

class FiltroPendientesPorHoraUseCase(
    private val pendienteUseCase: PendienteUseCase,
    private val medicamentoUseCase: MedicamentoUseCase,
    private val ganadoUseCase: GanadoUseCase
) {

    fun obtenerPendientes(): Flow<List<PendienteCompletoConHora>> {
        return pendienteUseCase.obtenerTodos().map { listaPendientes ->
            listaPendientes.map { pendiente ->
                val medicamento = medicamentoUseCase
                    .getMedicamentoById(pendiente.idMedicina.toInt())
                    .firstOrNull()

                val ganado = medicamento?.ganadoId?.let { idGanado ->
                    ganadoUseCase.getGanadoById(idGanado).firstOrNull()
                }
                val fecha = convertirFechaProgramada(pendiente.fechaProgramada)
                val hora = extraerHoraDesdeString(pendiente.hora) // viene como String

                val horasRestantes = calcularHoras(
                    fecha,
                    Calendar.getInstance().time,
                    hora
                )

                PendienteCompletoConHora(
                    pendiente = pendiente,
                    medicina = medicamento,
                    ganado = ganado,
                    horasRestantes = horasRestantes
                )
            }
        }
    }

    suspend fun filtrarPendientesPorHora(fechaReferencia: Date): PendientesFiltradosPorHora {
        val pendientes = obtenerPendientes().firstOrNull().orEmpty()

        val mismosDia = mutableListOf<PendienteCompletoConHora>()
        val pasados = mutableListOf<PendienteCompletoConHora>()
        val futuros = mutableListOf<PendienteCompletoConHora>()

        pendientes.forEach { pendienteCompleto ->
            val fecha = convertirFechaProgramada(pendienteCompleto.pendiente.fechaProgramada)
            val hora = extraerHoraDesdeString(pendienteCompleto.pendiente.hora)

            val horasRestantes = calcularHoras(
                fecha,
                fechaReferencia,
                hora
            )

            if (horasRestantes != -1L) {
                val estadoHora = determinarEstadoPorHora(pendienteCompleto, fechaReferencia)

                when (estadoHora) {
                    EstadoPendiente.EN_HORA -> mismosDia.add(pendienteCompleto)
                    EstadoPendiente.PASADO -> pasados.add(pendienteCompleto)
                    EstadoPendiente.FUTURO -> {
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

    private fun extraerHoraDesdeString(horaStr: String): Int {
        return try {
            val sdf = SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH)
            val date = sdf.parse(horaStr)
            val cal = Calendar.getInstance()
            cal.time = date!!
            cal.get(Calendar.HOUR_OF_DAY)
        } catch (e: Exception) {
            Log.e("FiltroPendientesPorHora", "No se pudo parsear hora: $horaStr", e)
            -1
        }
    }

    private fun calcularHoras(fechaPendiente: Date, fechaReferencia: Date, horaPendiente: Int): Long {
        if (horaPendiente == -1) return -1L

        val calRef = Calendar.getInstance().apply { time = fechaReferencia }
        val horaActual = calRef.get(Calendar.HOUR_OF_DAY)

        val diferenciaHoras = horaPendiente - horaActual

        return when (Math.abs(diferenciaHoras)) {
            0, 1, 3, 5 -> diferenciaHoras.toLong()
            else -> -1L
        }
    }

    private fun determinarEstadoPorHora(
        pendiente: PendienteCompletoConHora,
        fechaReferencia: Date
    ): EstadoPendiente {
        val fechaPendiente = convertirFechaProgramada(pendiente.pendiente.fechaProgramada)

        val horaPendiente = extraerHoraDesdeString(pendiente.pendiente.hora)

        if (horaPendiente == -1) return EstadoPendiente.PASADO

        val esMismoDia = esMismoDia(fechaPendiente, Calendar.getInstance().apply { time = fechaReferencia })

        val calRef = Calendar.getInstance().apply { time = fechaReferencia }
        val horaActual = calRef.get(Calendar.HOUR_OF_DAY)
        val minutoActual = calRef.get(Calendar.MINUTE)

        val diferenciaHoras = Math.abs(horaPendiente - horaActual)
        val esHoraValida = (diferenciaHoras == 0 || diferenciaHoras == 1 ||
                diferenciaHoras == 3 || diferenciaHoras == 5) &&
                minutoActual <= 1

        return when {
            esMismoDia && esHoraValida -> EstadoPendiente.EN_HORA
            fechaPendiente.after(fechaReferencia) || (esMismoDia && horaPendiente > horaActual) -> EstadoPendiente.FUTURO
            else -> EstadoPendiente.PASADO
        }
    }

    private fun esMismoDia(fecha: Date, calendarioReferencia: Calendar): Boolean {
        val calendarioFecha = Calendar.getInstance().apply { time = fecha }
        return calendarioFecha.get(Calendar.YEAR) == calendarioReferencia.get(Calendar.YEAR) &&
                calendarioFecha.get(Calendar.DAY_OF_YEAR) == calendarioReferencia.get(Calendar.DAY_OF_YEAR)
    }

    private fun convertirFechaProgramada(fechaProgramada: Any): Date {
        return when (fechaProgramada) {
            is Long -> Date(fechaProgramada)
            is String -> {
                try {
                    val formato = SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH)
                    formato.parse(fechaProgramada) ?: Date()
                } catch (e: Exception) {
                    Log.e("FiltroPendientesPorHora", "Error al parsear fecha: $fechaProgramada", e)
                    Date()
                }
            }
            else -> {
                Log.e("FiltroPendientesPorHora", "Formato de fecha desconocido: $fechaProgramada")
                Date()
            }
        }
    }

}

enum class EstadoPendiente {
    EN_HORA, FUTURO, PASADO
}
