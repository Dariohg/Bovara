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
import java.io.Serializable
import kotlin.math.roundToLong

data class PendienteCompletoConHora(
    val pendiente: PendienteEntity,
    val medicina: MedicamentoEntity?,
    val ganado: GanadoEntity?,
    val horasRestantes: Long
): Serializable

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



    suspend fun obtenerPendientes(): Flow<List<PendienteCompletoConHora>> {
        return pendienteUseCase.obtenerPendientesDelDiaConHorasRelativas().map { listaPendientes ->
            val ahora = Date() // Hora actual
            val dateFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH)

            listaPendientes.map { pendiente ->
                val medicamento = medicamentoUseCase
                    .getMedicamentoById(pendiente.idMedicina.toInt())
                    .firstOrNull()

                val ganado = medicamento?.ganadoId?.let { idGanado ->
                    ganadoUseCase.getGanadoById(idGanado).firstOrNull()
                }

                // Parsear la hora desde string a Date
                val horaParseada: Date = dateFormat.parse(pendiente.hora)

                // Calcular diferencia en horas
                val diferenciaMs = horaParseada.time - ahora.time
                val diferenciaHoras = diferenciaMs / (1000.0 * 60 * 60)

                PendienteCompletoConHora(
                    pendiente = pendiente,
                    medicina = medicamento,
                    ganado = ganado,
                    horasRestantes = diferenciaHoras.roundToLong()
                )
            }
        }
    }


    suspend fun filtrarPendientesPorHora(): PendientesFiltradosPorHora {
        val pendientes = obtenerPendientes().firstOrNull().orEmpty()
        val resultado = PendientesFiltradosPorHora()

        pendientes.forEach { pendienteCompleto ->
            val horas = pendienteCompleto.horasRestantes

            val estado = when {
                horas == 0L -> EstadoPendiente.EN_HORA
                horas > 0L -> EstadoPendiente.FUTURO
                else -> EstadoPendiente.PASADO
            }

            when (estado) {
                EstadoPendiente.EN_HORA -> resultado.mismoDia.add(pendienteCompleto)
                EstadoPendiente.FUTURO -> resultado.futuros.add(pendienteCompleto)
                EstadoPendiente.PASADO -> resultado.pasados.add(pendienteCompleto)
            }
        }

        return resultado
    }


    enum class EstadoPendiente {
        EN_HORA, FUTURO, PASADO
    }
}
