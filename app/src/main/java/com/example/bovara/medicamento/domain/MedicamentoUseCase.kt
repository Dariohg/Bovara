package com.example.bovara.medicamento.domain

import com.example.bovara.medicamento.data.model.MedicamentoEntity
import com.example.bovara.medicamento.data.repository.MedicamentoRepository
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import java.util.Date

class MedicamentoUseCase(private val repository: MedicamentoRepository) {
    fun getMedicamentosByGanadoId(ganadoId: Int): Flow<List<MedicamentoEntity>> =
        repository.getMedicamentosByGanadoId(ganadoId)

    fun getMedicamentoById(id: Int): Flow<MedicamentoEntity?> =
        repository.getMedicamentoById(id)

    fun getMedicamentosProgramados(): Flow<List<MedicamentoEntity>> =
        repository.getMedicamentosProgramados()

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
        fechaAplicacion: Date = Date(), // Por defecto la fecha actual
        dosisML: Float,
        ganadoId: Int,
        esProgramado: Boolean = false
    ): Long {
        // Validaciones
        require(nombre.isNotBlank()) { "El nombre no puede estar vacío" }
        require(dosisML > 0) { "La dosis debe ser mayor que 0" }

        val medicamento = MedicamentoEntity(
            id = id,
            nombre = nombre,
            descripcion = descripcion,
            fechaAplicacion = fechaAplicacion,
            dosisML = dosisML,
            ganadoId = ganadoId,
            esProgramado = esProgramado
        )

        return repository.insertMedicamento(medicamento)
    }

    suspend fun updateMedicamento(medicamento: MedicamentoEntity) =
        repository.updateMedicamento(medicamento)

    suspend fun deleteMedicamento(medicamento: MedicamentoEntity) =
        repository.deleteMedicamento(medicamento)
}