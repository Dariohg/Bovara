package com.example.bovara.medicamento.domain

import com.example.bovara.medicamento.data.model.MedicamentoEntity
import com.example.bovara.medicamento.data.repository.MedicamentoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.util.Calendar
import java.util.Date
import java.util.UUID

class MedicamentoUseCase(private val repository: MedicamentoRepository) {
    fun getMedicamentosByGanadoId(ganadoId: Int): Flow<List<MedicamentoEntity>> =
        repository.getMedicamentosByGanadoId(ganadoId)

    fun getMedicamentoById(id: Int): Flow<MedicamentoEntity?> =
        repository.getMedicamentoById(id)

    fun getMedicamentosProgramadosPendientes(): Flow<List<MedicamentoEntity>> =
        repository.getMedicamentosProgramadosPendientes()

    fun getMedicamentosAplicados(): Flow<List<MedicamentoEntity>> =
        repository.getMedicamentosAplicados()

    fun getMedicamentosByTipo(tipo: String): Flow<List<MedicamentoEntity>> =
        repository.getMedicamentosByTipo(tipo)

    fun getMedicamentosByLote(lote: String): Flow<List<MedicamentoEntity>> =
        repository.getMedicamentosByLote(lote)

    fun getMedicamentosGenericos(): Flow<List<MedicamentoEntity>> =
        repository.getMedicamentosGenericos()

    fun getMedicamentosGenericosByTipo(tipo: String): Flow<List<MedicamentoEntity>> =
        repository.getMedicamentosGenericosByTipo(tipo)

    fun getMedicamentosRecientes(diasAtras: Int = 30): Flow<List<MedicamentoEntity>> {
        val fechaFin = Calendar.getInstance().time
        val fechaInicio = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -diasAtras)
        }.time

        return repository.getMedicamentosByRangoDeFechas(fechaInicio, fechaFin)
    }


    suspend fun saveMedicamento(
        id: Int = 0,
        nombre: String,
        descripcion: String,
        fechaAplicacion: Date = Date(),
        dosisML: Float,
        ganadoId: Int = 0,  // Aquí está el problema
        tipo: String = "vacuna",
        esProgramado: Boolean = false,
        lote: String? = null,
        aplicado: Boolean = false,
        fechaProgramada: Date? = null,
        recordatorio: Boolean = false,
        notas: String? = null
    ): Long {
        // Validaciones
        require(nombre.isNotBlank()) { "El nombre no puede estar vacío" }
        require(dosisML > 0) { "La dosis debe ser mayor que 0" }

        val actualGanadoId = if (ganadoId <= 0) null else ganadoId

        val medicamento = MedicamentoEntity(
            id = id,
            nombre = nombre,
            descripcion = descripcion,
            fechaAplicacion = if (aplicado) fechaAplicacion else Date(),
            dosisML = dosisML,
            ganadoId = actualGanadoId,
            tipo = tipo,
            esProgramado = esProgramado,
            lote = lote,
            aplicado = aplicado,
            fechaProgramada = fechaProgramada,
            recordatorio = recordatorio,
            notas = notas,
            fechaRegistro = Date()
        )

        return repository.insertMedicamento(medicamento)
    }

    // Función para registrar vacunaciones en lote
    suspend fun saveMedicamentosEnLote(
        ganados: List<Int>,
        nombre: String,
        descripcion: String,
        fechaAplicacion: Date,
        dosisML: Float,
        tipo: String = "vacuna",
        esProgramado: Boolean = false,
        aplicado: Boolean = true,
        fechaProgramada: Date? = null,
        recordatorio: Boolean = false,
        notas: String? = null
    ): List<Long> {
        // Generar un ID de lote único
        val loteId = "LOTE-${UUID.randomUUID().toString().substring(0, 8)}"

        val medicamentos = ganados.map { ganadoId ->
            MedicamentoEntity(
                nombre = nombre,
                descripcion = descripcion,
                fechaAplicacion = fechaAplicacion,
                dosisML = dosisML,
                ganadoId = ganadoId,
                tipo = tipo,
                esProgramado = esProgramado,
                lote = loteId,
                aplicado = aplicado,
                fechaProgramada = fechaProgramada,
                recordatorio = recordatorio,
                notas = notas,
                fechaRegistro = Date()
            )
        }

        return repository.insertMedicamentos(medicamentos)
    }

    // Programa vacunaciones futuras en lote
    suspend fun programarMedicamentosEnLote(
        ganados: List<Int>,
        nombre: String,
        descripcion: String,
        fechaProgramada: Date,
        dosisML: Float,
        tipo: String = "vacuna",
        recordatorio: Boolean = true,
        notas: String? = null
    ): List<Long> {
        val loteId = "PROG-${UUID.randomUUID().toString().substring(0, 8)}"

        val medicamentos = ganados.map { ganadoId ->
            MedicamentoEntity(
                nombre = nombre,
                descripcion = descripcion,
                fechaAplicacion = Date(), // Fecha actual como marcador
                dosisML = dosisML,
                ganadoId = ganadoId,
                tipo = tipo,
                esProgramado = true,
                lote = loteId,
                aplicado = false, // No aplicado aún
                fechaProgramada = fechaProgramada,
                recordatorio = recordatorio,
                notas = notas,
                fechaRegistro = Date()
            )
        }

        return repository.insertMedicamentos(medicamentos)
    }

    suspend fun marcarComoAplicado(id: Int, fechaAplicacion: Date = Date()): Boolean {
        try {
            val medicamentoFlow = repository.getMedicamentoById(id)
            val medicamento = medicamentoFlow.firstOrNull() ?: return false

            val actualizado = medicamento.copy(
                aplicado = true,
                fechaAplicacion = fechaAplicacion
            )

            repository.updateMedicamento(actualizado)
            return true
        } catch (e: Exception) {
            return false
        }
    }

    suspend fun updateMedicamento(medicamento: MedicamentoEntity) =
        repository.updateMedicamento(medicamento)

    suspend fun deleteMedicamento(medicamento: MedicamentoEntity) =
        repository.deleteMedicamento(medicamento)
}