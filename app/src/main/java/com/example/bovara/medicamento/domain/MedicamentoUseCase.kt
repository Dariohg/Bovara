package com.example.bovara.medicamento.domain

import com.example.bovara.medicamento.data.model.MedicamentoEntity
import com.example.bovara.medicamento.data.repository.MedicamentoRepository
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import java.util.Date
import java.util.UUID

class MedicamentoUseCase(private val repository: MedicamentoRepository) {
    fun getMedicamentosByGanadoId(ganadoId: Int): Flow<List<MedicamentoEntity>> =
        repository.getMedicamentosByGanadoId(ganadoId)

    fun getMedicamentoById(id: Int): Flow<MedicamentoEntity?> =
        repository.getMedicamentoById(id)

    fun getMedicamentosAplicados(): Flow<List<MedicamentoEntity>> =
        repository.getMedicamentosAplicados()

    fun getMedicamentosByTipo(tipo: String): Flow<List<MedicamentoEntity>> =
        repository.getMedicamentosByTipo(tipo)

    fun getMedicamentosByLote(lote: String): Flow<List<MedicamentoEntity>> =
        repository.getMedicamentosByLote(lote)

    fun getMedicamentosRecientes(diasAtras: Int = 30): Flow<List<MedicamentoEntity>> {
        val fechaFin = Calendar.getInstance().time
        val fechaInicio = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -diasAtras)
        }.time

        return repository.getMedicamentosByRangoDeFechas(fechaInicio, fechaFin)
    }

    fun getMedicamentosGenericos(): Flow<List<MedicamentoEntity>> =
        repository.getMedicamentosGenericos()

    fun getMedicamentosGenericosByTipo(tipo: String): Flow<List<MedicamentoEntity>> =
        repository.getMedicamentosGenericosByTipo(tipo)

    suspend fun saveMedicamento(
        id: Int = 0,
        nombre: String,
        descripcion: String,
        fechaAplicacion: Date = Date(),
        dosisML: Float,
        ganadoId: Int = 0,
        tipo: String = "vacuna",
        lote: String? = null,
        aplicado: Boolean = true,
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
            fechaAplicacion = fechaAplicacion,
            dosisML = dosisML,
            ganadoId = actualGanadoId,
            tipo = tipo,
            lote = lote,
            aplicado = aplicado,
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
                lote = loteId,
                aplicado = true,
                notas = notas,
                fechaRegistro = Date()
            )
        }

        return repository.insertMedicamentos(medicamentos)
    }

    suspend fun updateMedicamento(medicamento: MedicamentoEntity) =
        repository.updateMedicamento(medicamento)

    suspend fun deleteMedicamento(medicamento: MedicamentoEntity) =
        repository.deleteMedicamento(medicamento)
}