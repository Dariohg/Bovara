package com.example.bovara.core.notification.helper

import com.example.bovara.ganado.domain.GanadoUseCase
import com.example.bovara.medicamento.domain.MedicamentoUseCase
import com.example.bovara.pendiente.domain.PendienteUseCase
import com.example.bovara.pendiente.data.model.PendienteEntity
import com.example.bovara.medicamento.data.model.MedicamentoEntity
import com.example.bovara.ganado.data.model.GanadoEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.util.Calendar
import java.util.Date

data class PendienteCompleto(
    val pendiente: PendienteEntity,
    val medicina: MedicamentoEntity?,
    val ganado: GanadoEntity?,
    val diasFaltantes: Int? // Agregamos esta propiedad
)
data class PendientesFiltradosPorFecha(
    val futuros: List<PendienteCompleto>,
    val pasados: List<PendienteCompleto>,
    val mismoDia: List<PendienteCompleto>
)


class PendienteCompletoUseCase(
    private val pendienteUseCase: PendienteUseCase,
    private val medicamentoUseCase: MedicamentoUseCase,
    private val ganadoUseCase: GanadoUseCase
) {

    fun obtenerPendientesCompletos(): Flow<List<PendienteCompleto>> {
        return pendienteUseCase.obtenerTodos().map { listaPendientes ->
            listaPendientes.map { pendiente ->
                val medicamento = medicamentoUseCase
                    .getMedicamentoById(pendiente.idMedicina.toInt())
                    .firstOrNull()

                val ganado = medicamento?.ganadoId?.let { idGanado ->
                    ganadoUseCase.getGanadoById(idGanado).firstOrNull()
                }

                val diasFaltantes = calcularDiasFaltantes(pendiente.fechaProgramada)

                PendienteCompleto(
                    pendiente = pendiente,
                    medicina = medicamento,
                    ganado = ganado,
                    diasFaltantes = diasFaltantes
                )
            }
        }
    }

    // Función para calcular los días faltantes
    private fun calcularDiasFaltantes(fechaPendiente: Date): Int {
        val calPendiente = Calendar.getInstance().apply { time = fechaPendiente }
        val calHoy = Calendar.getInstance()

        val diff = calPendiente.timeInMillis - calHoy.timeInMillis
        val diasFaltantes = (diff / (1000 * 60 * 60 * 24)).toInt() // Días restantes

        return diasFaltantes
    }

    suspend fun obtenerPendientesCompletosFiltradosPorFecha(
        fechaReferencia: Date
    ): PendientesFiltradosPorFecha {
        val pendientes = obtenerPendientesCompletos().firstOrNull().orEmpty()
        val filtrados = filtrarPendientesPorFechasCercanas(pendientes, fechaReferencia)

        val calRef = Calendar.getInstance().apply { time = fechaReferencia }

        val mismos = mutableListOf<PendienteCompleto>()
        val pasados = mutableListOf<PendienteCompleto>()
        val futuros = mutableListOf<PendienteCompleto>()

        filtrados.forEach { pendienteCompleto ->
            val fechaPendiente = pendienteCompleto.pendiente.fechaProgramada

            // Modificamos la lógica para evitar duplicados en día cero
            when {
                esMismoDia(fechaPendiente, calRef) -> {
                    // Si es el mismo día, solo lo agregamos a "mismos"
                    mismos.add(pendienteCompleto)
                }
                fechaPendiente.before(calRef.time) -> {
                    // Si es anterior, lo agregamos a "pasados"
                    pasados.add(pendienteCompleto)
                }
                else -> {
                    // Si es posterior y NO es el mismo día, lo agregamos a "futuros"
                    // Verificamos que no sea día cero (0 días faltantes)
                    if (pendienteCompleto.diasFaltantes == null || pendienteCompleto.diasFaltantes > 0) {
                        futuros.add(pendienteCompleto)
                    }
                }
            }
        }

        return PendientesFiltradosPorFecha(
            futuros = futuros,
            pasados = pasados,
            mismoDia = mismos
        )
    }

    private fun filtrarPendientesPorFechasCercanas(
        pendientes: List<PendienteCompleto>,
        fechaReferencia: Date
    ): List<PendienteCompleto> {
        val diasMargen = listOf(0, 1, 3, 5)

        val rangos = diasMargen.flatMap { margen ->
            listOf(
                calcularRangoFecha(fechaReferencia, -margen),
                calcularRangoFecha(fechaReferencia, margen)
            )
        }.distinct() // Incluye el mismo día también

        return pendientes.filter { pendienteCompleto ->
            val fechaPendiente = pendienteCompleto.pendiente.fechaProgramada
            rangos.any { (inicio, fin) ->
                fechaPendiente in inicio..fin
            } && pendienteCompleto.diasFaltantes != null
        }
    }

    private fun calcularRangoFecha(base: Date, diasOffset: Int): Pair<Date, Date> {
        val calInicio = Calendar.getInstance().apply {
            time = base
            add(Calendar.DAY_OF_YEAR, diasOffset)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val calFin = Calendar.getInstance().apply {
            time = base
            add(Calendar.DAY_OF_YEAR, diasOffset)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }

        return Pair(calInicio.time, calFin.time)
    }

    private fun esMismoDia(fecha: Date, calRef: Calendar): Boolean {
        val calFecha = Calendar.getInstance().apply { time = fecha }
        return calFecha.get(Calendar.YEAR) == calRef.get(Calendar.YEAR) &&
                calFecha.get(Calendar.DAY_OF_YEAR) == calRef.get(Calendar.DAY_OF_YEAR)
    }
}

